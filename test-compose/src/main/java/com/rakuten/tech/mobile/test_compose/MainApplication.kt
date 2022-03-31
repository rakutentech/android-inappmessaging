package com.rakuten.tech.mobile.test_compose

import android.app.Application
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging

class MainApplication : Application() {

    val provider = AppUserInfoProvider()

    override fun onCreate() {
        super.onCreate()
        InAppMessaging.configure(this)
        InAppMessaging.instance().registerPreference(provider)
    }
}