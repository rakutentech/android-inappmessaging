package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.api.MessageMixerRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.DisplayPermissionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.displaypermission.DisplayPermissionResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ResourceUtils
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkerUtils
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
internal interface MessageReadinessManager {
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
    fun getDisplayCall(displayPermissionUrl: String, request: DisplayPermissionRequest):
        Call<DisplayPermissionResponse>

    companion object {
        private const val TAG = "IAM_MsgReadinessManager"
        private const val DISP_TAG = "IAM_DisplayPermission"
        private var instance: MessageReadinessManager = MessageReadinessManagerImpl(
            CampaignRepository.instance()
        )
        internal val shouldRetry = AtomicBoolean(true)
        fun instance() = instance
    }

    @SuppressWarnings("TooManyFunctions", "LargeClass")
    class MessageReadinessManagerImpl(private val campaignRepo: CampaignRepository) : MessageReadinessManager {
        internal val queuedMessages = mutableListOf<String>()
        private val queuedTooltips = mutableListOf<String>()

        override fun addMessageToQueue(id: String) {
            val message = campaignRepo.messages[id] ?: return
            val queue = if (message.getType() == InAppMessageType.TOOLTIP.typeId) queuedTooltips else queuedMessages
            synchronized(queue) { queue.add(id) }
        }

        override fun removeMessageFromQueue(id: String) {
            val message = campaignRepo.messages[id] ?: return
            val queue = if (message.getType() == InAppMessageType.TOOLTIP.typeId) queuedTooltips else queuedMessages
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
                    InAppLogger(TAG).debug("Queued campaign $messageId does not exist in the repository anymore")
                    continue
                }

                InAppLogger(TAG).debug("checking permission for message: %s", message.getCampaignId())

                // First, check if this message should be displayed.
                if (!shouldDisplayMessage(message)) {
                    InAppLogger(TAG).debug("skipping message: %s", message.getCampaignId())
                    // Skip to next message.
                    continue
                }

                // If message is test message, no need to do more checks.
                if (shouldPing(message, result)) break

                // Multiple tooltips can be displayed, checked other from queue.
                if (queuedTooltips.isNotEmpty()) continue
                else if (result.isNotEmpty()) return result
            }
            return result
        }

        private fun shouldPing(message: Message, result: MutableList<Message>) = if (message.isTest()) {
            InAppLogger(TAG).debug("skipping test message: %s", message.getCampaignId())
            result.add(message)
            false
        } else {
            // Check message display permission with server.
            val displayPermissionResponse = getMessagePermission(message)
            // If server wants SDK to ping for updated messages, do a new ping request and break this loop.
            when {
                (displayPermissionResponse != null) && displayPermissionResponse.performPing -> {
                    // reset current delay to initial
                    MessageMixerPingScheduler.currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
                    MessageMixerPingScheduler.instance().pingMessageMixerService(0)
                    true
                }
                isMessagePermissibleToDisplay(displayPermissionResponse) -> {
                    result.add(message)
                    false
                }
                else -> false
            }
        }

        @VisibleForTesting
        override fun getDisplayPermissionRequest(message: Message): DisplayPermissionRequest {
            return DisplayPermissionRequest(
                campaignId = message.getCampaignId(),
                appVersion = HostAppInfoRepository.instance().getVersion(),
                sdkVersion = BuildConfig.VERSION_NAME,
                locale = HostAppInfoRepository.instance().getDeviceLocale(),
                lastPingInMillis = CampaignRepository.instance().lastSyncMillis ?: 0,
                userIdentifier = RuntimeUtil.getUserIdentifiers()
            )
        }

        @VisibleForTesting
        override fun getDisplayCall(
            displayPermissionUrl: String,
            request: DisplayPermissionRequest
        ):
            Call<DisplayPermissionResponse> =
            RuntimeUtil.getRetrofit()
                .create(MessageMixerRetrofitService::class.java)
                .getDisplayPermissionService(
                    subscriptionId = HostAppInfoRepository.instance().getSubscriptionKey(),
                    accessToken = AccountRepository.instance().getAccessToken(),
                    url = displayPermissionUrl,
                    request = request
                )

        /**
         * This method checks if the message has infinite impressions, or has been displayed less
         * than its max impressions, or has been opted out.
         * Additional checks are performed depending on message type.
         */
        private fun shouldDisplayMessage(message: Message): Boolean {
            val impressions = message.impressionsLeft ?: message.getMaxImpressions()
            val isOptOut = message.isOptedOut == true
            val hasPassedBasicCheck = (message.infiniteImpressions() || impressions > 0) && !isOptOut

            return if (message.getType() == InAppMessageType.TOOLTIP.typeId) {
                val shouldDisplayTooltip = hasPassedBasicCheck &&
                    isTooltipTargetViewVisible(message) // if view where to attach tooltip is indeed visible
                shouldDisplayTooltip
            } else {
                hasPassedBasicCheck
            }
        }

        @SuppressWarnings("ReplaceSafeCallChainWithRun")
        private fun isTooltipTargetViewVisible(message: Message): Boolean {
            val activity = InAppMessaging.instance().getRegisteredActivity()
            if (activity != null) {
                val view = message.getTooltipConfig()?.id?.let { ResourceUtils.findViewByName<View>(activity, it) }
                view?.let { return ViewUtil.isViewVisible(it) }
            }
            return false
        }

        /**
         * This method returns if message is permissible to be displayed according to the message.
         * display permission response parameter.
         */
        private fun isMessagePermissibleToDisplay(
            response: DisplayPermissionResponse?
        ): Boolean = response != null && response.display

        /**
         * This method returns display message permission (from server).
         */
        private fun getMessagePermission(message: Message): DisplayPermissionResponse? {
            // Prepare request data.
            val displayPermissionUrl: String = ConfigResponseRepository.instance().getDisplayPermissionEndpoint()
            if (displayPermissionUrl.isEmpty()) return null

            // Prepare network request.
            val request = getDisplayPermissionRequest(message)
            val permissionCall: Call<DisplayPermissionResponse> =
                getDisplayCall(displayPermissionUrl, request)
            AccountRepository.instance().logWarningForUserInfo(TAG)
            return executeDisplayRequest(permissionCall)
        }

        @SuppressWarnings("TooGenericExceptionCaught")
        private fun executeDisplayRequest(call: Call<DisplayPermissionResponse>): DisplayPermissionResponse? {
            return try {
                val response = call.execute()
                handleResponse(response, call.clone())
            } catch (e: Exception) {
                checkAndRetry(call.clone()) {
                    InAppLogger(DISP_TAG).error(e.message)
                    InAppMessaging.errorCallback?.let {
                        it(InAppMessagingException("In-App Messaging display permission request failed", e))
                    }
                }
            }
        }

        private fun handleResponse(
            response: Response<DisplayPermissionResponse>,
            callClone: Call<DisplayPermissionResponse>
        ): DisplayPermissionResponse? {
            return when {
                response.isSuccessful -> {
                    InAppLogger(DISP_TAG).debug(
                        "display: %b performPing: %b", response.body()?.display, response.body()?.performPing
                    )
                    response.body()
                }
                response.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> checkAndRetry(callClone) {
                    WorkerUtils.logRequestError(DISP_TAG, response.code(), response.errorBody()?.string())
                }
                else -> {
                    WorkerUtils.logRequestError(DISP_TAG, response.code(), response.errorBody()?.string())
                    null
                }
            }
        }

        private fun checkAndRetry(
            call: Call<DisplayPermissionResponse>,
            errorHandling: () -> Unit
        ): DisplayPermissionResponse? {
            return if (shouldRetry.getAndSet(false)) {
                executeDisplayRequest(call)
            } else {
                errorHandling.invoke()
                null
            }
        }
    }
}
