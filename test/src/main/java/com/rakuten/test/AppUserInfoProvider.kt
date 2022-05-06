package com.rakuten.test

import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider

class AppUserInfoProvider : UserInfoProvider {

    var userId = ""
    var accessToken = ""
    var idTracking = ""
    
    override fun provideAccessToken() = accessToken

    override fun provideUserId() = userId

    override fun provideIdTrackingIdentifier() = idTracking
}