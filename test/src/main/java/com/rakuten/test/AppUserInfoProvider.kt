package com.rakuten.test

import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider

class AppUserInfoProvider : UserInfoProvider {

    var userId = "user1"
    var raeToken = "token1"
    var rakutenId = "rakuten1"

    /**
     * If user is logged in, then return RAE token utilizing User SDK.
     */
    override fun provideRaeToken(): String? {
        return ""
    }

    /**
     * Returns the logged in userId utilizing User SDK.
     */
    override fun provideUserId(): String? {
        return userId
    }

    /**
     * Returns the logged in rakutenId utilizing any logging process.
     */
    override fun provideRakutenId(): String? {
        // Any value can be set as Rakuten ID.
        return rakutenId
    }
}