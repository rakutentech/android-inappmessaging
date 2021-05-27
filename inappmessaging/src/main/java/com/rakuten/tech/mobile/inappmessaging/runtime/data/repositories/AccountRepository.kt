package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider
import java.math.BigInteger
import java.security.MessageDigest

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
     * This method updates the encrypted value using the current user information.
     * @return true if there are changes in user info.
     */
    abstract fun updateUserInfo(): Boolean

    companion object {
        private const val TOKEN_PREFIX = "OAuth2 "

        private var instance: AccountRepository = AccountRepositoryImpl()

        fun instance() = instance
    }

    private class AccountRepositoryImpl : AccountRepository() {

        override fun getRaeToken(): String = if (this.userInfoProvider == null ||
                this.userInfoProvider?.provideRaeToken() == null ||
                this.userInfoProvider?.provideRaeToken()!!.isEmpty()) {

            ""
        } else TOKEN_PREFIX + this.userInfoProvider?.provideRaeToken()
        // According to backend specs, token has to start with "OAuth2{space}", followed by real token.

        override fun getUserId(): String =
                if (this.userInfoProvider == null || this.userInfoProvider?.provideUserId() == null) {
                    ""
                } else userInfoProvider!!.provideUserId().toString()

        override fun getRakutenId(): String =
                if (this.userInfoProvider == null || this.userInfoProvider?.provideRakutenId() == null) {
                    ""
                } else userInfoProvider!!.provideRakutenId().toString()

        override fun updateUserInfo(): Boolean {
            val curr = hash(getUserId() + getRakutenId())

            if (userInfoHash != curr) {
                userInfoHash = curr
                return true
            }

            return false
        }

        @Suppress("MagicNumber")
        private fun hash(input: String): String {
            // MD5 hashing
            val bytes = MessageDigest
                    .getInstance("MD5")
                    .digest(input.toByteArray())

            return BigInteger(1, bytes).toString(16).padStart(32, '0')
        }
    }
}
