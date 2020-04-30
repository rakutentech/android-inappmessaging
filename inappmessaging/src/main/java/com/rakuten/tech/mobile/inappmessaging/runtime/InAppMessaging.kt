package com.rakuten.tech.mobile.inappmessaging.runtime

import android.app.Activity
import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingInitializationException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.Initializer
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.ConfigScheduler
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * Main entry point for the IAM SDK.
 * Should be accessed via [InAppMessaging.instance].
 */
@Suppress("UnnecessaryAbstractClass")
abstract class InAppMessaging internal constructor() {
    /**
     * This method registers provider containing user information [userInfoProvider], like RAE Token and Uer ID.
     */
    abstract fun registerPreference(@NotNull userInfoProvider: UserInfoProvider)

    /**
     * This method registers [activity] where message can be displayed
     * This method should be called in onResume() of the activity to register.
     * In order for InAppMessaging SDK to display messages, host app must pass an Activity
     * which the host app allows the SDK to display any Messages.
     */
    @Throws(IllegalArgumentException::class)
    abstract fun registerMessageDisplayActivity(@NotNull activity: Activity)

    /**
     * This method unregisters the activity from InAppMessaging
     * This method should be called in onPause() of the registered activity in order to avoid memory leaks.
     * If there is message being displayed, it will be closed automatically.
     */
    @Suppress("FunctionMaxLength")
    abstract fun unregisterMessageDisplayActivity()

    /**
     * This methods logs the [event] which the InAppMessaging SDK checks to know the messages'
     * triggers are satisfied, then display that message if all trigger conditions are satisfied.
     */
    @Throws(IllegalArgumentException::class, NullPointerException::class)
    abstract fun logEvent(@NotNull event: Event)

    /**
     * This methods updates the host app's session. This allows InAppMessaging to update the locally stored
     * messages which can be dependent on user information.
     */
    abstract fun updateSession()

    /**
     * This method returns registered activity of the host app.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @Nullable
    internal abstract fun getRegisteredActivity(): Activity?

    /**
     * This method returns application context of the host app.
     */
    @Nullable
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    internal abstract fun getHostAppContext(): Context?

    companion object {
        private var instance: InAppMessaging = NotInitializedInAppMessaging()

        /**
         * Instance of [InAppMessaging].
         *
         * @return [InAppMessaging] instance
         */
        @JvmStatic
        fun instance(): InAppMessaging = instance

        /**
         * [isDebugLogging] is used to enable/disable the debug logging of InAppMessaging SDK.
         * Debug logging is disabled by default.
         * Note: All InAppMessaging SDK logs' tags begins with "IAM_".
         */
        @Suppress("LongParameterList")
        @Throws(InAppMessagingInitializationException::class)
        internal fun init(
            context: Context,
            subscriptionKey: String?,
            configUrl: String?,
            isDebugLogging: Boolean = false,
            isForTesting: Boolean = false,
            configScheduler: ConfigScheduler = ConfigScheduler.instance()
        ) {
            instance = InApp(context, isDebugLogging)
            // Initializing SDK using background worker thread.
            Initializer.initializeSdk(context, subscriptionKey, configUrl, isForTesting)
            configScheduler.startConfig()
        }

        @VisibleForTesting
        internal fun setUninitializedInstance() {
            instance = NotInitializedInAppMessaging()
        }
    }

    @Suppress("EmptyFunctionBlock")
    internal class NotInitializedInAppMessaging : InAppMessaging() {
        override fun registerPreference(userInfoProvider: UserInfoProvider) {}

        override fun registerMessageDisplayActivity(activity: Activity) {}

        @Suppress("FunctionMaxLength")
        override fun unregisterMessageDisplayActivity() {}

        override fun logEvent(event: Event) {}

        override fun updateSession() {}

        override fun getRegisteredActivity(): Activity? = null

        override fun getHostAppContext(): Context? = null
    }
}
