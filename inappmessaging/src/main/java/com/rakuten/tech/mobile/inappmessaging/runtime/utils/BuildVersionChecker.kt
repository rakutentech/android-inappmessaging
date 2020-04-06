package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.os.Build

/**
 * Class for checking build version.
 */
internal interface BuildVersionChecker {
    fun isNougatAndAbove(): Boolean
    fun isAndroidQAndAbove(): Boolean

    companion object {
        private var instance: BuildVersionChecker = BuildVersionCheckerImpl()

        fun instance() = instance
    }

    private class BuildVersionCheckerImpl : BuildVersionChecker {
        override fun isNougatAndAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        override fun isAndroidQAndAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
}
