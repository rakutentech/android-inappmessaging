package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingInitializationException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.ARGUMENT_IS_NULL_EXCEPTION
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.DEVICE_ID_IS_EMPTY_EXCEPTION
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.LOCALE_IS_EMPTY_EXCEPTION
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.PACKAGE_NAME_IS_EMPTY_EXCEPTION
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.SUBSCRIPTION_KEY_IS_EMPTY_EXCEPTION
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.VERSION_IS_EMPTY_EXCEPTION
import timber.log.Timber
import java.util.Locale

/**
 * Host app information repository which stores information such as host app version, package name,
 * subscription key, etc.
 */
internal interface HostAppInfoRepository {
    /**
     * This method adds host information.
     */
    @Throws(InAppMessagingInitializationException::class)
    fun addHostInfo(hostAppInfo: HostAppInfo?)

    /**
     * This method returns host app's version or empty string if not set.
     */
    fun getVersion(): String?

    /**
     * This method returns host app's package name or empty string if not set.
     */
    fun getPackageName(): String?

    /**
     * This method returns device's locale or null if not set.
     */
    fun getDeviceLocale(): Locale?

    /**
     * This method returns IAM subscription key.
     */
    @Suppress("FunctionMaxLength")
    fun getInAppMessagingSubscriptionKey(): String?

    /**
     * This method returns Android device ID or empty String if not set.
     */
    fun getDeviceId(): String?

    /**
     * This method returns IAM config url.
     */
    fun getConfigUrl(): String?

    companion object {
        private const val TAG = "IAM_HostAppRepository"
        private var instance: HostAppInfoRepository = HostAppInfoRepositoryImpl()

        fun instance() = instance
    }

    private class HostAppInfoRepositoryImpl : HostAppInfoRepository {
        @SuppressWarnings("PMD")
        @Volatile
        private var hostAppInfo: HostAppInfo? = null

        @Suppress("LongMethod", "FunctionMaxLength")
        @Throws(InAppMessagingInitializationException::class)
        override fun addHostInfo(hostAppInfo: HostAppInfo?) {
            if (hostAppInfo == null) {
                throw InAppMessagingInitializationException(ARGUMENT_IS_NULL_EXCEPTION)
            }
            synchronized(hostAppInfo) {
                var message = ""
                when {
                    hostAppInfo.version.isNullOrEmpty() -> message = VERSION_IS_EMPTY_EXCEPTION
                    hostAppInfo.packageName.isNullOrEmpty() -> message = PACKAGE_NAME_IS_EMPTY_EXCEPTION
                    hostAppInfo.subscriptionKey.isNullOrEmpty() -> message = SUBSCRIPTION_KEY_IS_EMPTY_EXCEPTION
                    hostAppInfo.locale == null ->
                        Timber.tag(TAG).e(LOCALE_IS_EMPTY_EXCEPTION)
                    hostAppInfo.deviceId.isNullOrEmpty() ->
                        // Should continue initialization without device id.
                        Timber.tag(TAG).e(DEVICE_ID_IS_EMPTY_EXCEPTION)
                }
                if (message.isNotEmpty()) {
                    throw InAppMessagingInitializationException(message)
                }
                this.hostAppInfo = hostAppInfo
            }
        }

        override fun getVersion(): String? = hostAppInfo?.version ?: ""

        override fun getPackageName(): String? = hostAppInfo?.packageName ?: ""

        override fun getDeviceLocale(): Locale? = hostAppInfo?.locale ?: Locale.getDefault()

        @Suppress("FunctionMaxLength")
        override fun getInAppMessagingSubscriptionKey(): String? = hostAppInfo?.subscriptionKey ?: ""

        override fun getDeviceId(): String? = hostAppInfo?.deviceId ?: ""

        override fun getConfigUrl(): String? = hostAppInfo?.configUrl ?: ""
    }
}
