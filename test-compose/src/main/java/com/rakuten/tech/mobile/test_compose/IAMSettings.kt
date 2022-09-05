package com.rakuten.tech.mobile.test_compose

import android.content.Context
import android.content.pm.PackageManager

class IAMSettings(context: Context) {

    var subscriptionKey: String
    var configUrl: String

    init {
        val metadata =
            context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).metaData

        subscriptionKey = metadata.getString("com.rakuten.tech.mobile.inappmessaging.subscriptionkey") ?: ""
        configUrl = metadata.getString("com.rakuten.tech.mobile.inappmessaging.configurl") ?: ""
    }
}