package com.rakuten.tech.mobile.inappmessaging.runtime

import android.app.Activity
import android.content.Context
import androidx.annotation.RestrictTo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import timber.log.Timber
import java.lang.ref.WeakReference

internal class InApp(
    private val context: Context,
    isDebugLogging: Boolean,
    private val displayManager: DisplayManager = DisplayManager.instance()
) : InAppMessaging() {

    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // No need to initialize.
    // Used for displaying or removing messages from screen.
    private var activityWeakReference: WeakReference<Activity>? = null

    init {
        if (isDebugLogging) {
            // Start logging for debug builds.
            Timber.plant(Timber.DebugTree())
        }

        LegacyEventBroadcasterHelper.setContext(context)
    }

    // ------------------------------------Public APIs-----------------------------------------------
    override fun registerPreference(userInfoProvider: UserInfoProvider) {
        AccountRepository.instance().userInfoProvider = userInfoProvider
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
        activityWeakReference?.clear()

        Timber.tag(TAG)
        Timber.d("unregisterMessageDisplayActivity()")
    }

    override fun logEvent(event: Event) = EventsManager.onEventReceived(event)

    // ------------------------------------Library Internal APIs-------------------------------------
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun getRegisteredActivity(): Activity? =
            if (activityWeakReference != null) activityWeakReference!!.get() else null

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun getHostAppContext(): Context? = context

    companion object {
        private const val TAG = "IAM_InAppMessaging"
    }
}
