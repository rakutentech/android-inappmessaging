package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.os.Build
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test
import org.robolectric.util.ReflectionHelpers

class BuildVersionCheckerSpec {

    @Test
    fun `should return correctly when calling isNougatAndAbove()`() {
        setSdkInt(Build.VERSION_CODES.N)
        BuildVersionChecker.isNougatAndAbove().shouldBeTrue()

        setSdkInt(Build.VERSION_CODES.N + 1)
        BuildVersionChecker.isNougatAndAbove().shouldBeTrue()

        setSdkInt(Build.VERSION_CODES.N - 1)
        BuildVersionChecker.isNougatAndAbove().shouldBeFalse()
    }

    @Test
    fun `should return correctly when calling isAndroidQAndAbove()`() {
        setSdkInt(Build.VERSION_CODES.Q)
        BuildVersionChecker.isAndroidQAndAbove().shouldBeTrue()

        setSdkInt(Build.VERSION_CODES.Q + 1)
        BuildVersionChecker.isAndroidQAndAbove().shouldBeTrue()

        setSdkInt(Build.VERSION_CODES.Q - 1)
        BuildVersionChecker.isAndroidQAndAbove().shouldBeFalse()
    }

    @Test
    fun `should return correctly when calling isAndroidOAndAbove()`() {
        setSdkInt(Build.VERSION_CODES.O)
        BuildVersionChecker.isAndroidOAndAbove().shouldBeTrue()

        setSdkInt(Build.VERSION_CODES.O + 1)
        BuildVersionChecker.isAndroidOAndAbove().shouldBeTrue()

        setSdkInt(Build.VERSION_CODES.O - 1)
        BuildVersionChecker.isAndroidOAndAbove().shouldBeFalse()
    }

    @Test
    fun `should return correctly when calling isAndroidTAndAbove()`() {
        setSdkInt(Build.VERSION_CODES.TIRAMISU)
        BuildVersionChecker.isAndroidTAndAbove().shouldBeTrue()

        setSdkInt(Build.VERSION_CODES.TIRAMISU + 1)
        BuildVersionChecker.isAndroidTAndAbove().shouldBeTrue()

        setSdkInt(Build.VERSION_CODES.TIRAMISU - 1)
        BuildVersionChecker.isAndroidTAndAbove().shouldBeFalse()
    }

    private fun setSdkInt(sdkInt: Int) {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", sdkInt)
    }
}
