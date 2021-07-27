package com.rakuten.tech.mobile.inappmessaging.runtime

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalEventRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ReadyForDisplayMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.SessionManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.SharedPreferencesUtil
import com.rakuten.tech.mobile.manifestconfig.annotations.ManifestConfig
import com.rakuten.tech.mobile.manifestconfig.annotations.MetaData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference

@SuppressWarnings("TooManyFunctions", "TooGenericExceptionCaught")
internal class InApp(
    private val context: Context,
    isDebugLogging: Boolean,
    private val displayManager: DisplayManager = DisplayManager.instance(),
    private var isCacheHandling: Boolean = BuildConfig.IS_CACHE_HANDLING,
    private val eventsManager: EventsManager = EventsManager,
    private val sessionManager: SessionManager = SessionManager
) : InAppMessaging() {

    // Used for displaying or removing messages from screen.
    private var activityWeakReference: WeakReference<Activity>? = null

    @VisibleForTesting
    internal var tempEventList = ArrayList<Event>()

    init {
        if (isDebugLogging) {
            // Start logging for debug builds.
            Timber.plant(Timber.DebugTree())
        }

        LegacyEventBroadcasterHelper.setContext(context)
    }

    // ------------------------------------Public APIs-----------------------------------------------
    @NonNull
    override var onVerifyContext: (contexts: List<String>, campaignTitle: String) -> Boolean = { _, _ -> Boolean
        // Allow all contexts by default
        true
    }

    override fun registerPreference(userInfoProvider: UserInfoProvider) {
        try {
            AccountRepository.instance().userInfoProvider = userInfoProvider
            AccountRepository.instance().updateUserInfo()
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

            Timber.tag(TAG)
            Timber.d("unregisterMessageDisplayActivity()")
        } catch (ex: Exception) {
            errorCallback?.let {
                it(InAppMessagingException("In-App Messaging unregister activity failed", ex))
            }
        }
    }

    override fun logEvent(event: Event) {
        try {
            if (ConfigResponseRepository.instance().isConfigEnabled()) {
                eventsManager.onEventReceived(event)
            } else {
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

    override fun updateSession() {
        try {
            if (ConfigResponseRepository.instance().isConfigEnabled()) {
                // Updates the current session to update all locally stored messages
                sessionManager.onSessionUpdate()
            }
        } catch (ex: Exception) {
            errorCallback?.let {
                it(InAppMessagingException("In-App Messaging session update failed", ex))
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

    // ------------------------------------Library Internal APIs-------------------------------------
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun getRegisteredActivity() = activityWeakReference?.get()

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun getHostAppContext() = context

    override fun isLocalCachingEnabled() = isCacheHandling

    override fun getSharedPref() = SharedPreferencesUtil.createSharedPreference(context,
            AccountRepository.instance().userInfoHash)

    override fun saveTempData() {
        try {
            AccountRepository.instance().updateUserInfo()
            synchronized(tempEventList) {
                tempEventList.forEach { LocalEventRepository.instance().addEvent(it) }
                tempEventList.clear()
            }
        } catch (ex: Exception) {
            errorCallback?.let {
                it(InAppMessagingException("In-App Messaging moving temp data to cache failed", ex))
            }
        }
    }

    @VisibleForTesting
    internal fun removeMessage(clearQueuedCampaigns: Boolean) {
        val id = displayManager.removeMessage(getRegisteredActivity())

        if (clearQueuedCampaigns) {
            ReadyForDisplayMessageRepository.instance().clearMessages(true)
        } else if (id != null) {
            ReadyForDisplayMessageRepository.instance().removeMessage(id as String, true)
        }
    }

    @ManifestConfig
    internal interface App {

        /**
         * Subscription Key from the InAppMessaging Dashboard.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.inappmessaging.subscriptionkey")
        fun subscriptionKey(): String?

        /**
         * Config URL for the IAM API.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.inappmessaging.configurl")
        fun configUrl(): String?

        /**
         * Flag to enable/disable debug logging.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.inappmessaging.debugging", value = "false")
        fun isDebugging(): Boolean
    }

    companion object {
        internal var errorCallback: ((ex: Exception) -> Unit)? = null
        private const val TAG = "IAM_InAppMessaging"
    }
}
