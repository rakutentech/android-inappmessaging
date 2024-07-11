package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.content.pm.PackageManager

internal object PermissionUtil {

    fun isPermissionGranted(context: Context, permission: String) =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}
