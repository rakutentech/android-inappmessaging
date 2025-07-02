package com.rakuten.tech.mobile.inappmessaging.runtime

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull
import androidx.annotation.RestrictTo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.EventLoggerConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.InAppEventLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.Event as ELEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.SdkApi
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.Initializer
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.ConfigScheduler

/**
 * Main entry point for the IAM SDK.
 * Should be accessed via [InAppMessaging.instance].
 */
@SuppressWarnings("TooManyFunctions")
abstract class InAppMessaging internal constructor() {
    /**
     * This callback is called just before showing a message of campaign that has registered contexts.
     * Return `false` to prevent the message from displaying.
     */
    abstract var onVerifyContext: (contexts: List<String>, campaignTitle: String) -> Boolean

    /**
     * This callback is called if a push primer button is tapped. If not set, SDK will request push permission.
     */
    abstract var onPushPrimer: (() -> Unit)?

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
     * This method returns flag if local caching feature is enabled.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    internal abstract fun isLocalCachingEnabled(): Boolean

    /**
     * Close the currently displayed message.
     * This should be called when app needs to force-close the displayed message without user action.
     * Calling this method will not increment the campaign impression.
     * @param clearQueuedCampaigns An optional parameter, when set to true (false by default), will additionally
     * remove all campaigns that were queued to be displayed.
     */
    abstract fun closeMessage(clearQueuedCampaigns: Boolean = false)

    /**
     * Closes a tooltip by `viewId` (`UIElement` identifier).
     * This should be called when app needs to force-close the displayed tooltip without user action.
     * Calling this method will not increment the campaign impression.
     * @param viewId The ID of UI element where the tooltip is attached.
     */
    abstract fun closeTooltip(viewId: String)

    /**
     * Tracks if user grants or denies the push notification via push primer message.
     * This API only works for Android 13 and up devices.
     *
     * @param: [permissions] requested list.
     * @param: [grantResults] permission granted/denied results.
     */
    abstract fun trackPushPrimer(permissions: Array<String>, grantResults: IntArray)

    companion object {

        private const val TAG = "IAM_InAppMessaging"

        /**
         * This is the request code that will be used when requesting push permission.
         */
        const val PUSH_PRIMER_REQ_CODE = 999

        /**
         * This optional callback function is for app to receive the exception that caused failed configuration
         * or non-fatal failures in the SDK.
         */
        var errorCallback: ((ex: Exception) -> Unit)? = null

        private var eventLoggerConfig: EventLoggerConfig? = null

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
         * @param context Context object.
         * @param subscriptionKey An optional subscription key. Default is the value set in your app's AndroidManifest.
         * @param configUrl An optional config URL. Default is the value set in your app's AndroidManifest.
         * @param enableTooltipFeature An optional flag to en/dis-able tooltip campaigns feature. Disabled by default.
         *
         * @return `true` if configuration is successful, and `false` otherwise.
         */
        @SuppressWarnings(
            "LongMethod",
            "TooGenericExceptionCaught",
        )
        @JvmOverloads
        fun configure(
            context: Context,
            subscriptionKey: String? = null,
            configUrl: String? = null,
            enableTooltipFeature: Boolean? = false,
        ): Boolean {
            return try {
                InAppEventLogger.configure(context, eventLoggerConfig)

                if (!shouldProcess(subscriptionKey)) {
                    InAppLogger(TAG).info("configure called but using RMC, skipping")
                    return false
                }

                initialize(
                    context = context,
                    isCacheHandling = BuildConfig.IS_CACHE_HANDLING,
                    subscriptionKey = if (RmcHelper.isRmcIntegrated()) {
                        subscriptionKey?.removeSuffix(RmcHelper.RMC_SUFFIX)
                    } else {
                        subscriptionKey
                    },
                    configUrl = configUrl,
                    enableTooltipFeature = enableTooltipFeature,
                )
                true
            } catch (ex: Exception) {
                // reset instance when configuration failed
                setNotConfiguredInstance()
                "In-App Messaging configuration failed".let {
                    InAppErrorLogger.logError(
                        TAG,
                        InAppError(
                            it, InAppMessagingException(it, ex),
                            ELEvent.OperationFailed(SdkApi.CONFIG.name),
                        ),
                    )
                }
                false
            }
        }

        /**
         * Configures the Event Logger platform for sending SDK critical or warning events.
         *
         * @param apiUrl An optional EventLogger API URL. Default is the value set in the app's AndroidManifest.
         * @param apiKey An optional EventLogger API Key. Default is the value set in the app's AndroidManifest.
         * @param enableEventLogger An optional flag to en/dis-able capturing events. Enabled by default.
         *
         * This method is intended for internal use only, and clients should not call this method directly.
         */
        @JvmOverloads
        fun setEventLoggerConfig(apiUrl: String, apiKey: String, enableEventLogger: Boolean = true) {
            this.eventLoggerConfig = EventLoggerConfig(apiUrl, apiKey, enableEventLogger)
        }

        @SuppressWarnings("LongParameterList")
        @Throws(InAppMessagingException::class)
        internal fun initialize(
            context: Context,
            isCacheHandling: Boolean = false,
            subscriptionKey: String? = null,
            configUrl: String? = null,
            enableTooltipFeature: Boolean? = false,
            configScheduler: ConfigScheduler = ConfigScheduler.instance(),
        ) {
            val manifestConfig = InApp.AppManifestConfig(context)

            // `manifestConfig.isDebugging()` is used to enable/disable the debug logging of InAppMessaging SDK.
            // Note: All InAppMessaging SDK logs' tags begins with "IAM_".
            if (instance is NotConfiguredInAppMessaging) {
                instance = InApp(
                    isDebugLogging = manifestConfig.isDebugging(),
                    isCacheHandling = isCacheHandling,
                )
            }

            val subsKeyTrim = subscriptionKey?.trim()
            val configUrlTrim = configUrl?.trim()
            Initializer.initializeSdk(
                context = context,
                subscriptionKey = if (!subsKeyTrim.isNullOrEmpty()) subsKeyTrim else manifestConfig.subscriptionKey(),
                configUrl = if (!configUrlTrim.isNullOrEmpty()) configUrlTrim else manifestConfig.configUrl(),
                enableTooltipFeature = enableTooltipFeature,
            )

            configScheduler.startConfig()
        }

        internal fun setNotConfiguredInstance(isCacheHandling: Boolean = false) {
            instance = NotConfiguredInAppMessaging(isCacheHandling)
        }

        internal fun getPreferencesFile() = "internal_shared_prefs_" + AccountRepository.instance().userInfoHash

        /**
         * Checks whether to process configure API call or not.
         *
         * This assumes that when configure API is called from RMC SDK, it appended the [RmcHelper.RMC_SUFFIX] in the
         * subscriptionKey value.
         *
         * @return false when RMC SDK is integrated but the API call is not from RMC SDK.
         */
        private fun shouldProcess(subscriptionKey: String?): Boolean {
            if (!RmcHelper.isRmcIntegrated()) {
                return true
            }

            return subscriptionKey != null && subscriptionKey.endsWith(RmcHelper.RMC_SUFFIX)
        }
    }

    @SuppressWarnings("TooManyFunctions")
    internal class NotConfiguredInAppMessaging(private var isCacheHandling: Boolean = false) : InAppMessaging() {
        override var onVerifyContext: (contexts: List<String>, campaignTitle: String) -> Boolean = { _, _ -> true }

        override var onPushPrimer: (() -> Unit)? = null

        override fun registerPreference(userInfoProvider: UserInfoProvider) = Unit

        override fun registerMessageDisplayActivity(activity: Activity) = Unit

        @SuppressWarnings("FunctionMaxLength")
        override fun unregisterMessageDisplayActivity() = Unit

        override fun logEvent(event: Event) = Unit

        override fun isLocalCachingEnabled() = isCacheHandling

        override fun closeMessage(clearQueuedCampaigns: Boolean) = Unit

        override fun closeTooltip(viewId: String) = Unit

        override fun trackPushPrimer(permissions: Array<String>, grantResults: IntArray) = Unit
    }
}
