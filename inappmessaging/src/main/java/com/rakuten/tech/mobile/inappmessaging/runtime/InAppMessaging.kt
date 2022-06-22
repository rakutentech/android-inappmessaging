package com.rakuten.tech.mobile.inappmessaging.runtime

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RestrictTo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.PingResponseMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.Initializer
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.ConfigScheduler

/**
 * Main entry point for the IAM SDK.
 * Should be accessed via [InAppMessaging.instance].
 */
@SuppressWarnings("UnnecessaryAbstractClass", "TooManyFunctions")
abstract class InAppMessaging internal constructor() {
    /**
     * This callback is called just before showing a message of campaign that has registered contexts.
     * Return `false` to prevent the message from displaying.
     */
    abstract var onVerifyContext: (contexts: List<String>, campaignTitle: String) -> Boolean

    /**
     * This method registers provider containing user information [userInfoProvider], like Access Token and User ID.
     */
    abstract fun registerPreference(@NonNull userInfoProvider: UserInfoProvider)

    /**
     * This method registers [activity] where message can be displayed
     * This method should be called in onResume() of the activity to register.
     * In order for InAppMessaging SDK to display messages, host app must pass an Activity
     * which the host app allows the SDK to display any Messages.
     */
    abstract fun registerMessageDisplayActivity(@NonNull activity: Activity)

    /**
     * This method unregisters the activity from InAppMessaging
     * This method should be called in onPause() of the registered activity in order to avoid memory leaks.
     * If there is message being displayed, it will be closed automatically.
     */
    @SuppressWarnings("FunctionMaxLength")
    abstract fun unregisterMessageDisplayActivity()

    /**
     * This methods logs the [event] which the InAppMessaging SDK checks to know the messages'
     * triggers are satisfied, then display that message if all trigger conditions are satisfied.
     */
    abstract fun logEvent(@NonNull event: Event)

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
        /**
         * This optional callback function is for app to receive the exception that caused failed configuration
         * or non-fatal failures in the SDK.
         */
        var errorCallback: ((ex: Exception) -> Unit)? = null

        private var instance: InAppMessaging = NotConfiguredInAppMessaging()

        /**
         * Instance of [InAppMessaging].
         *
         * @return [InAppMessaging] instance
         */
        @JvmStatic
        fun instance(): InAppMessaging = instance

        /**
         * Configures the In-App Messaging SDK.
         *
         * @return `true` if configuration is successful, and `false` otherwise.
         */
        @SuppressWarnings("TooGenericExceptionCaught")
        fun configure(context: Context): Boolean {
            return try {
                initialize(context, isCacheHandling = BuildConfig.IS_CACHE_HANDLING)
                true
            } catch (ex: Exception) {
                // reset instance when configuration failed
                setNotConfiguredInstance()
                errorCallback?.let {
                    it(InAppMessagingException("In-App Messaging configuration failed", ex))
                }
                false
            }
        }

        @Throws(InAppMessagingException::class)
        @SuppressWarnings("MagicNumber")
        internal fun initialize(
            context: Context,
            isCacheHandling: Boolean = false,
            configScheduler: ConfigScheduler = ConfigScheduler.instance()
        ) {
            val manifestConfig = InApp.AppManifestConfig(context)

            // startup delay test
            Thread.sleep(7000)

            // `manifestConfig.isDebugging()` is used to enable/disable the debug logging of InAppMessaging SDK.
            // Note: All InAppMessaging SDK logs' tags begins with "IAM_".
            instance = InApp(context, manifestConfig.isDebugging(), isCacheHandling = isCacheHandling)

            Initializer.initializeSdk(
                context = context,
                subscriptionKey = manifestConfig.subscriptionKey(),
                configUrl = manifestConfig.configUrl()
            )

            // inform repositories that it is initial launch to display app launch campaign at least once
            PingResponseMessageRepository.isInitialLaunch = true

            configScheduler.startConfig()
        }

        internal fun setNotConfiguredInstance(isCacheHandling: Boolean = false) {
            instance = NotConfiguredInAppMessaging(isCacheHandling)
        }

        internal fun getPreferencesFile() = "internal_shared_prefs_" + AccountRepository.instance().userInfoHash
    }

    @SuppressWarnings("EmptyFunctionBlock", "TooManyFunctions")
    internal class NotConfiguredInAppMessaging(private var isCacheHandling: Boolean = false) : InAppMessaging() {
        override var onVerifyContext: (contexts: List<String>, campaignTitle: String) -> Boolean = { _, _ -> true }

        override fun registerPreference(userInfoProvider: UserInfoProvider) = Unit

        override fun registerMessageDisplayActivity(activity: Activity) = Unit

        @SuppressWarnings("FunctionMaxLength")
        override fun unregisterMessageDisplayActivity() = Unit

        override fun logEvent(event: Event) = Unit

        override fun getRegisteredActivity(): Activity? = null

        override fun getHostAppContext(): Context? = null

        override fun isLocalCachingEnabled() = isCacheHandling

        override fun closeMessage(clearQueuedCampaigns: Boolean) = Unit

        override fun saveTempData() = Unit
    }
}
