package com.rakuten.tech.mobile.test_compose

import android.app.Application
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging

class MainApplication : Application() {

    val provider = AppUserInfoProvider()
    lateinit var settings: IAMSettings

    override fun onCreate() {
        super.onCreate()
        settings = IAMSettings(this)
        InAppMessaging.configure(this)
        InAppMessaging.instance().registerPreference(provider)
    }
}