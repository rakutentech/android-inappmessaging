package com.rakuten.test

import android.app.Application
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging

class MainApplication : Application() {

    val provider = AppUserInfoProvider()
    lateinit var settings: IAMSettings

    override fun onCreate() {
        super.onCreate()
        settings = IAMSettings(this)
//        InAppMessaging.configure(this)
        InAppMessaging.configure(this, configUrl = "http://127.0.0.1:6789/iam/")
        InAppMessaging.instance().registerPreference(provider)
    }
}