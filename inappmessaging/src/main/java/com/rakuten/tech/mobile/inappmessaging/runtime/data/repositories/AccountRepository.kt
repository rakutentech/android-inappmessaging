package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.annotation.SuppressLint
import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
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

    abstract fun logWarningForUserInfo(tag: String, logger: InAppLogger = InAppLogger(tag))

    /**
     * Checks whether user cache uses old cache structure (structure until v7.1.0) and clears it since it will no
     * longer be used.
     */
    abstract fun clearUserOldCacheStructure()

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
            val newHash = hash(getUserId() + getIdTrackingIdentifier(), algo)
            if (userInfoHash.isEmpty()) {
                userInfoHash = newHash
                return true
            }
            val currentHash = userInfoHash
            userInfoHash = newHash
            return currentHash != userInfoHash
        }

        @SuppressLint("BinaryOperationInTimber")
        override fun logWarningForUserInfo(tag: String, logger: InAppLogger) {
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

        override fun clearUserOldCacheStructure() {
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                InAppMessaging.instance().getHostAppContext()?.let { ctx ->
                    val prefs = InAppMessaging.getPreferencesFile()
                    // Clear if using old cache structure
                    if (!PreferencesUtil.contains(ctx, prefs, CampaignRepository.IAM_USER_CACHE)) {
                        PreferencesUtil.clear(ctx, prefs)
                    }
                }
            }
        }

        @SuppressWarnings("TooGenericExceptionCaught")
        private fun hash(input: String, algo: String?): String {
            return try {
                // MD5 hashing
                val bytes = MessageDigest
                    .getInstance(algo ?: "MD5")
                    .digest(input.toByteArray())

                BigInteger(1, bytes).toString(RADIX).padStart(PAD_LENGTH, '0')
            } catch (ex: Exception) {
                // should never happen since "MD5" is a supported algorithm
                InAppLogger(TAG).debug(ex.message)
                input
            }
        }

        companion object {
            private const val TAG = "AccountRepository"
            private const val RADIX = 16
            private const val PAD_LENGTH = 32
        }
    }
}
