package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.MockPicasso
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.MockPicassoReturnType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.PositionType
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TooltipHelper
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InAppMessagingTooltipViewSpec {
    private val tooltipView = TooltipHelper.inflateTooltipView(mock(Activity::class.java))

    @Test
    fun `should not be displayed when image URL is null`() {
        tooltipView.populateViewData(TooltipHelper.createMessage(imageUrl = null))

        tooltipView.visibility.shouldBeEqualTo(View.GONE)
    }

    @Test
    fun `should not be displayed when image URL is empty`() {
        tooltipView.populateViewData(TooltipHelper.createMessage(imageUrl = ""))

        tooltipView.visibility.shouldBeEqualTo(View.GONE)
    }

    @Test
    fun `should not be displayed when exception is encountered during image display`() {
        tooltipView.picasso = MockPicasso.init(MockPicassoReturnType.GENERIC_EXCEPTION)
        tooltipView.populateViewData(TooltipHelper.createMessage())

        tooltipView.visibility.shouldBeEqualTo(View.GONE)
    }

    @Test
    fun `should not be displayed when callback error is encountered during image display`() {
        tooltipView.picasso = MockPicasso.init(MockPicassoReturnType.CALLBACK_ERROR)
        tooltipView.populateViewData(TooltipHelper.createMessage())

        tooltipView.visibility.shouldBeEqualTo(View.GONE)
    }
}

@RunWith(RobolectricTestRunner::class)
class InAppMessagingTooltipAnchorListenerSpec {
    private val hostAppActivity = mock(Activity::class.java)
    private val mockObserver = mock(ViewTreeObserver::class.java)
    private val mockAnchorView = mock(View::class.java)
    private val mockResources = mock(Resources::class.java)
    private lateinit var tooltipView: InAppMessagingTooltipView

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        InAppMessaging.initialize(context)
        InAppMessaging.instance().registerMessageDisplayActivity(hostAppActivity)

        `when`(hostAppActivity.resources).thenReturn(mockResources)
        `when`(hostAppActivity.packageName).thenReturn("test")
        controlFindViewById(1, mockAnchorView)
        `when`(mockAnchorView.viewTreeObserver).thenReturn(mockObserver)
        `when`(mockObserver.isAlive).thenReturn(true)

        tooltipView = TooltipHelper.inflateTooltipView(hostAppActivity)
        tooltipView.populateViewData(TooltipHelper.createMessage())
    }

    @Test
    fun `should listen to anchor view layout updates`() {
        tooltipView.addAnchorViewListeners()

        verify(mockObserver).addOnGlobalLayoutListener(any())
    }

    @Test
    fun `should not listen to anchor view layout updates when activity is null`() {
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        tooltipView.addAnchorViewListeners()

        verify(mockObserver, never()).addOnGlobalLayoutListener(any())
    }

    @Test
    fun `should not listen to anchor view layout updates when anchor view is not found`() {
        controlFindViewById(1, null)
        tooltipView.addAnchorViewListeners()

        verify(mockObserver, never()).addOnGlobalLayoutListener(any())
    }

    @Test
    fun `addAnchorViewListeners() should not crash when observer is null`() {
        `when`(mockAnchorView.viewTreeObserver).thenReturn(null)

        tooltipView.addAnchorViewListeners()
    }

    @Test
    fun `addAnchorViewListeners() should not crash when observer is not alive anymore`() {
        `when`(mockObserver.isAlive).thenReturn(false)

        tooltipView.addAnchorViewListeners()
    }

    @Test
    fun `should call addAnchorViewListeners() when attached to window`() {
        // This test is using reflection and should be generally avoided.
        tooltipView.javaClass.getDeclaredMethod("onAttachedToWindow").apply {
            isAccessible = true
        }.invoke(tooltipView)

        verify(mockObserver).addOnGlobalLayoutListener(any())
    }

    @Test
    fun `should remove anchor view layout listeners`() {
        tooltipView.removeAnchorViewListeners()

        verify(mockObserver).removeOnGlobalLayoutListener(any())
    }

    @Test
    fun `removeAnchorViewListeners() should not crash when observer is null`() {
        `when`(mockAnchorView.viewTreeObserver).thenReturn(null)

        tooltipView.removeAnchorViewListeners()
    }

    @Test
    fun `removeAnchorViewListeners() should not crash when observer is not alive anymore`() {
        `when`(mockObserver.isAlive).thenReturn(false)

        tooltipView.removeAnchorViewListeners()
    }

    @Test
    fun `should call removeAnchorViewListeners() when attached to window`() {
        // This test is using reflection and should be generally avoided.
        tooltipView.javaClass.getDeclaredMethod("onDetachedFromWindow").apply {
            isAccessible = true
        }.invoke(tooltipView)

        verify(mockObserver).removeOnGlobalLayoutListener(any())
    }

    @Test
    fun `should set position when there is a change in anchor view's layout in window`() {
        val mockAnchor = Button(ApplicationProvider.getApplicationContext())
        mockAnchor.apply {
            text = "I am an anchor view"
            x = 100f
            y = 100f
        }

        controlFindViewById(2, mockAnchor)

        tooltipView.addAnchorViewListeners()

        controlFindViewById(android.R.id.content, mock(ViewGroup::class.java))

        mockAnchor.viewTreeObserver.dispatchOnGlobalLayout()

        tooltipView.x.shouldNotBeEqualTo(0)
        tooltipView.y.shouldNotBeEqualTo(0)
    }

    @Test
    fun `should set position when there is a change in anchor view's layout in ScrollView`() {
        setPositionInScrollingParent(ScrollView(ApplicationProvider.getApplicationContext()))
    }

    @Test
    fun `should set position when there is a change in anchor view's layout in NestedScrollView`() {
        setPositionInScrollingParent(NestedScrollView(ApplicationProvider.getApplicationContext()))
    }

    private fun setPositionInScrollingParent(scroll: ViewGroup) {
        val anchor = Button(ApplicationProvider.getApplicationContext())
        anchor.apply {
            text = "I am an anchor view"
            x = 100f
            y = 100f
        }

        controlFindViewById(2, anchor)

        scroll.addView(anchor)
        tooltipView.addAnchorViewListeners()

        anchor.viewTreeObserver.dispatchOnGlobalLayout()
    }

    private fun controlFindViewById(id: Int, returnView: View?) {
        `when`(mockResources.getIdentifier(any(), any(), any())).thenReturn(id)
        `when`(hostAppActivity.findViewById<View>(id)).thenReturn(returnView)
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
class InAppMessagingTooltipViewCallbackSuccessSpec(private val position: String) {
    private val tooltipView = TooltipHelper.inflateTooltipView(mock(Activity::class.java))

    @Test
    fun `should be displayed when image is loaded`() {
        TooltipHelper.loadImage(tooltipView, position)

        tooltipView.visibility.shouldBeEqualTo(View.VISIBLE)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0}",
        )
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf("top-left"),
                arrayOf("top-center"),
                arrayOf("top-right"),
                arrayOf("right"),
                arrayOf("bottom-right"),
                arrayOf("bottom-center"),
                arrayOf("bottom-left"),
                arrayOf("left"),
            )
        }
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
class InAppMessagingTooltipViewPopulateDataSpec(private val position: String, private val expected: Int) {
    private val tooltipView = TooltipHelper.inflateTooltipView(mock(Activity::class.java))

    @Test
    fun `populateViewData should set correct tooltip position`() {
        tooltipView.populateViewData(TooltipHelper.createMessage(position))

        tooltipView.type.ordinal.shouldBeEqualTo(expected)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0}",
        )
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf("top-left", PositionType.TOP_LEFT.ordinal),
                arrayOf("top-center", PositionType.TOP_CENTER.ordinal),
                arrayOf("top-right", PositionType.TOP_RIGHT.ordinal),
                arrayOf("right", PositionType.RIGHT.ordinal),
                arrayOf("bottom-right", PositionType.BOTTOM_RIGHT.ordinal),
                arrayOf("bottom-center", PositionType.BOTTOM_CENTER.ordinal),
                arrayOf("bottom-left", PositionType.BOTTOM_LEFT.ordinal),
                arrayOf("left", PositionType.LEFT.ordinal),
                arrayOf("", PositionType.BOTTOM_CENTER.ordinal),
                arrayOf("invalid-position", PositionType.BOTTOM_CENTER.ordinal),
            )
        }
    }
}
