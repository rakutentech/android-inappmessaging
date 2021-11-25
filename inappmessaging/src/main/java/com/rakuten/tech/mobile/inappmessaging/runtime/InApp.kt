package com.rakuten.tech.mobile.inappmessaging.runtime

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalEventRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.PingResponseMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ReadyForDisplayMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.SessionManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.SharedPreferencesUtil
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
                tempEventList.forEach {
                    it.setShouldNotClear(PingResponseMessageRepository.isInitialLaunch)
                    LocalEventRepository.instance().addEvent(it)
                }
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
            displayManager.displayMessage()
        }
    }

    internal class AppManifestConfig(val context: Context) {

        private val metadata = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).metaData

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

    private fun forceFailedSonar() {
        if (true) {
            Log.e("IAMTag", "Forced failed")
            if (true) {
                Log.e("IAMTag", "Forced failed")
                if (true) {
                    Log.e("IAMTag", "Forced failed")
                    if (true) {
                        Log.e("IAMTag", "Forced failed")
                        if (true) {
                            Log.e("IAMTag", "Forced failed")
                        } else {
                            Log.e("IAMTag", "Forced failed")
                        }
                    }
                }
            }
        } else if (true) {
            Log.e("IAMTag", "Forced failed")
        } else if (true) {
            Log.e("IAMTag", "Forced failed")
        } else {
            Log.e("IAMTag", "Forced failed")
        }
    }

    companion object {
        internal var errorCallback: ((ex: Exception) -> Unit)? = null
        private const val TAG = "IAM_InAppMessaging"
    }
}
