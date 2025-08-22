package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.Manifest
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppError
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppErrorLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.api.MessageMixerRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.DisplayPermissionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.DisplayPermissionResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.BackendApi
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.PermissionUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ViewUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import retrofit2.Call
import retrofit2.Response
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The MessageReadinessManager dispatches the actual work to check if a message is ready to display.
 * Returns the next ready to display message.
 */
internal interface ReadinessManager {
    /**
     * Adds a message by Id as ready for display.
     */
    fun addMessageToQueue(id: String)

    /**
     * Removes a message Id to ready for display.
     */
    fun removeMessageFromQueue(id: String)

    /**
     * Clears all queued messages Ids for display.
     */
    fun clearMessages()

    /**
     * This method returns the next ready to display message.
     */
    @WorkerThread
    fun getNextDisplayMessage(): List<Message>

    /**
     * This method returns a DisplayPermissionRequest object.
     */
    @VisibleForTesting
    fun getDisplayPermissionRequest(message: Message): DisplayPermissionRequest

    /**
     * This method returns a DisplayPermissionResponse object.
     */
    @VisibleForTesting
    fun getDisplayCall(displayPermissionUrl: String, request: DisplayPermissionRequest): Call<DisplayPermissionResponse>
}

@SuppressWarnings(
    "TooManyFunctions",
    "LargeClass",
    "LongParameterList",
)
internal class MessageReadinessManager(
    val campaignRepo: CampaignRepository,
    val configResponseRepo: ConfigResponseRepository,
    val hostAppInfoRepo: HostAppInfoRepository,
    val accountRepo: AccountRepository,
    val pingScheduler: MessageMixerPingScheduler,
    val viewUtil: ViewUtil,
) : ReadinessManager {
    private val queuedMessages = mutableListOf<String>()
    private val queuedTooltips = mutableListOf<String>()

    override fun addMessageToQueue(id: String) {
        val message = campaignRepo.messages[id] ?: return
        val queue = if (message.type == InAppMessageType.TOOLTIP.typeId) queuedTooltips else queuedMessages
        synchronized(queue) { queue.add(id) }
    }

    override fun removeMessageFromQueue(id: String) {
        val message = campaignRepo.messages[id] ?: return
        val queue = if (message.type == InAppMessageType.TOOLTIP.typeId) queuedTooltips else queuedMessages
        synchronized(queue) { queue.remove(id) }
    }

    override fun clearMessages() {
        synchronized(queuedMessages) {
            queuedMessages.clear()
        }
    }

    @WorkerThread
    @SuppressWarnings("LongMethod", "ComplexMethod", "ReturnCount")
    override fun getNextDisplayMessage(): List<Message> {
        shouldRetry.set(true)
        val result = mutableListOf<Message>()
        val hasCampaignsInQueue = queuedMessages.isNotEmpty()
        // toList() to prevent ConcurrentModificationException
        val queuedMessagesCopy = if (hasCampaignsInQueue) queuedMessages.toList() else queuedTooltips.toList()
        for (messageId in queuedMessagesCopy) {
            val message = campaignRepo.messages[messageId]
            if (message == null) {
                InAppLogger(TAG).debug("queued campaign $messageId does not exist in the repository anymore")
                continue
            }

            // First, check if this message should be displayed.
            if (!shouldDisplayMessage(message)) {
                InAppLogger(TAG).info("campaign won't be displayed: ${message.campaignId}")
                // Skip to next message.
                continue
            }

            // If message is test message, no need to do more checks.
            if (shouldPing(message, result)) break

            // Multiple tooltips can be displayed, checked other from queue.
            if (queuedTooltips.isNotEmpty()) {
                continue
            } else if (result.isNotEmpty()) return result
        }
        return result
    }

    @SuppressWarnings("LongMethod")
    private fun shouldPing(message: Message, result: MutableList<Message>) = if (message.isTest) {
        InAppLogger(TAG).debug("skipping test message: ${message.campaignId}")
        result.add(message)
        false
    } else {
        // Check message display permission with server.
        val displayPermissionResponse = getMessagePermission(message)
        // If server wants SDK to ping for updated messages, do a new ping request and break this loop.
        when {
            (displayPermissionResponse != null) && displayPermissionResponse.shouldPing -> {
                // reset current delay to initial
                MessageMixerPingScheduler.currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
                pingScheduler.pingMessageMixerService(0)
                true
            }
            isMessagePermissibleToDisplay(displayPermissionResponse) -> {
                result.add(message)
                false
            }
            else -> {
                removeMessageFromQueue(message.campaignId)
                false
            }
        }
    }

    @VisibleForTesting
    override fun getDisplayPermissionRequest(message: Message): DisplayPermissionRequest {
        return DisplayPermissionRequest(
            campaignId = message.campaignId,
            appVersion = hostAppInfoRepo.getVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            locale = hostAppInfoRepo.getDeviceLocale(),
            lastPingInMillis = campaignRepo.lastSyncMillis ?: 0,
            userIdentifier = RuntimeUtil.getUserIdentifiers(),
            rmcSdkVersion = hostAppInfoRepo.getRmcSdkVersion(),
        )
    }

    @VisibleForTesting
    override fun getDisplayCall(
        displayPermissionUrl: String,
        request: DisplayPermissionRequest,
    ): Call<DisplayPermissionResponse> = RuntimeUtil.getRetrofit()
        .create(MessageMixerRetrofitService::class.java)
        .getDisplayPermissionService(
            subscriptionId = hostAppInfoRepo.getSubscriptionKey(),
            accessToken = accountRepo.getAccessToken(),
            url = displayPermissionUrl,
            request = request,
            deviceId = hostAppInfoRepo.getDeviceId(),
        )

    /**
     * This method checks whether message should be displayed based on opt-out status, impressions, and additional
     * checks based on the campaign type.
     */
    @SuppressWarnings("LongMethod", "ReturnCount")
    private fun shouldDisplayMessage(message: Message): Boolean {
        if (message.isOptedOut == true) {
            return false
        }

        if (!message.areImpressionsInfinite &&
            (message.impressionsLeft ?: message.maxImpressions) <= 0
        ) {
            return false
        }

        if (message.type == InAppMessageType.TOOLTIP.typeId && !isTooltipTargetViewVisible(message)) {
            return false
        }

        // Check if device is unsupported or permission already granted
        if (message.isPushPrimer) {
            if (!BuildVersionChecker.isAndroidTAndAbove()) {
                return false
            }

            hostAppInfoRepo.getContext()?.let {
                return !PermissionUtil.isPermissionGranted(it, Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        return true
    }

    private fun isTooltipTargetViewVisible(message: Message): Boolean {
        val activity = hostAppInfoRepo.getRegisteredActivity()
        val id = message.getTooltipConfig()?.id

        if (activity != null && id != null) {
            return viewUtil.isViewByNameVisible(activity, id)
        }
        return false
    }

    /**
     * This method returns if message is permissible to be displayed according to the message.
     * display permission response parameter.
     */
    private fun isMessagePermissibleToDisplay(response: DisplayPermissionResponse?): Boolean =
        response != null && response.display

    /**
     * This method returns display message permission (from server).
     */
    @SuppressWarnings("ReturnCount")
    private fun getMessagePermission(message: Message): DisplayPermissionResponse? {
        if (!configResponseRepo.isConfigEnabled()) return null

        // Prepare request data.
        val displayPermissionUrl: String = configResponseRepo.getDisplayPermissionEndpoint()
        if (displayPermissionUrl.isEmpty()) {
            InAppErrorLogger.logError(
                DISP_TAG,
                InAppError(
                    "Invalid displayPermission URL",
                    ev = Event.InvalidConfiguration(BackendApi.DISPLAY_PERMISSION.name),
                ),
            )
            return null
        }

        // Prepare network request.
        val request = getDisplayPermissionRequest(message)
        val permissionCall: Call<DisplayPermissionResponse> =
            getDisplayCall(displayPermissionUrl, request)
        accountRepo.logWarningForUserInfo(TAG)
        return executeDisplayRequest(permissionCall)
    }

    @SuppressWarnings("TooGenericExceptionCaught")
    private fun executeDisplayRequest(call: Call<DisplayPermissionResponse>): DisplayPermissionResponse? {
        return try {
            val response = call.execute()
            handleResponse(response, call.clone())
        } catch (e: Exception) {
            checkAndRetry(call.clone()) {
                "In-App Messaging display permission request failed".let {
                    InAppErrorLogger.logError(
                        DISP_TAG,
                        InAppError(
                            it,
                            InAppMessagingException(it, e),
                            ev = Event.OperationFailed(BackendApi.DISPLAY_PERMISSION.name),
                        ),
                    )
                }
            }
        }
    }

    private fun handleResponse(
        response: Response<DisplayPermissionResponse>,
        callClone: Call<DisplayPermissionResponse>,
    ): DisplayPermissionResponse? {
        InAppLogger(DISP_TAG).info("check API - code: ${response.code()}")

        return when {
            response.isSuccessful -> response.body()
            response.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> checkAndRetry(callClone) {
                logFailedResponse(response)
            }
            else -> {
                logFailedResponse(response)
                null
            }
        }
    }

    private fun checkAndRetry(
        call: Call<DisplayPermissionResponse>,
        errorHandling: () -> Unit,
    ): DisplayPermissionResponse? {
        return if (shouldRetry.getAndSet(false)) {
            executeDisplayRequest(call)
        } else {
            errorHandling.invoke()
            null
        }
    }

    private fun logFailedResponse(response: Response<DisplayPermissionResponse>) {
        InAppErrorLogger.logError(
            DISP_TAG,
            InAppError(
                "${BackendApi.DISPLAY_PERMISSION.alias} API failed - ${response.errorBody()?.string()}",
                ev = Event.ApiRequestFailed(BackendApi.DISPLAY_PERMISSION, response.code().toString()),
            ),
        )
    }

    companion object {
        private const val TAG = "IAM_MsgReadinessManager"
        private const val DISP_TAG = "IAM_DisplayPermission"
        private val instance: MessageReadinessManager = MessageReadinessManager(
            campaignRepo = CampaignRepository.instance(),
            configResponseRepo = ConfigResponseRepository.instance(),
            hostAppInfoRepo = HostAppInfoRepository.instance(),
            accountRepo = AccountRepository.instance(),
            pingScheduler = MessageMixerPingScheduler.instance(),
            viewUtil = ViewUtil,
        )
        internal val shouldRetry = AtomicBoolean(true)
        fun instance() = instance
    }
}
