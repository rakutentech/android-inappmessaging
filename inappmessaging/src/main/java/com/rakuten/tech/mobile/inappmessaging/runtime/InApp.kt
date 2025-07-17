package com.rakuten.tech.mobile.inappmessaging.runtime

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.SdkApi
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.Event as ELEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.PushPrimerTrackerManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.SessionManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.EventMatchingUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressWarnings("LongParameterList", "TooManyFunctions", "LargeClass")
internal class InApp(
    isDebugLogging: Boolean,
    private val isCacheHandling: Boolean = BuildConfig.IS_CACHE_HANDLING,
    private val displayManager: DisplayManager = DisplayManager.instance(),
    private val eventsManager: EventsManager = EventsManager,
    private val messageReadinessManager: MessageReadinessManager = MessageReadinessManager.instance(),
    private val sessionManager: SessionManager = SessionManager,
    private val primerManager: PushPrimerTrackerManager = PushPrimerTrackerManager,
    private val accountRepo: AccountRepository = AccountRepository.instance(),
    private val campaignRepo: CampaignRepository = CampaignRepository.instance(),
    private val configRepo: ConfigResponseRepository = ConfigResponseRepository.instance(),
    private val hostAppInfoRepo: HostAppInfoRepository = HostAppInfoRepository.instance(),
    private val eventMatchingUtil: EventMatchingUtil = EventMatchingUtil.instance(),
) : InAppMessaging() {

    init {
        // Start logging for debug builds.
        InAppLogger.isDebug = isDebugLogging
    }

    // ------------------------------------Public APIs-----------------------------------------------
    @NonNull
    override var onVerifyContext: (contexts: List<String>, campaignTitle: String) -> Boolean = { _, _ ->
        Boolean
        // Allow all contexts by default
        true
    }

    override var onPushPrimer: (() -> Unit)? = null

    override fun registerPreference(userInfoProvider: UserInfoProvider) {
        InAppLogger(TAG).info("registerPreference: $userInfoProvider")
        accountRepo.userInfoProvider = userInfoProvider
    }

    @SuppressWarnings("TooGenericExceptionCaught")
    override fun registerMessageDisplayActivity(activity: Activity) {
        InAppLogger(TAG).info("registerActivity: $activity")
        try {
            hostAppInfoRepo.registerActivity(activity)
            // Making worker thread to display message.
            if (configRepo.isConfigEnabled()) {
                displayManager.displayMessage()
            }
        } catch (ex: Exception) {
            "In-App Messaging register activity failed".let {
                InAppErrorLogger.logError(
                    TAG,
                    InAppError(
                        it, InAppMessagingException(it, ex),
                        ELEvent.OperationFailed(SdkApi.REGISTER_ACTIVITY.name),
                    ),
                )
            }
        }
    }

    @SuppressWarnings("FunctionMaxLength", "TooGenericExceptionCaught")
    override fun unregisterMessageDisplayActivity() {
        InAppLogger(TAG).info("unregisterActivity")
        try {
            if (configRepo.isConfigEnabled()) {
                displayManager.removeMessage(hostAppInfoRepo.getRegisteredActivity(), removeAll = true)
            }
            hostAppInfoRepo.registerActivity(null)
        } catch (ex: Exception) {
            "In-App Messaging unregister activity failed".let {
                InAppErrorLogger.logError(
                    TAG,
                    InAppError(
                        it, InAppMessagingException(it, ex),
                        ELEvent.OperationFailed(SdkApi.UNREGISTER_ACTIVITY.name),
                    ),
                )
            }
        }
    }

    @SuppressWarnings(
        "TooGenericExceptionCaught",
        "LongMethod",
    )
    override fun logEvent(event: Event) {
        try {
            val isConfigEnabled = configRepo.isConfigEnabled()
            val isSameUser = !accountRepo.updateUserInfo()
            val areCampaignsSynced = campaignRepo.lastSyncMillis != null && eventMatchingUtil.eventBuffer.isEmpty()

            InAppLogger(TAG).info(
                "logEvent: ${event.getEventName()} - isConfigEnabled: $isConfigEnabled," +
                    " isSameUser: $isSameUser, synced: $areCampaignsSynced",
            )
            InAppLogger(TAG).debug("attributes: ${event.getAttributeMap()}")

            if (!isConfigEnabled || !isSameUser || !areCampaignsSynced) {
                // To be processed later (flushed after sync)
                InAppLogger(TAG).debug("event added to buffer")
                eventMatchingUtil.addToEventBuffer(event)
            }

            if (!isSameUser) {
                // Sync campaigns, flush event buffer, then match events
                InAppLogger(TAG).debug("there is a change in user, will perform onSessionUpdate")
                sessionManager.onSessionUpdate()
                return
            }

            if (areCampaignsSynced) {
                // Match event right away
                InAppLogger(TAG).debug("event ${event.getEventName()} will be processed")
                eventsManager.onEventReceived(event)
            }
        } catch (ex: Exception) {
            "In-App Messaging log event failed".let {
                InAppErrorLogger.logError(
                    TAG,
                    InAppError(
                        it, InAppMessagingException(it, ex),
                        ELEvent.OperationFailed(SdkApi.LOG_EVENT.name),
                    ),
                )
            }
        }
    }

    override fun closeMessage(clearQueuedCampaigns: Boolean) {
        InAppLogger(TAG).info("closeMessage: $clearQueuedCampaigns")
        closeCampaign(clearQueuedCampaigns = clearQueuedCampaigns)
    }

    override fun closeTooltip(viewId: String) {
        InAppLogger(TAG).info("closeTooltip: $viewId")
        closeCampaign(viewId = viewId)
    }

    override fun trackPushPrimer(permissions: Array<String>, grantResults: IntArray) {
        InAppLogger(TAG).info("trackPushPrimer - perm: $permissions, res: $grantResults")
        if (!BuildVersionChecker.isAndroidTAndAbove()) {
            return
        }

        var idx = 0
        while (idx < permissions.size && idx < grantResults.size) {
            if (permissions[idx] == Manifest.permission.POST_NOTIFICATIONS) {
                val result = if (grantResults[idx] == PackageManager.PERMISSION_GRANTED) 1 else 0
                primerManager.sendPrimerEvent(result)
                break
            }
            idx++
        }
    }

    // ------------------------------------Library Internal APIs-------------------------------------
    override fun isLocalCachingEnabled() = isCacheHandling

    @SuppressWarnings(
        "TooGenericExceptionCaught",
        "CanBeNonNullable",
        "LongMethod",
    )
    private fun closeCampaign(clearQueuedCampaigns: Boolean? = null, viewId: String? = null) {
        if (configRepo.isConfigEnabled()) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    if (clearQueuedCampaigns != null) {
                        // close normal campaign - `clearQueuedCampaigns` not null
                        removeMessage(clearQueuedCampaigns)
                    } else if (viewId != null) {
                        // close tooltip campaign - `viewId` not null
                        removeMessage(viewId)
                    }
                } catch (ex: Exception) {
                    "In-App Messaging close message failed".let {
                        InAppErrorLogger.logError(
                            TAG,
                            InAppError(
                                it, InAppMessagingException(it, ex),
                                ELEvent.OperationFailed(SdkApi.CLOSE_MESSAGE.name),
                            ),
                        )
                    }
                }
            }
        }
    }

    @VisibleForTesting
    internal fun removeMessage(clearQueuedCampaigns: Boolean) {
        val id = displayManager.removeMessage(hostAppInfoRepo.getRegisteredActivity())

        if (clearQueuedCampaigns) {
            messageReadinessManager.clearMessages()
        } else if (id != null) {
            messageReadinessManager.removeMessageFromQueue(id as String)
            displayManager.displayMessage()
        }
    }

    /**
     * Removes tooltip message by `viewId` .
     */
    @VisibleForTesting
    internal fun removeMessage(viewId: String) {
        val campaignId = campaignRepo
            .messages
            .values
            .firstOrNull { message ->
                message.getTooltipConfig()?.id == viewId
            }
            ?.campaignId

        if (campaignId != null) {
            displayManager.removeMessage(hostAppInfoRepo.getRegisteredActivity(), delay = 0, id = campaignId)
            messageReadinessManager.removeMessageFromQueue(campaignId)
            displayManager.displayMessage() // next message
        }
    }

    internal class AppManifestConfig(val context: Context) {

        @SuppressWarnings("Deprecation")
        private val metadata = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
            ).metaData
        } else {
            context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                .metaData
        }

        /**
         * Subscription Key from the InAppMessaging Dashboard.
         **/
        fun subscriptionKey(): String? = metadata.getString("com.rakuten.tech.mobile.inappmessaging.subscriptionkey")

        /**
         * Config URL for the IAM API.
         **/
        fun configUrl(): String? = metadata.getString("com.rakuten.tech.mobile.inappmessaging.configurl")

        /**
         * Flag to enable/disable debug logging.
         **/
        fun isDebugging(): Boolean = metadata.getBoolean("com.rakuten.tech.mobile.inappmessaging.debugging")

        fun enableEventLogger(): Boolean =
            metadata.getBoolean("com.rakuten.tech.mobile.inappmessaging.enableeventlogger", true)

        fun eventLoggerApiUrl(): String? =
            metadata.getString("com.rakuten.tech.mobile.inappmessaging.eventloggerapiurl")

        fun eventLoggerApiKey(): String? =
            metadata.getString("com.rakuten.tech.mobile.inappmessaging.eventloggerapikey")
    }

    companion object {
        private const val TAG = "IAM_InAppMessaging"
    }
}
