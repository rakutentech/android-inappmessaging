package com.rakuten.tech.mobile.inappmessaging.runtime

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.RestrictTo
import androidx.work.Configuration
import androidx.work.WorkManager
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalDisplayedMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.PingResponseMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingInitializationException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.Initializer
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.ConfigScheduler
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * Main entry point for the IAM SDK.
 * Should be accessed via [InAppMessaging.instance].
 */
@Suppress("UnnecessaryAbstractClass", "TooManyFunctions")
abstract class InAppMessaging internal constructor() {
    /**
     * This callback is called just before showing a message of campaign that has registered contexts.
     * Return `false` to prevent the message from displaying.
     */
    abstract var onVerifyContext: (contexts: List<String>, campaignTitle: String) -> Boolean

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
    @Deprecated("This method is no longer needs to be called when updating user info because session updates" +
            "are handled internally by the sdk.")
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

    /**
     * This method returns flag if local caching feature is enabled.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    internal abstract fun isLocalCachingEnabled(): Boolean

    /**
     * This method returns the encrypted shared preference.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    internal abstract fun getSharedPref(): SharedPreferences?

    /**
     * This method moves temp data to persistent cache.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    internal abstract fun saveTempData()

    /**
     * Close the currently displayed message.
     * This should be called when app needs to force-close the displayed message without user action.
     * Calling this method will not increment the campaign impression.
     * @param clearQueuedCampaigns An optional parameter, when set to true (false by default), will additionally
     * remove all campaigns that were queued to be displayed.
     */
    abstract fun closeMessage(clearQueuedCampaigns: Boolean = false)

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
         * Initializes the In-App Messaging SDK. [errorCallback] is an optional callback function for
         * app to receive the exception that caused failed init.
         *
         * @return `true` if initialization is successful, and `false` otherwise.
         */
        @SuppressWarnings("TooGenericExceptionCaught")
        fun init(context: Context, errorCallback: ((ex: Exception) -> Unit)? = null): Boolean {
            InApp.errorCallback = errorCallback
            return try {
                initialize(context, isCacheHandling = BuildConfig.IS_CACHE_HANDLING)
                true
            } catch (ex: Exception) {
                errorCallback?.let {
                    it(InAppMessagingInitializationException("In-App Messaging initialization failed", ex.cause))
                }
                false
            }
        }

        /**
         * [isDebugLogging] is used to enable/disable the debug logging of InAppMessaging SDK.
         * Debug logging is disabled by default.
         * Note: All InAppMessaging SDK logs' tags begins with "IAM_".
         */
        @Throws(InAppMessagingInitializationException::class)
        internal fun initialize(
            context: Context,
            isForTesting: Boolean = false,
            isCacheHandling: Boolean = false,
            configScheduler: ConfigScheduler = ConfigScheduler.instance()
        ) {
            val manifestConfig = AppManifestConfig(context)

            // Special handling of WorkManager initialization for Android 11
            val config = Configuration.Builder().build()
            WorkManager.initialize(context, config)

            instance = InApp(context, manifestConfig.isDebugging(), isCacheHandling = isCacheHandling)

            // Initializing SDK using background worker thread.
            Initializer.initializeSdk(context, manifestConfig.subscriptionKey(), manifestConfig.configUrl(),
                    isForTesting)

            // inform ping response repository that it is initial launch to display app launch campaign at least once
            PingResponseMessageRepository.isInitialLaunch = true
            LocalDisplayedMessageRepository.isInitialLaunch = true

            configScheduler.startConfig()
        }

        internal fun setUninitializedInstance() {
            instance = NotInitializedInAppMessaging()
        }
    }

    @Suppress("EmptyFunctionBlock", "TooManyFunctions")
    internal class NotInitializedInAppMessaging : InAppMessaging() {
        override var onVerifyContext: (contexts: List<String>, campaignTitle: String) -> Boolean = { _, _ -> true }

        override fun registerPreference(userInfoProvider: UserInfoProvider) {}

        override fun registerMessageDisplayActivity(activity: Activity) {}

        @Suppress("FunctionMaxLength")
        override fun unregisterMessageDisplayActivity() {}

        override fun logEvent(event: Event) {}

        override fun updateSession() {}

        override fun getRegisteredActivity(): Activity? = null

        override fun getHostAppContext(): Context? = null

        override fun isLocalCachingEnabled() = false

        override fun getSharedPref(): SharedPreferences? = null

        override fun closeMessage(clearQueuedCampaigns: Boolean) {}

        override fun saveTempData() {}
    }
}
