package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.PositionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

/**
 * Test class for ViewUtil.
 */
@RunWith(RobolectricTestRunner::class)
@SuppressWarnings("LargeClass")
class ViewUtilSpec : BaseTest() {

    @Test
    fun `should have correct duration for bottom animation`() {
        val animation = ViewUtil.getSlidingAnimation(
            ApplicationProvider.getApplicationContext(),
            SlideFromDirectionType.BOTTOM,
        )
        animation?.duration shouldBeEqualTo 400L
    }

    @Test
    fun `should have correct duration for right animation`() {
        val animation = ViewUtil.getSlidingAnimation(
            ApplicationProvider.getApplicationContext(),
            SlideFromDirectionType.RIGHT,
        )
        animation?.duration shouldBeEqualTo 400L
    }

    @Test
    fun `should have correct duration for left animation`() {
        val animation = ViewUtil.getSlidingAnimation(
            ApplicationProvider.getApplicationContext(),
            SlideFromDirectionType.LEFT,
        )
        animation?.duration shouldBeEqualTo 400L
    }

    @Test
    fun `should have correct duration for others animation`() {
        val animation = ViewUtil.getSlidingAnimation(
            ApplicationProvider.getApplicationContext(),
            SlideFromDirectionType.INVALID,
        )
        animation?.duration shouldBeEqualTo 400L
    }

    @Test
    fun `should return null if invalid animation`() {
        val mockContext = mock(Context::class.java)
        val mockResource = mock(Resources::class.java)
        `when`(mockContext.resources).thenReturn(mockResource)
        `when`(mockResource.getAnimation(any())).thenThrow(Resources.NotFoundException("test"))
        val animation = ViewUtil.getSlidingAnimation(mockContext, SlideFromDirectionType.BOTTOM)
        animation.shouldBeNull()
    }

    @Test
    fun `should return null for null parent`() {
        val mockView = mock(View::class.java)
        `when`(mockView.parent).thenReturn(null)
        ViewUtil.getScrollView(mockView).shouldBeNull()
    }

    @Test
    fun `should return null for non-scroll parent`() {
        val mockView = mock(View::class.java)
        `when`(mockView.parent).thenReturn(mock(ViewParent::class.java))
        ViewUtil.getScrollView(mockView).shouldBeNull()
    }

    @Test
    fun `should return scrollview parent`() {
        val mockView = mock(View::class.java)
        val mockScrollView = mock(ScrollView::class.java)
        `when`(mockView.parent).thenReturn(mockScrollView)
        ViewUtil.getScrollView(mockView).shouldNotBeNull()
    }

    @Test
    fun `should return nested scrollview parent`() {
        val mockView = mock(View::class.java)
        val mockScrollView = mock(NestedScrollView::class.java)
        `when`(mockView.parent).thenReturn(mockScrollView)
        ViewUtil.getScrollView(mockView).shouldNotBeNull()
    }

    @Test
    fun `should return scrollview grandparent`() {
        val mockView = mock(View::class.java)
        val mockView2 = mock(ViewParent::class.java)
        val mockScrollView = mock(ScrollView::class.java)
        `when`(mockView.parent).thenReturn(mockView2)
        `when`(mockView2.parent).thenReturn(mockScrollView)
        ViewUtil.getScrollView(mockView).shouldNotBeNull()
    }

    @Test
    fun `should return false when calling isViewByNameVisible`() {
        val mockResourceUtil = mock(ResourceUtils::class.java)
        val mockActivity = mock(Activity::class.java)
        `when`(mockResourceUtil.findViewByName<View>(mockActivity, "viewName"))
            .thenReturn(mock(View::class.java))
        ViewUtil.isViewByNameVisible(mockActivity, "viewName", mockResourceUtil).shouldBeFalse()
    }
}

@RunWith(RobolectricTestRunner::class)
class ViewUtilGetTooltipPosition {
    private val mockContainer = mock(ViewGroup::class.java)
    private val mockAnchor = mock(View::class.java)
    private val mockTooltip = mock(View::class.java)

    @Before
    fun setup() {
        // Simulate anchor and tooltip have same dimensions to easily verify approximate location
        `when`(mockTooltip.width).thenReturn(WIDTH_HEIGHT)
        `when`(mockTooltip.height).thenReturn(WIDTH_HEIGHT)
        `when`(mockAnchor.width).thenReturn(WIDTH_HEIGHT)
        `when`(mockAnchor.height).thenReturn(WIDTH_HEIGHT)

        val location = IntArray(2)
        doAnswer { invocation ->
            val args = invocation.arguments[0] as IntArray
            args[0] = LOC_X_Y
            args[1] = LOC_X_Y
            args
        }.`when`(mockAnchor).getLocationOnScreen(location)
    }

    @Test
    fun `should return correct position when type is top-left`() {
        val pos = callGetTooltipPosition(PositionType.TOP_LEFT)
        pos.y shouldBeLessThan LOC_X_Y
        pos.x shouldBeLessThan LOC_X_Y
    }

    @Test
    fun `should return correct position when type is top-center`() {
        val pos = callGetTooltipPosition(PositionType.TOP_CENTER)
        pos.y shouldBeLessThan LOC_X_Y
        pos.x shouldBeEqualTo LOC_X_Y
    }

    @Test
    fun `should return correct position when type is top-right`() {
        val pos = callGetTooltipPosition(PositionType.TOP_RIGHT)
        pos.y shouldBeLessThan LOC_X_Y
        pos.x shouldBeGreaterThan LOC_X_Y
    }

    @Test
    fun `should return correct position when type is right`() {
        val pos = callGetTooltipPosition(PositionType.RIGHT)
        pos.y shouldBeEqualTo LOC_X_Y
        pos.x shouldBeGreaterThan LOC_X_Y
    }

    @Test
    fun `should return correct position when type is bottom-right`() {
        val pos = callGetTooltipPosition(PositionType.BOTTOM_RIGHT)
        pos.y shouldBeGreaterThan LOC_X_Y
        pos.x shouldBeGreaterThan LOC_X_Y
    }

    @Test
    fun `should return correct position when type is bottom-center`() {
        val pos = callGetTooltipPosition(PositionType.BOTTOM_CENTER)
        pos.y shouldBeGreaterThan LOC_X_Y
        pos.x shouldBeEqualTo LOC_X_Y
    }

    @Test
    fun `should return correct position when type is bottom-left`() {
        val pos = callGetTooltipPosition(PositionType.BOTTOM_LEFT)
        pos.y shouldBeGreaterThan LOC_X_Y
        pos.x shouldBeLessThan LOC_X_Y
    }

    @Test
    fun `should return correct position when type is left`() {
        val pos = callGetTooltipPosition(PositionType.LEFT)
        pos.y shouldBeEqualTo LOC_X_Y
        pos.x shouldBeLessThan LOC_X_Y
    }

    private fun callGetTooltipPosition(type: PositionType) = ViewUtil.getTooltipPosition(
        mockContainer,
        mockTooltip,
        mockAnchor,
        type,
        0,
    )

    companion object {
        private const val WIDTH_HEIGHT = 20
        private const val LOC_X_Y = 50
    }
}
