package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.annotation.SuppressLint
import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import java.math.BigInteger
import java.security.MessageDigest

/**
 * This object contains userInfoProvider ID used when logging in, and Access token.
 * Note: Never persist account information without encrypting first.
 */
internal abstract class AccountRepository {
    var userInfoProvider: UserInfoProvider? = null

    @get:Synchronized @set:Synchronized
    internal var userInfoHash = ""

    /**
     * This method returns access token, or empty String.
     */
    abstract fun getAccessToken(): String

    /**
     * This method returns User ID, or empty String.
     */
    abstract fun getUserId(): String

    /**
     * This method returns ID tracking identifier, or empty String.
     */
    abstract fun getIdTrackingIdentifier(): String

    /**
     * This method updates the encrypted value using the current user information.
     * @return true if there are changes in user info.
     */
    abstract fun updateUserInfo(algo: String? = null): Boolean

    abstract fun logWarningForUserInfo(tag: String, logger: Logger = Logger(tag))

    companion object {
        private const val TOKEN_PREFIX = "OAuth2 "
        internal const val ID_TRACKING_ERR_MSG = "Both an access token and a user tracking id have been set. " +
            "Only one of these id types is expected to be set at the same time"
        internal const val TOKEN_USER_ERR_MSG = "User Id must be present and not empty when access token is specified"

        private var instance: AccountRepository = AccountRepositoryImpl()

        fun instance() = instance
    }

    private class AccountRepositoryImpl : AccountRepository() {

        override fun getAccessToken() = if (this.userInfoProvider == null ||
            this.userInfoProvider?.provideAccessToken().isNullOrEmpty()
        ) {
            ""
        } else TOKEN_PREFIX + this.userInfoProvider?.provideAccessToken()
        // According to backend specs, token has to start with "OAuth2{space}", followed by real token.

        override fun getUserId() = this.userInfoProvider?.provideUserId().orEmpty()

        override fun getIdTrackingIdentifier() = this.userInfoProvider?.provideIdTrackingIdentifier().orEmpty()

        override fun updateUserInfo(algo: String?): Boolean {
            val curr = hash(getUserId() + getIdTrackingIdentifier(), algo)

            if (userInfoHash != curr) {
                userInfoHash = curr
                clearUserOldCacheStructure()
                return true
            }

            return false
        }

        @SuppressLint("BinaryOperationInTimber")
        override fun logWarningForUserInfo(tag: String, logger: Logger) {
            if (getAccessToken().isNotEmpty()) {
                if (getIdTrackingIdentifier().isNotEmpty()) {
                    logger.warn(ID_TRACKING_ERR_MSG)
                    if (BuildConfig.DEBUG) {
                        error(ID_TRACKING_ERR_MSG)
                    }
                }
                if (getUserId().isEmpty()) {
                    logger.warn(TOKEN_USER_ERR_MSG)
                    if (BuildConfig.DEBUG) {
                        error(TOKEN_USER_ERR_MSG)
                    }
                }
            }
        }

        @SuppressWarnings("MagicNumber", "SwallowedException", "TooGenericExceptionCaught")
        private fun hash(input: String, algo: String?): String {
            return try {
                // MD5 hashing
                val bytes = MessageDigest
                    .getInstance(algo ?: "MD5")
                    .digest(input.toByteArray())

                BigInteger(1, bytes).toString(16).padStart(32, '0')
            } catch (ex: Exception) {
                // should never happen since "MD5" is a supported algorithm
                input
            }
        }

        /**
         * From v7.1.0, the structure of cached data is changed.
         * Remove old data since it will never be used.
         */
        private fun clearUserOldCacheStructure() {
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                clearOldCacheByKey("ping_response_list")
                clearOldCacheByKey("local_event_list")
                clearOldCacheByKey("ready_display_list")
                clearOldCacheByKey("local_displayed_list")
            }
        }

        private fun clearOldCacheByKey(key: String) {
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                val prefs = InAppMessaging.getPreferencesFile()
                InAppMessaging.instance().getHostAppContext()?.let { ctx ->
                    if (PreferencesUtil.contains(ctx, prefs, key)) {
                        PreferencesUtil.remove(ctx, prefs, key)
                    }
                }
            }
        }
    }
}
