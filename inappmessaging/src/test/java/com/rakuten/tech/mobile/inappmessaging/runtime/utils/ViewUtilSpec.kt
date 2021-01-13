package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for ViewUtil.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ViewUtilSpec : BaseTest() {

    @Test
    fun `should have correct animation duration`() {
        val animation = ViewUtil.getSlidingAnimation(
                ApplicationProvider.getApplicationContext(),
                SlideFromDirectionType.BOTTOM)
        animation.duration shouldBeEqualTo 400L
    }
}
