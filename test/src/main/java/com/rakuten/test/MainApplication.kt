package com.rakuten.test

import android.app.Application
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        InAppMessaging.instance().registerPreference(AppUserInfoProvider())
    }
}