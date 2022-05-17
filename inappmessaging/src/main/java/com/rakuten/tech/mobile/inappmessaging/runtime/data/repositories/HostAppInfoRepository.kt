package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.ARGUMENT_IS_NULL_EXCEPTION
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.DEVICE_ID_IS_EMPTY_EXCEPTION
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.LOCALE_IS_EMPTY_EXCEPTION
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.PACKAGE_NAME_IS_EMPTY_EXCEPTION
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.SUBSCRIPTION_KEY_IS_EMPTY_EXCEPTION
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.VERSION_IS_EMPTY_EXCEPTION
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import java.util.Locale

/**
 * Host app information repository which stores information such as host app version, package name,
 * subscription key, etc.
 */
internal interface HostAppInfoRepository {
    /**
     * This method adds host information.
     */
    @Throws(InAppMessagingException::class)
    fun addHostInfo(hostAppInfo: HostAppInfo?)

    /**
     * This method returns host app's version or empty string if not set.
     */
    fun getVersion(): String

    /**
     * This method returns host app's package name or empty string if not set.
     */
    fun getPackageName(): String

    /**
     * This method returns device's locale or default if not set in String and lowercase format (i.e. xx_xx).
     */
    fun getDeviceLocale(): String

    /**
     * This method returns IAM subscription key.
     */
    @SuppressWarnings("FunctionMaxLength")
    fun getInAppMessagingSubscriptionKey(): String

    /**
     * This method returns Android device ID or empty String if not set.
     */
    fun getDeviceId(): String

    /**
     * This method returns IAM config url.
     */
    fun getConfigUrl(): String

    /**
     * Clears host app info for testing.
     */
    @VisibleForTesting
    fun clearInfo()

    companion object {
        private const val TAG = "IAM_HostAppRepository"
        private var instance: HostAppInfoRepository = HostAppInfoRepositoryImpl()

        fun instance() = instance
    }

    private class HostAppInfoRepositoryImpl : HostAppInfoRepository {
        @Volatile
        private var hostAppInfo: HostAppInfo? = null

        @SuppressWarnings("LongMethod", "FunctionMaxLength")
        @Throws(InAppMessagingException::class)
        override fun addHostInfo(hostAppInfo: HostAppInfo?) {
            if (hostAppInfo == null) {
                throw InAppMessagingException(ARGUMENT_IS_NULL_EXCEPTION)
            }
            synchronized(hostAppInfo) {
                var message = ""
                when {
                    hostAppInfo.version.isNullOrEmpty() -> message = VERSION_IS_EMPTY_EXCEPTION
                    hostAppInfo.packageName.isNullOrEmpty() -> message = PACKAGE_NAME_IS_EMPTY_EXCEPTION
                    hostAppInfo.subscriptionKey.isNullOrEmpty() -> message = SUBSCRIPTION_KEY_IS_EMPTY_EXCEPTION
                    hostAppInfo.locale == null ->
                        Logger(TAG).error(LOCALE_IS_EMPTY_EXCEPTION)
                    hostAppInfo.deviceId.isNullOrEmpty() ->
                        // Should continue initialization without device id.
                        Logger(TAG).error(DEVICE_ID_IS_EMPTY_EXCEPTION)
                }
                if (message.isNotEmpty()) {
                    throw InAppMessagingException(message)
                }
                this.hostAppInfo = hostAppInfo
            }
        }

        override fun getVersion(): String = hostAppInfo?.version.orEmpty()

        override fun getPackageName(): String = hostAppInfo?.packageName.orEmpty()

        override fun getDeviceLocale(): String {
            val locale = hostAppInfo?.locale ?: Locale.getDefault()
            return locale.toString().replace("_", "-").lowercase(Locale.getDefault())
        }

        @SuppressWarnings("FunctionMaxLength")
        override fun getInAppMessagingSubscriptionKey(): String = hostAppInfo?.subscriptionKey.orEmpty()

        override fun getDeviceId(): String = hostAppInfo?.deviceId.orEmpty()

        override fun getConfigUrl(): String = hostAppInfo?.configUrl.orEmpty()

        override fun clearInfo() {
            hostAppInfo = null
        }
    }
}
