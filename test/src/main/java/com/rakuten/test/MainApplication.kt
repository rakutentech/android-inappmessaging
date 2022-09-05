package com.rakuten.test

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging

class MainApplication : Application() {

    val provider = AppUserInfoProvider()
    val settings = Settings()

    override fun onCreate() {
        super.onCreate()
        InAppMessaging.configure(this)
        InAppMessaging.instance().registerPreference(provider)
        getConfigurationMetadata(this)
    }

    private fun getConfigurationMetadata(context: Context) {
        val metadata = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).metaData
        settings.subscriptionKey = metadata.getString("com.rakuten.tech.mobile.inappmessaging.subscriptionkey") ?: ""
        settings.configUrl = metadata.getString("com.rakuten.tech.mobile.inappmessaging.configurl") ?: ""
    }
}