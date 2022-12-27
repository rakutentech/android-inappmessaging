package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.PositionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessagingTooltipView
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
@SuppressWarnings("LargeClass")
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

    @Test
    fun `should return correct position for top-right`() {
        val pos = setupPosition(PositionType.TOP_RIGHT)
        pos.first shouldBeEqualTo 100 + WIDTH + InAppMessagingTooltipView.PADDING / 4
        pos.second shouldBeEqualTo 100 - HEIGHT - MARGIN_V - InAppMessagingTooltipView.TRI_SIZE / 2
    }

    @Test
    fun `should return correct position for top-center`() {
        val pos = setupPosition(PositionType.TOP_CENTER)
        pos.first shouldBeEqualTo 105 - WIDTH / 2
        pos.second shouldBeEqualTo 100 - HEIGHT - MARGIN_V - InAppMessagingTooltipView.TRI_SIZE / 2
    }

    @Test
    fun `should return correct position for top-left`() {
        val pos = setupPosition(PositionType.TOP_LEFT)
        pos.first shouldBeEqualTo 100 - WIDTH - MARGIN_H - InAppMessagingTooltipView.TRI_SIZE / 2
        pos.second shouldBeEqualTo 100 - HEIGHT - MARGIN_V - InAppMessagingTooltipView.TRI_SIZE / 2
    }

    @Test
    fun `should return correct position for bottom-right`() {
        val pos = setupPosition(PositionType.BOTTOM_RIGHT)
        pos.first shouldBeEqualTo 110 + InAppMessagingTooltipView.PADDING / 4
        pos.second shouldBeEqualTo 110 - MARGIN_V + InAppMessagingTooltipView.TRI_SIZE / 2
    }

    @Test
    fun `should return correct position for bottom-center`() {
        val pos = setupPosition(PositionType.BOTTOM_CENTER)
        pos.first shouldBeEqualTo 105 - WIDTH / 2
        pos.second shouldBeEqualTo 110
    }

    @Test
    fun `should return correct position for bottom-left`() {
        val pos = setupPosition(PositionType.BOTTOM_LEFT)
        pos.first shouldBeEqualTo 100 - WIDTH - MARGIN_H - InAppMessagingTooltipView.TRI_SIZE / 2
        pos.second shouldBeEqualTo 110 - MARGIN_V + InAppMessagingTooltipView.TRI_SIZE / 2
    }

    @Test
    fun `should return correct position for right`() {
        val pos = setupPosition(PositionType.RIGHT)
        pos.first shouldBeEqualTo 110 - InAppMessagingTooltipView.TRI_SIZE / 2
        pos.second shouldBeEqualTo 100 - (HEIGHT - MARGIN_V + InAppMessagingTooltipView.TRI_SIZE) / 2
    }

    @Test
    fun `should return correct position for left`() {
        val pos = setupPosition(PositionType.LEFT)
        pos.first shouldBeEqualTo 100 - WIDTH - MARGIN_H - InAppMessagingTooltipView.TRI_SIZE / 2
        pos.second shouldBeEqualTo 100 - (HEIGHT - MARGIN_V + InAppMessagingTooltipView.TRI_SIZE) / 2
    }

    @Test
    fun `should return correct position for scroll parent`() {
        val pos = setupPosition(PositionType.TOP_CENTER, true)
        pos.first shouldBeEqualTo 105 - WIDTH / 2
        pos.second shouldBeEqualTo 100 - HEIGHT - MARGIN_V - InAppMessagingTooltipView.TRI_SIZE / 2
    }

    @Test
    fun `should return null for null parent`() {
        val mockView = Mockito.mock(View::class.java)
        `when`(mockView.parent).thenReturn(null)
        ViewUtil.getScrollView(mockView).shouldBeNull()
    }

    @Test
    fun `should return null for non-scroll parent`() {
        val mockView = Mockito.mock(View::class.java)
        `when`(mockView.parent).thenReturn(Mockito.mock(ViewParent::class.java))
        ViewUtil.getScrollView(mockView).shouldBeNull()
    }

    @Test
    fun `should return scrollview parent`() {
        val mockView = Mockito.mock(View::class.java)
        val mockScrollView = Mockito.mock(ScrollView::class.java)
        `when`(mockView.parent).thenReturn(mockScrollView)
        ViewUtil.getScrollView(mockView).shouldNotBeNull()
    }

    @Test
    fun `should return nested scrollview parent`() {
        val mockView = Mockito.mock(View::class.java)
        val mockScrollView = Mockito.mock(NestedScrollView::class.java)
        `when`(mockView.parent).thenReturn(mockScrollView)
        ViewUtil.getScrollView(mockView).shouldNotBeNull()
    }

    @Test
    fun `should return scrollview grandparent`() {
        val mockView = Mockito.mock(View::class.java)
        val mockView2 = Mockito.mock(ViewParent::class.java)
        val mockScrollView = Mockito.mock(ScrollView::class.java)
        `when`(mockView.parent).thenReturn(mockView2)
        `when`(mockView2.parent).thenReturn(mockScrollView)
        ViewUtil.getScrollView(mockView).shouldNotBeNull()
    }

    @Test
    fun `should return correct non-null right and bottom edge position`() {
        val pos = ViewUtil.getEdgePosition(500, 500, POS)
        pos.first.shouldNotBeNull()
        pos.second.shouldNotBeNull()
    }

    @Test
    fun `should return correct null right and bottom edge position`() {
        val pos = ViewUtil.getEdgePosition(10, 10, POS)
        pos.first.shouldBeNull()
        pos.second.shouldBeNull()
    }

    @Test
    fun `should return parent FrameLayout`() {
        val mockView = Mockito.mock(View::class.java)
        val mockView2 = Mockito.mock(FrameLayout::class.java)
        `when`(mockView.parent).thenReturn(mockView2)

        ViewUtil.getFrameLayout(mockView).shouldBeEqualTo(mockView2)
    }

    @Test
    fun `should return parent FrameLayout (multiple hierarchy)`() {
        val mockView = Mockito.mock(View::class.java)
        val mockView2 = Mockito.mock(LinearLayout::class.java)
        val mockView3 = Mockito.mock(FrameLayout::class.java)
        `when`(mockView.parent).thenReturn(mockView2)
        `when`(mockView2.parent).thenReturn(mockView3)

        ViewUtil.getFrameLayout(mockView).shouldBeEqualTo(mockView3)
    }

    @Test
    fun `should return null if no FrameLayout`() {
        val mockView = Mockito.mock(View::class.java)
        val mockView2 = Mockito.mock(LinearLayout::class.java)
        `when`(mockView.parent).thenReturn(mockView2)

        ViewUtil.getFrameLayout(mockView).shouldBeNull()
    }

    @Test
    fun `should return -1 layout child index if view has no child`() {
        val mockGroup = Mockito.mock(ViewGroup::class.java)
        `when`(mockGroup.childCount).thenReturn(0)

        ViewUtil.getFirstLayoutChild(mockGroup).shouldBeEqualTo(-1)
    }

    @Test
    fun `should return -1 layout child index if child is not a ViewGroup`() {
        val mockGroup = Mockito.mock(ViewGroup::class.java)
        val mockView = Mockito.mock(View::class.java)
        `when`(mockGroup.childCount).thenReturn(1)
        `when`(mockGroup.getChildAt(0)).thenReturn(mockView)

        ViewUtil.getFirstLayoutChild(mockGroup).shouldBeEqualTo(-1)
    }

    @Test
    fun `should return layout child index if child is a ViewGroup`() {
        val mockGroup = Mockito.mock(ViewGroup::class.java)
        val mockGroup2 = Mockito.mock(ViewGroup::class.java)
        `when`(mockGroup.childCount).thenReturn(1)
        `when`(mockGroup.getChildAt(0)).thenReturn(mockGroup2)

        ViewUtil.getFirstLayoutChild(mockGroup).shouldBeEqualTo(0)
    }

    @Test
    fun `should return toolbar index`() {
        val mockFrameLayout = Mockito.mock(FrameLayout::class.java)
        val mockToolbar = Mockito.mock(Toolbar::class.java)
        `when`(mockFrameLayout.childCount).thenReturn(1)
        `when`(mockFrameLayout.getChildAt(0)).thenReturn(mockToolbar)

        ViewUtil.getToolBarIndex(mockFrameLayout).shouldBeEqualTo(0)
    }

    @Test
    fun `should return visible if view is not scrollable`() {
        val mockView = Mockito.mock(View::class.java)

        ViewUtil.isViewVisible(mockView).shouldBeTrue()
    }

    @Test
    fun `should return true (visible) if scrollable area target is visible`() {
        val mockView = Mockito.mock(View::class.java)
        val mockScrollView = Mockito.mock(ScrollView::class.java)
        `when`(mockView.parent).thenReturn(mockScrollView)
        `when`(mockView.getLocalVisibleRect(any())).thenReturn(true)

        ViewUtil.isViewVisible(mockView).shouldBeTrue()
    }

    @Test
    fun `should return false (not visible) if scrollable area target is not visible`() {
        val mockView = Mockito.mock(View::class.java)
        val mockScrollView = Mockito.mock(ScrollView::class.java)
        `when`(mockView.parent).thenReturn(mockScrollView)
        `when`(mockView.getLocalVisibleRect(any())).thenReturn(false)

        ViewUtil.isViewVisible(mockView).shouldBeFalse()
    }

    private fun setupPosition(type: PositionType, isScroll: Boolean = false): Pair<Int, Int> {
        val mockView = Mockito.mock(View::class.java)
        `when`(mockView.width).thenReturn(10)
        `when`(mockView.height).thenReturn(10)
        `when`(mockView.top).thenReturn(100)
        `when`(mockView.left).thenReturn(100)
        val mockP = if (isScroll) Mockito.mock(SwipeRefreshLayout::class.java) else Mockito.mock(ViewGroup::class.java)
        val mockGp = Mockito.mock(ViewGroup::class.java)
        val mockRoot = Mockito.mock(ViewGroup::class.java)
        `when`(mockView.parent).thenReturn(mockP)
        `when`(mockP.parent).thenReturn(mockGp)
        `when`(mockGp.parent).thenReturn(mockRoot)
        `when`(mockView.rootView).thenReturn(mockRoot)
        `when`(mockView.getDrawingRect(any())).thenAnswer {
            val rect = it.getArgument<Rect>(0)
            rect.set(100, 100, 100, 100)
        }

        return ViewUtil.getPosition(mockView, type, WIDTH, HEIGHT, MARGIN_H, MARGIN_V, holderLocation)
    }

    companion object {
        private const val WIDTH = 10
        private const val HEIGHT = 10
        private const val MARGIN_H = 10
        private const val MARGIN_V = 10
        private val POS = Pair(100, 100)
    }
}
