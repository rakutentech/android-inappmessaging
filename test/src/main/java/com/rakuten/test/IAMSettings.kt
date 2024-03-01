package com.rakuten.test

import android.content.Context
import android.content.pm.PackageManager
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil

class IAMSettings(private val context: Context) {

    init {
        setDefaults()
    }

    fun getConfigUrl(): String {
        return PreferencesUtil.getString(context, SHARED_FILE, CONFIG_URL, "").orEmpty()
    }

    fun getSubscriptionKey(): String {
        return PreferencesUtil.getString(context, SHARED_FILE, SUBSCRIPTION_KEY, "").orEmpty()
    }

    fun isTooltipEnabled(): Boolean {
        return PreferencesUtil.getBoolean(context, SHARED_FILE, IS_TOOLTIP_ENABLED, true)
    }

    fun saveConfig(configUrl: String, subscriptionKey: String, isTooltipEnabled: Boolean) {
        PreferencesUtil.clear(context, SHARED_FILE)
        PreferencesUtil.putString(context, SHARED_FILE, CONFIG_URL, configUrl)
        PreferencesUtil.putString(context, SHARED_FILE, SUBSCRIPTION_KEY, subscriptionKey)
        PreferencesUtil.putBoolean(context, SHARED_FILE, IS_TOOLTIP_ENABLED, isTooltipEnabled)
    }

    private fun setDefaults() {
        val metadata =
            context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).metaData

        var configUrl = getConfigUrl()
        if (configUrl.isEmpty()) {
            configUrl = metadata.getString("com.rakuten.tech.mobile.inappmessaging.configurl").orEmpty()
        }

        var subscriptionKey = getSubscriptionKey()
        if (subscriptionKey.isEmpty()) {
            subscriptionKey = metadata.getString("com.rakuten.tech.mobile.inappmessaging.subscriptionkey").orEmpty()
        }

        saveConfig(configUrl, subscriptionKey, isTooltipEnabled())
    }

    companion object {
        private const val SHARED_FILE = "iam-config"
        private const val CONFIG_URL = "config-url"
        private const val SUBSCRIPTION_KEY = "subscription-key"
        private const val IS_TOOLTIP_ENABLED = "is-tooltip-enabled"
    }
}