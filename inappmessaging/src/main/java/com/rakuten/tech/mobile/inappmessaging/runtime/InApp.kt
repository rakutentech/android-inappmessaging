package com.rakuten.tech.mobile.inappmessaging.runtime

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.SessionManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.EventMatchingUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

@SuppressWarnings(
    "TooManyFunctions",
    "TooGenericExceptionCaught",
    "LongParameterList"
)
internal class InApp(
    private val context: Context,
    isDebugLogging: Boolean,
    private val displayManager: DisplayManager = DisplayManager.instance(),
    private var isCacheHandling: Boolean = BuildConfig.IS_CACHE_HANDLING,
    private val eventsManager: EventsManager = EventsManager,
    private val eventMatchingUtil: EventMatchingUtil = EventMatchingUtil.instance(),
    private val messageReadinessManager: MessageReadinessManager = MessageReadinessManager.instance(),
    private val accountRepo: AccountRepository = AccountRepository.instance(),
    private val sessionManager: SessionManager = SessionManager
) : InAppMessaging() {

    // Used for displaying or removing messages from screen.
    private var activityWeakReference: WeakReference<Activity>? = null

    @VisibleForTesting
    internal val tempEventList = ArrayList<Event>()

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

    override fun registerPreference(userInfoProvider: UserInfoProvider) {
        try {
            accountRepo.userInfoProvider = userInfoProvider
            accountRepo.updateUserInfo()
        } catch (ex: Exception) {
            errorCallback?.let {
                it(InAppMessagingException("In-App Messaging register preference failed", ex))
            }
        }
    }

    override fun registerMessageDisplayActivity(activity: Activity) {
        try {
            activityWeakReference = WeakReference(activity)
            // Making worker thread to display message.
            if (ConfigResponseRepository.instance().isConfigEnabled()) {
                displayManager.displayMessage()
            }
        } catch (ex: Exception) {
            errorCallback?.let {
                it(InAppMessagingException("In-App Messaging register activity failed", ex))
            }
        }
    }

    @SuppressWarnings("FunctionMaxLength")
    override fun unregisterMessageDisplayActivity() {
        try {
            if (ConfigResponseRepository.instance().isConfigEnabled()) {
                displayManager.removeMessage(getRegisteredActivity())
            }
            activityWeakReference?.clear()

            InAppLogger(TAG).debug("unregisterMessageDisplayActivity()")
        } catch (ex: Exception) {
            errorCallback?.let {
                it(InAppMessagingException("In-App Messaging unregister activity failed", ex))
            }
        }
    }

    override fun logEvent(event: Event) {
        try {
            val isEnabled = ConfigResponseRepository.instance().isConfigEnabled()
            val hasUserChanged = userDidChange()

            if (isEnabled && !hasUserChanged) {
                eventsManager.onEventReceived(event)
            } else if (!isEnabled || hasUserChanged) {
                // Save temp events while config is not enabled yet, or
                // when there is a change in user (ping in progress)
                synchronized(tempEventList) {
                    tempEventList.add(event)
                }
            }
        } catch (ex: Exception) {
            errorCallback?.let {
                it(InAppMessagingException("In-App Messaging log event failed", ex))
            }
        }
    }

    override fun closeMessage(clearQueuedCampaigns: Boolean) {
        try {
            if (ConfigResponseRepository.instance().isConfigEnabled()) {
                CoroutineScope(Dispatchers.Main).launch {
                    // called inside main dispatcher to make sure that it is always called in UI thread
                    removeMessage(clearQueuedCampaigns)
                }
            }
        } catch (ex: Exception) {
            errorCallback?.let {
                it(InAppMessagingException("In-App Messaging close message failed", ex))
            }
        }
    }

    @VisibleForTesting
    internal fun userDidChange(): Boolean {
        if (accountRepo.updateUserInfo()) {
            // Update user-related data such as cache and ping data
            sessionManager.onSessionUpdate()
            return true
        }
        return false
    }

    // ------------------------------------Library Internal APIs-------------------------------------
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun getRegisteredActivity() = activityWeakReference?.get()

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun getHostAppContext() = context

    override fun isLocalCachingEnabled() = isCacheHandling

    override fun flushEventList() {
        synchronized(tempEventList) {
            tempEventList.forEach { ev ->
                eventMatchingUtil.matchAndStore(ev)
            }
            tempEventList.clear()
        }
    }

    @VisibleForTesting
    internal fun removeMessage(clearQueuedCampaigns: Boolean) {
        val id = displayManager.removeMessage(getRegisteredActivity())

        if (clearQueuedCampaigns) {
            messageReadinessManager.clearMessages()
        } else if (id != null) {
            displayManager.displayMessage()
        }
    }

    internal class AppManifestConfig(val context: Context) {

        @SuppressWarnings("Deprecation")
        private val metadata = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            ).metaData
        } else {
            context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).metaData
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
    }

    companion object {
        private const val TAG = "IAM_InAppMessaging"
    }
}
