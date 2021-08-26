package com.rakuten.test

import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider

class AppUserInfoProvider : UserInfoProvider {

    var userId = "user1"
    var raeToken = "token1"
    var rakutenId = "rakuten1"
    var idTracking = "tracking1"
    
    override fun provideRaeToken() = ""

    override fun provideUserId() = userId

    override fun provideRakutenId() = rakutenId

    override fun provideIdTrackingIdentifier() = idTracking
}