package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.InApp
import com.rakuten.tech.mobile.inappmessaging.runtime.api.MessageMixerRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalDisplayedMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalOptedOutMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ReadyForDisplayMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.PingResponseMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.DisplayPermissionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.displaypermission.DisplayPermissionResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkerUtils
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import retrofit2.Call
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The MessageReadinessManager dispatches the actual work to check if a message is ready to display.
 * Returns the next ready to display message.
 */
internal interface MessageReadinessManager {
    /**
     * This method returns the next ready to display message.
     */
    @WorkerThread
    @SuppressWarnings("LongMethod", "ReturnCount")
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
    @SuppressWarnings("FunctionMaxLength")
    fun getDisplayPermissionResponseCall(displayPermissionUrl: String, request: DisplayPermissionRequest):
            Call<DisplayPermissionResponse>

    companion object {
        private const val TAG = "IAM_MsgReadinessManager"
        private const val DISP_TAG = "IAM_DisplayPermission"
        private var instance: MessageReadinessManager = MessageReadinessManagerImpl()
        internal val shouldRetry = AtomicBoolean(true)
        fun instance() = instance
    }

    private class MessageReadinessManagerImpl : MessageReadinessManager {
        @WorkerThread
        @SuppressWarnings("LongMethod", "ReturnCount")
        override fun getNextDisplayMessage(): Message? {
            shouldRetry.set(true)
            val messageList: List<Message> = ReadyForDisplayMessageRepository.instance().getAllMessagesCopy()
            for (message in messageList) {
                Logger(TAG).debug("checking permission for message: %s", message.getCampaignId())

                // First, check if this message should be displayed.
                if (!shouldDisplayMessage(message)) {
                    Logger(TAG).debug("skipping message: %s", message.getCampaignId())
                    // Skip to next message.
                    continue
                }

                // If message is test message, no need to do more checks.
                if (message.isTest()) {
                    Logger(TAG).debug("skipping test message: %s", message.getCampaignId())
                    return message
                }

                // Check message display permission with server.
                val displayPermissionResponse = getMessagePermission(message)
                // If server wants SDK to ping for updated messages, do a new ping request and break this loop.
                if (isPingServerNeeded(displayPermissionResponse)) {
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
                message.getCampaignId(),
                HostAppInfoRepository.instance().getVersion(),
                BuildConfig.VERSION_NAME,
                HostAppInfoRepository.instance().getDeviceLocale(),
                PingResponseMessageRepository.instance().lastPingMillis,
                RuntimeUtil.getUserIdentifiers()
            )
        }

        @VisibleForTesting
        @SuppressWarnings("FunctionMaxLength")
        override fun getDisplayPermissionResponseCall(
            displayPermissionUrl: String,
            request: DisplayPermissionRequest
        ):
                Call<DisplayPermissionResponse> =
            RuntimeUtil.getRetrofit()
                .create(MessageMixerRetrofitService::class.java)
                .getDisplayPermissionService(
                    HostAppInfoRepository.instance().getInAppMessagingSubscriptionKey(),
                    AccountRepository.instance().getAccessToken(),
                    displayPermissionUrl,
                    request
                )

        /**
         * This method checks if the message displayed less than its max impressions,
         * or message has been opted out.
         */
        private fun shouldDisplayMessage(message: Message): Boolean =
            (LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message)
                    < message.getMaxImpressions()) &&
                    !LocalOptedOutMessageRepository.instance().hasMessage(message.getCampaignId())

        /**
         * This methods checks if According to the message display permission response parameter,
         * return if message is outdated.
         */
        private fun isPingServerNeeded(response: DisplayPermissionResponse?): Boolean =
            (response != null) && response.performPing

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
                getDisplayPermissionResponseCall(displayPermissionUrl, request)
            AccountRepository.instance().logWarningForUserInfo(TAG)
            return executeDisplayRequest(permissionCall)
        }

        @SuppressWarnings("TooGenericExceptionCaught", "LongMethod")
        private fun executeDisplayRequest(call: Call<DisplayPermissionResponse>): DisplayPermissionResponse? {
            try {
                val response = call.execute()
                return when {
                    response.isSuccessful -> {
                        Logger(DISP_TAG).debug(
                            "display: %b performPing: %b", response.body()?.display, response.body()?.performPing
                        )
                        response.body()
                    }
                    response.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> checkAndRetry(call.clone()) {
                        WorkerUtils.logRequestError(DISP_TAG, response.code(), response.errorBody()?.string())
                    }
                    else -> {
                        WorkerUtils.logRequestError(DISP_TAG, response.code(), response.errorBody()?.string())
                        null
                    }
                }
            } catch (e: Exception) {
                return checkAndRetry(call.clone()) {
                    Logger(DISP_TAG).error(e.message)
                    InApp.errorCallback?.let {
                        it(InAppMessagingException("In-App Messaging display permission request failed", e))
                    }
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
