package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for ViewUtil.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ViewUtilSpec : BaseTest() {

    @Test
    fun `should have correct duration for bottom animation`() {
        val animation = ViewUtil.getSlidingAnimation(
            ApplicationProvider.getApplicationContext(),
            SlideFromDirectionType.BOTTOM
        )
        animation?.duration shouldBeEqualTo 400L
    }

    @Test
    fun `should have correct duration for right animation`() {
        val animation = ViewUtil.getSlidingAnimation(
            ApplicationProvider.getApplicationContext(),
            SlideFromDirectionType.RIGHT
        )
        animation?.duration shouldBeEqualTo 400L
    }

    @Test
    fun `should have correct duration for left animation`() {
        val animation = ViewUtil.getSlidingAnimation(
            ApplicationProvider.getApplicationContext(),
            SlideFromDirectionType.LEFT
        )
        animation?.duration shouldBeEqualTo 400L
    }

    @Test
    fun `should have correct duration for others animation`() {
        val animation = ViewUtil.getSlidingAnimation(
            ApplicationProvider.getApplicationContext(),
            SlideFromDirectionType.INVALID
        )
        animation?.duration shouldBeEqualTo 400L
    }

    @Test
    fun `should return null if invalid animation`() {
        val mockContext = Mockito.mock(Context::class.java)
        val mockResource = Mockito.mock(Resources::class.java)
        `when`(mockContext.resources).thenReturn(mockResource)
        `when`(mockResource.getAnimation(any())).thenThrow(Resources.NotFoundException("test"))
        val animation = ViewUtil.getSlidingAnimation(mockContext, SlideFromDirectionType.BOTTOM)
        animation.shouldBeNull()
    }
}
