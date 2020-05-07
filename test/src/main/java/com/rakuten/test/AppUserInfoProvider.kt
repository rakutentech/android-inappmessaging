package com.rakuten.test

import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider

class AppUserInfoProvider : UserInfoProvider {

    /**
     * If user is logged in, then return RAE token utilizing User SDK.
     */
    override fun provideRaeToken(): String? {
        // Uncomment this code if your app uses User SDK.
        // if (LoginManager.getInstance().isLoggedIn()) {
        //    return LoginManager.getInstance().getTokenCache().getToken("token_id").getToken();
        // }
        // RAE Prod token for daniel.a.tam@rakuten.com. Will expire.
            return ""
    }

    /**
     * Returns the logged in userId utilizing User SDK.
     */
    override fun provideUserId(): String? {
        // Uncomment this code if your app uses User SDK.
        // if (LoginManager.getInstance().isLoggedIn()) {
        //   return LoginManager.getInstance().getLoginService().getUserId();
        // }
        return ""
    }

    /**
     * Returns the logged in rakutenId utilizing any logging process.
     */
    override fun provideRakutenId(): String? {
        // Any value can be set as Rakuten ID.
        return ""
    }
}