package com.rakuten.tech.mobile.inappmessaging.runtime.extensions

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging

/**
 * Prompts the Push Notification permission dialog if applicable
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal fun Activity.promptPushNotifPermissionDialog(requestCode: Int = InAppMessaging.PUSH_PRIMER_REQ_CODE) =
    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), requestCode)

/**
 * Redirects to app Push Notification Settings
 */
internal fun Activity.openAppNotifPermissionSettings() {
    val intent = Intent()
    intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.putExtra("android.provider.extra.APP_PACKAGE", this.packageName)

    this.startActivity(intent)
}