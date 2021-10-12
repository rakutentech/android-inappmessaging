package com.rakuten.test

import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider

class AppUserInfoProvider : UserInfoProvider {

    var userId = "user1"
    var accessToken = "token1"
    var idTracking = "tracking1"
    
    override fun provideAccessToken() = ""

    override fun provideUserId() = userId

    override fun provideIdTrackingIdentifier() = idTracking
}