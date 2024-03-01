package com.rakuten.test

import android.app.Application
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging

class MainApplication : Application() {

    val provider = AppUserInfoProvider(this)
    lateinit var settings: IAMSettings

    override fun onCreate() {
        super.onCreate()
        settings = IAMSettings(this)
        InAppMessaging.configure(
            context = this,
            subscriptionKey = settings.getSubscriptionKey(),
            configUrl = settings.getConfigUrl(),
            enableTooltipFeature = settings.isTooltipEnabled()
        )
        InAppMessaging.instance().registerPreference(provider)
    }
}