package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.WorkerThread
import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider

/**
 * This object contains userInfoProvider ID used when logging in, and RAE token.
 * Note: Never persist account information without encrypting first.
 */
internal abstract class AccountRepository {
    var userInfoProvider: UserInfoProvider? = null

    /**
     * This method returns RAE token, or empty String.
     */
    @WorkerThread
    abstract fun getRaeToken(): String

    /**
     * This method returns User ID, or empty String.
     */
    @WorkerThread
    abstract fun getUserId(): String

    companion object {
        private const val TOKEN_PREFIX = "OAuth2 "

        private var instance: AccountRepository = AccountRepositoryImpl()

        fun instance() = instance
    }

    private class AccountRepositoryImpl : AccountRepository() {

        @WorkerThread
        override fun getRaeToken(): String = if (this.userInfoProvider == null ||
                this.userInfoProvider?.provideRaeToken() == null ||
                this.userInfoProvider?.provideRaeToken()!!.isEmpty()) {

            ""
        } else TOKEN_PREFIX + this.userInfoProvider?.provideRaeToken()
        // According to backend specs, token has to start with "OAuth2{space}", followed by real token.

        @WorkerThread
        override fun getUserId(): String =
                if (this.userInfoProvider == null || this.userInfoProvider?.provideUserId() == null) {
                    ""
                } else userInfoProvider!!.provideUserId().toString()
    }
}
