package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.test_helpers.MockPicasso
import com.rakuten.tech.mobile.inappmessaging.runtime.test_helpers.MockPicassoReturnType
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.PositionType
import com.rakuten.tech.mobile.inappmessaging.runtime.test_helpers.TooltipHelper
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
    private val hostAppActivity = mock(Activity::class.java)
    private lateinit var tooltipView: InAppMessagingTooltipView

    @Before
    fun setup() {
        `when`(hostAppActivity.layoutInflater)
            .thenReturn(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
        tooltipView = hostAppActivity.layoutInflater
            .inflate(R.layout.in_app_message_tooltip, null) as InAppMessagingTooltipView
    }

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
    private lateinit var tooltipView: InAppMessagingTooltipView

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        InAppMessaging.initialize(context)
        InAppMessaging.instance().registerMessageDisplayActivity(hostAppActivity)

        `when`(hostAppActivity.layoutInflater).thenReturn(LayoutInflater.from(context))
        val mockResources = mock(Resources::class.java)
        `when`(hostAppActivity.resources).thenReturn(mockResources)
        `when`(hostAppActivity.packageName).thenReturn("test")
        `when`(mockResources.getIdentifier(any(), any(), any())).thenReturn(1)
        `when`(hostAppActivity.findViewById<View>(any())).thenReturn(mockAnchorView)
        `when`(mockAnchorView.viewTreeObserver).thenReturn(mockObserver)
        `when`(mockObserver.isAlive).thenReturn(true)

        tooltipView = hostAppActivity.layoutInflater
            .inflate(R.layout.in_app_message_tooltip, null) as InAppMessagingTooltipView
        tooltipView.populateViewData(TooltipHelper.createMessage())
    }

    @Test
    fun `should listen to anchor view layout updates`() {
        tooltipView.addAnchorViewListeners()

        verify(mockObserver).addOnGlobalLayoutListener(any())
    }

    @Test
    fun `addAnchorViewListeners() should not crash when observer is not alive anymore`() {
        `when`(mockObserver.isAlive).thenReturn(false)

        tooltipView.addAnchorViewListeners()
    }

    @Test
    fun `should remove anchor view layout listeners`() {
        tooltipView.removeAnchorViewListeners()

        verify(mockObserver).removeOnGlobalLayoutListener(any())
    }

    @Test
    fun `removeAnchorViewListeners() should not crash when observer is not alive anymore`() {
        `when`(mockObserver.isAlive).thenReturn(false)

        tooltipView.removeAnchorViewListeners()
    }

    @Test // TODO: dispatchOnGlobalLayout not called
    fun `should set position when anchor view's OnGlobalLayoutListener is triggered`() {
        `when`(mockAnchorView.x).thenReturn(100f)
        `when`(mockAnchorView.y).thenReturn(100f)
        tooltipView.addAnchorViewListeners()
        mockAnchorView.viewTreeObserver.dispatchOnGlobalLayout()

        tooltipView.x shouldNotBeEqualTo 0
        tooltipView.y shouldNotBeEqualTo 0
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
class InAppMessagingTooltipViewCallbackSuccessSpec(private val position: String) {
    private val hostAppActivity = mock(Activity::class.java)
    private lateinit var tooltipView: InAppMessagingTooltipView

    @Before
    fun setup() {
        `when`(hostAppActivity.layoutInflater)
            .thenReturn(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
        tooltipView = hostAppActivity.layoutInflater
            .inflate(R.layout.in_app_message_tooltip, null) as InAppMessagingTooltipView
    }

    @Test
    fun `should be displayed when image is loaded`() {
        TooltipHelper.loadImage(tooltipView, position)

        tooltipView.visibility.shouldBeEqualTo(View.VISIBLE)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0}"
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
                arrayOf("left")
            )
        }
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
class InAppMessagingTooltipViewPopulateDataSpec(private val position: String, private val expected: Int) {
    private val hostAppActivity = mock(Activity::class.java)
    private lateinit var tooltipView: InAppMessagingTooltipView

    @Before
    fun setup() {
        `when`(hostAppActivity.layoutInflater)
            .thenReturn(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
        tooltipView = hostAppActivity.layoutInflater
            .inflate(R.layout.in_app_message_tooltip, null) as InAppMessagingTooltipView
    }

    @Test
    fun `populateViewData should set correct tooltip position`() {
        tooltipView.populateViewData(TooltipHelper.createMessage(position))

        tooltipView.type.ordinal.shouldBeEqualTo(expected)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0}"
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
