package com.rakuten.tech.mobile.test_compose

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging

class MainApplication : Application() {

    val provider = AppUserInfoProvider()
    // For display purposes only
    lateinit var realConfigUrl: String
    lateinit var realSubscriptionKey: String

    override fun onCreate() {
        super.onCreate()
        InAppMessaging.configure(this)
        InAppMessaging.instance().registerPreference(provider)
        getConfigurationMetadata(this)
    }

    private fun getConfigurationMetadata(context: Context) {
        val metadata = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).metaData
        realConfigUrl = metadata.getString("com.rakuten.tech.mobile.inappmessaging.configurl") ?: ""
        realSubscriptionKey = metadata.getString("com.rakuten.tech.mobile.inappmessaging.subscriptionkey") ?: ""
    }
}