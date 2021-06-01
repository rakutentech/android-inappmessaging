package com.rakuten.tech.mobile.inappmessaging.runtime

import android.app.Activity
import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalDisplayedMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalEventRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ReadyForDisplayMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.SessionManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.SharedPreferencesUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference

@SuppressWarnings("TooManyFunctions")
internal class InApp(
    private val context: Context,
    isDebugLogging: Boolean,
    private val displayManager: DisplayManager = DisplayManager.instance(),
    private var isCacheHandling: Boolean = BuildConfig.IS_CACHE_HANDLING,
    private val eventsManager: EventsManager = EventsManager
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
    override var onVerifyContext: (contexts: List<String>, campaignTitle: String) -> Boolean = { _, _ -> Boolean
        // Allow all contexts by default
        true
    }

    override fun registerPreference(userInfoProvider: UserInfoProvider) {
        AccountRepository.instance().userInfoProvider = userInfoProvider
        AccountRepository.instance().updateUserInfo()
    }

    override fun registerMessageDisplayActivity(activity: Activity) {
        activityWeakReference = WeakReference(activity)
        // Making worker thread to display message.
        if (ConfigResponseRepository.instance().isConfigEnabled()) {
            displayManager.displayMessage()
        }
    }

    @Suppress("FunctionMaxLength")
    override fun unregisterMessageDisplayActivity() {
        if (ConfigResponseRepository.instance().isConfigEnabled()) {
            val id = displayManager.removeMessage(getRegisteredActivity())
            LocalDisplayedMessageRepository.instance().setRemovedMessage(id as String?)
        }
        activityWeakReference?.clear()

        Timber.tag(TAG)
        Timber.d("unregisterMessageDisplayActivity()")
    }

    override fun logEvent(event: Event) {
        if (ConfigResponseRepository.instance().isConfigEnabled()) {
            eventsManager.onEventReceived(event)
        } else {
            synchronized(tempEventList) {
                tempEventList.add(event)
            }
        }
    }

    override fun updateSession() {
        if (ConfigResponseRepository.instance().isConfigEnabled()) {
            // Updates the current session to update all locally stored messages
            SessionManager.onSessionUpdate()
        }
    }

    override fun closeMessage(clearQueuedCampaigns: Boolean) {
        if (ConfigResponseRepository.instance().isConfigEnabled()) {
            CoroutineScope(Dispatchers.Main).launch {
                // called inside main dispatcher to make sure that it is always called in UI thread
                removeMessage(clearQueuedCampaigns)
            }
        }
    }

    // ------------------------------------Library Internal APIs-------------------------------------
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun getRegisteredActivity() = if (activityWeakReference != null) activityWeakReference!!.get() else null

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun getHostAppContext() = context

    override fun isLocalCachingEnabled() = isCacheHandling

    override fun getSharedPref() = SharedPreferencesUtil.createSharedPreference(context,
            AccountRepository.instance().userInfoHash)

    override fun saveTempData() {
        AccountRepository.instance().updateUserInfo()
        synchronized(tempEventList) {
            tempEventList.forEach { LocalEventRepository.instance().addEvent(it) }
            tempEventList.clear()
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

    companion object {
        private const val TAG = "IAM_InAppMessaging"
    }
}
