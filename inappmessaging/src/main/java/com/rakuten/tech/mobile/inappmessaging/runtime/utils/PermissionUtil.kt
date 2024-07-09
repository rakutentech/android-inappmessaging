package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

internal enum class CheckPermissionResult {
    CAN_ASK,

    /** Permission is denied once. */
    PREVIOUSLY_DENIED,

    /** Permission is permanently denied, which means native prompt won't show ever again. */
    PERMANENTLY_DENIED,

    GRANTED,
}

internal object PermissionUtil {

    fun isPermissionGranted(context: Context, permission: String) =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    @SuppressWarnings("ReturnCount")
    @JvmStatic
    fun checkPermission(activity: Activity, permission: String): CheckPermissionResult {
        if (isPermissionGranted(activity, permission)) {
            return CheckPermissionResult.GRANTED
        }

        // If permission denied previously
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            return CheckPermissionResult.PREVIOUSLY_DENIED
        } else {
            // Permission denied or first time requested
            val isFirstTime = isFirstTimeAskingPermission(activity, permission)
            if (isFirstTime) {
                firstTimeAskingPermission(activity, permission, false)
                return CheckPermissionResult.CAN_ASK
            } else {
                // Handle the feature without permission or ask user to manually allow permission
                return CheckPermissionResult.PERMANENTLY_DENIED
            }
        }
    }

    private fun firstTimeAskingPermission(context: Context, permission: String, isFirstTime: Boolean) {
        getPermissionCache(context).edit().putBoolean(permission, isFirstTime).apply()
    }

    private fun isFirstTimeAskingPermission(context: Context, permission: String): Boolean =
        getPermissionCache(context).getBoolean(permission, true)

    private fun getPermissionCache(context: Context) =
        context.getSharedPreferences("iam_permission_prefs", Context.MODE_PRIVATE)
}
