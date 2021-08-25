package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.annotation.SuppressLint
import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider
import timber.log.Timber
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.Exception

/**
 * This object contains userInfoProvider ID used when logging in, and RAE token.
 * Note: Never persist account information without encrypting first.
 */
internal abstract class AccountRepository {
    var userInfoProvider: UserInfoProvider? = null

    @get:Synchronized @set:Synchronized
    internal var userInfoHash = ""

    /**
     * This method returns RAE token, or empty String.
     */
    abstract fun getRaeToken(): String

    /**
     * This method returns User ID, or empty String.
     */
    abstract fun getUserId(): String

    /**
     * This method returns Rakuten ID, or empty String.
     */
    abstract fun getRakutenId(): String

    /**
     * This method returns ID tracking identifier, or empty String.
     */
    abstract fun getIdTrackingIdentifier(): String

    /**
     * This method updates the encrypted value using the current user information.
     * @return true if there are changes in user info.
     */
    abstract fun updateUserInfo(algo: String? = null): Boolean

    abstract fun logWarningForUserInfo(tag: String, timber: Timber.Tree = Timber.tag(tag))

    companion object {
        private const val TOKEN_PREFIX = "OAuth2 "

        private var instance: AccountRepository = AccountRepositoryImpl()

        fun instance() = instance
    }

    private class AccountRepositoryImpl : AccountRepository() {

        override fun getRaeToken() = if (this.userInfoProvider == null ||
                this.userInfoProvider?.provideRaeToken().isNullOrEmpty()) {
            ""
        } else TOKEN_PREFIX + this.userInfoProvider?.provideRaeToken()
        // According to backend specs, token has to start with "OAuth2{space}", followed by real token.

        override fun getUserId() = this.userInfoProvider?.provideUserId() ?: ""

        override fun getRakutenId() = this.userInfoProvider?.provideRakutenId() ?: ""

        override fun getIdTrackingIdentifier() = this.userInfoProvider?.provideIdTrackingIdentifier() ?: ""

        override fun updateUserInfo(algo: String?): Boolean {
            val curr = hash(getUserId() + getRakutenId() + getIdTrackingIdentifier(), algo)

            if (userInfoHash != curr) {
                userInfoHash = curr
                return true
            }

            return false
        }

        @SuppressLint("BinaryOperationInTimber")
        override fun logWarningForUserInfo(tag: String, timber: Timber.Tree) {
            if (getRaeToken().isNotEmpty() && getIdTrackingIdentifier().isNotEmpty()) {
                timber.w("Both an RAE token and a user tracking id have been set. " +
                        "Only one of these id types is expected to be set at the same time")
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
    }
}
