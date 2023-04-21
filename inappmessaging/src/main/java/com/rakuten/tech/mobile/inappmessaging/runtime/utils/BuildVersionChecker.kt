package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.os.Build

/**
 * Class for checking build version.
 */
internal object BuildVersionChecker {
    fun isNougatAndAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    fun isAndroidQAndAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    fun isAndroidOAndAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    fun isAndroidTAndAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}
