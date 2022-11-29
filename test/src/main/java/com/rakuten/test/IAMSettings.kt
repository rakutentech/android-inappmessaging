package com.rakuten.test

import android.content.Context
import android.content.pm.PackageManager

class IAMSettings(context: Context) {

    var subscriptionKey: String
    var configUrl: String
    var isTooltipFeatEnabled: Boolean = false

    init {
        val metadata =
            context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).metaData

        subscriptionKey = metadata.getString("com.rakuten.tech.mobile.inappmessaging.subscriptionkey") ?: ""
        configUrl = metadata.getString("com.rakuten.tech.mobile.inappmessaging.configurl") ?: ""
    }
}