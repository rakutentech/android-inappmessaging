package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.api.MessageMixerRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.DisplayPermissionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.displaypermission.DisplayPermissionResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkerUtils
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
     * Adds a message Id to ready for display.
     */
    fun addMessageToQueue(id: String)

    /**
     * Removes a message Id to ready for display.
     */
    fun removeMessageToQueue(id: String)

    /**
     * Clears all queued messages Ids for display.
     */
    fun clearMessages()

    /**
     * This method returns the next ready to display message.
     */
    @WorkerThread
    fun getNextDisplayMessage(): Message?

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

    @SuppressWarnings("TooManyFunctions")
    class MessageReadinessManagerImpl(private val campaignRepo: CampaignRepository) : MessageReadinessManager {
        internal val queuedMessages = mutableListOf<String>()

        override fun addMessageToQueue(id: String) {
            synchronized(queuedMessages) {
                queuedMessages.add(id)
            }
        }

        override fun removeMessageToQueue(id: String) {
            synchronized(queuedMessages) {
                queuedMessages.remove(id)
            }
        }

        override fun clearMessages() {
            synchronized(queuedMessages) {
                queuedMessages.clear()
            }
        }

        @WorkerThread
        @SuppressWarnings("LongMethod", "ComplexMethod", "ReturnCount")
        override fun getNextDisplayMessage(): Message? {
            shouldRetry.set(true)

            val queuedMessagesCopy = queuedMessages.toList() // Prevent ConcurrentModificationException
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
                if (message.isTest()) {
                    InAppLogger(TAG).debug("skipping test message: %s", message.getCampaignId())
                    return message
                }

                // Check message display permission with server.
                val displayPermissionResponse = getMessagePermission(message)
                // If server wants SDK to ping for updated messages, do a new ping request and break this loop.
                if ((displayPermissionResponse != null) && displayPermissionResponse.performPing) {
                    // reset current delay to initial
                    MessageMixerPingScheduler.currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
                    MessageMixerPingScheduler.instance().pingMessageMixerService(0)
                    break
                } else if (isMessagePermissibleToDisplay(displayPermissionResponse)) {
                    return message
                }
            }
            return null
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
         */
        private fun shouldDisplayMessage(message: Message): Boolean {
            val impressions = message.impressionsLeft ?: message.getMaxImpressions()
            val isOptOut = message.isOptedOut == true
            return (message.infiniteImpressions() || impressions > 0) && !isOptOut
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
