package com.rakuten.tech.mobile.inappmessaging.runtime.runnable

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageModalView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageSlideUpView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

/**
 * Test class for DisplayMessageRunnable.
 */
@RunWith(RobolectricTestRunner::class)
@SuppressWarnings("LargeClass")
class DisplayMessageRunnableSpec : BaseTest() {
    private val message = mock(Message::class.java)
    private var hostAppActivity = mock(Activity::class.java)
    private var mockDisplay = mock(DisplayManager::class.java)
    private val view = mock(View::class.java)
    private val window = mock(Window::class.java)
    private val payload = TestDataHelper.message0Payload

    @Before
    override fun setup() {
        super.setup()
        `when`(view!!.id).thenReturn(12343254)
        `when`(hostAppActivity.window).thenReturn(window)
        `when`(message.campaignId).thenReturn("1234")
    }

    @Test
    fun `should not throw exception fo invalid message type`() {
        `when`(message.type).thenReturn(0)
        DisplayMessageRunnable(message, hostAppActivity).run()
    }

    @Test
    fun `should not throw exception fo invalid does not exist`() {
        `when`(message.type).thenReturn(100)
        DisplayMessageRunnable(message, hostAppActivity).run()
    }

    @Test(expected = NullPointerException::class)
    fun `should throw null pointer exception when modal`() {
        `when`(message.type).thenReturn(InAppMessageType.MODAL.typeId)
        DisplayMessageRunnable(message, hostAppActivity).run()
    }

    @Test(expected = NullPointerException::class)
    fun `should throw null pointer exception when fullscreen`() {
        `when`(message.type).thenReturn(InAppMessageType.FULL.typeId)
        DisplayMessageRunnable(message, hostAppActivity).run()
    }

    @Test(expected = NullPointerException::class)
    fun `should throw null pointer exception when tooltip`() {
        `when`(message.type).thenReturn(InAppMessageType.TOOLTIP.typeId)
        DisplayMessageRunnable(message, hostAppActivity).run()
    }

    @Test
    fun `should throw exception with mock activity for full`() {
        setupActivity()
        `when`(message.type).thenReturn(InAppMessageType.FULL.typeId)
        verifyShouldDisplayTooltip()
    }

    @Test
    fun `should not throw exception with mock activity for modal`() {
        setupActivity()
        `when`(message.type).thenReturn(InAppMessageType.MODAL.typeId)
        verifyShouldDisplayTooltip()
    }

    @Test
    fun `should not throw exception with mock activity for slide`() {
        setupActivity()
        `when`(message.type).thenReturn(InAppMessageType.SLIDE.typeId)
        verifyShouldDisplayTooltip()
    }

    @Test
    fun `should not throw exception with mock activity for tooltip`() {
        setupActivity()
        `when`(message.type).thenReturn(InAppMessageType.TOOLTIP.typeId)
        verifyShouldDisplayTooltip()
    }

    @Test
    fun `should not display campaign due to already displayed normal campaign`() {
        setupActivity()
        `when`(message.type).thenReturn(InAppMessageType.MODAL.typeId)
        `when`(hostAppActivity.findViewById<View>(R.id.in_app_message_base_view))
            .thenReturn(mock(View::class.java))
        DisplayMessageRunnable(message, hostAppActivity).run()
        verify(hostAppActivity, never()).layoutInflater
    }

    @Test
    fun `should display for tooltip with no child`() {
        setupActivity()
        setupTooltip(0)
        verifyShouldDisplayTooltip()
    }

    @Test
    fun `should display for tooltip with no other tooltip`() {
        setupActivity()
        setupTooltip()
        `when`(hostAppActivity.findViewById<View>(R.id.in_app_message_tooltip_view)).thenReturn(null)
        verifyShouldDisplayTooltip()
    }

    @Test
    fun `should display for tooltip with null parent`() {
        setupActivity()
        setupTooltip(isNullParent = true)
        verifyShouldDisplayTooltip()
    }

    @Test
    fun `should display for tooltip with null child`() {
        setupActivity()
        setupTooltip(isNullChild = true)
        verifyShouldDisplayTooltip()
    }

    @Test
    fun `should display for tooltip with diff child`() {
        setupActivity()
        setupTooltip(tag = "not-test")
        verifyShouldDisplayTooltip()
    }

    @Test
    fun `should not display campaign due to already displayed normal campaign for tooltip`() {
        setupActivity()
        setupTooltip()
        `when`(hostAppActivity.findViewById<View>(R.id.in_app_message_base_view))
            .thenReturn(mock(InAppMessageModalView::class.java))
        verifyNotShowTooltip()
    }

    @Test
    fun `should not display campaign due to already displayed tooltip campaign`() {
        setupActivity()
        setupTooltip()
        verifyNotShowTooltip()
    }

    @Test
    fun `should not display for tooltip with valid details and not visible`() {
        setupActivity()
        setupTooltip()
        setupTooltipDetails()
        `when`(hostAppActivity.findViewById<View>(1)).thenReturn(null)
        verifyShouldDisplayTooltip()
        verify(hostAppActivity, never()).addContentView(any(), any())
    }

    @Test
    fun `should display for tooltip with valid details`() {
        setupActivity()
        setupTooltip()
        setupTooltipDetails()
        verifyShouldDisplayTooltip()
        verify(hostAppActivity).addContentView(any(), any())
        verify(mockDisplay).removeMessage(any(), any(), any(), any())
    }

    @Test
    fun `should not call display manager for null auto-disappear`() {
        setupActivity()
        setupTooltip()
        setupTooltipDetails(autoDisappear = null)
        verifyShouldDisplayTooltip()
        verify(hostAppActivity).addContentView(any(), any())
        verify(mockDisplay, never()).removeMessage(any(), any(), any(), any())
    }

    @Test
    fun `should not call display manager for 0 auto-disappear`() {
        setupActivity()
        setupTooltip()
        setupTooltipDetails(autoDisappear = 0)
        verifyShouldDisplayTooltip()
        verify(hostAppActivity).addContentView(any(), any())
        verify(mockDisplay, never()).removeMessage(any(), any(), any(), any())
    }

    @Test
    fun `should display tooltip in scroll view`() {
        setupActivity()
        setupTooltip()
        val scroll = setupTooltipDetails(true)
        val scrollChild = mock(ViewGroup::class.java)
        `when`(scroll?.getChildAt(0)).thenReturn(scrollChild)
        val runner = DisplayMessageRunnable(message, hostAppActivity, mockDisplay)
        runner.run()

        verify(scrollChild)?.addView(any(), any<ViewGroup.LayoutParams>())
    }

    @SuppressWarnings("LongMethod")
    private fun setupTooltipDetails(isScroll: Boolean = false, autoDisappear: Int? = 5): ScrollView? {
        val tooltip = Tooltip("target", "top-center", "testurl", autoDisappear)
        val mockView = mock(View::class.java)
        val mockResource = mock(Resources::class.java)
        `when`(hostAppActivity.findViewById<View>(R.id.in_app_message_tooltip_view)).thenReturn(null)
        `when`(message.getTooltipConfig()).thenReturn(tooltip)
        `when`(hostAppActivity.packageName).thenReturn("test")
        `when`(hostAppActivity.resources).thenReturn(mockResource)
        val metrics = DisplayMetrics()
        metrics.density = 10f
        `when`(mockResource.displayMetrics).thenReturn(metrics)
        `when`(mockResource.getIdentifier(eq("target"), eq("id"), any())).thenReturn(1)
        `when`(hostAppActivity.findViewById<View>(1)).thenReturn(mockView)
        return if (isScroll) {
            val parent = mock(ScrollView::class.java)
            `when`(mockView.parent).thenReturn(parent)
            parent
        } else {
            null
        }
    }

    @SuppressWarnings("LongMethod")
    private fun setupTooltip(
        childCount: Int = 1,
        isNullChild: Boolean = false,
        tag: String? = null,
        isNullParent: Boolean = false,
    ) {
        `when`(hostAppActivity.findViewById<View>(R.id.in_app_message_base_view))
            .thenReturn(mock(InAppMessageSlideUpView::class.java))
        `when`(message.type).thenReturn(InAppMessageType.TOOLTIP.typeId)
        val tooltip = mock(View::class.java)
        val parent = mock(ViewGroup::class.java)
        `when`(hostAppActivity.findViewById<View>(R.id.in_app_message_tooltip_view))
            .thenReturn(tooltip)
        if (isNullParent) {
            `when`(tooltip.parent).thenReturn(null)
        } else {
            `when`(tooltip.parent).thenReturn(parent)
        }
        `when`(parent.childCount).thenReturn(childCount)
        if (isNullChild) {
            `when`(parent.getChildAt(0)).thenReturn(null)
        } else {
            `when`(parent.getChildAt(0)).thenReturn(tooltip)
        }
        `when`(tooltip.id).thenReturn(R.id.in_app_message_tooltip_view)
        `when`(tooltip.tag).thenReturn(tag ?: "test")
        `when`(message.campaignId).thenReturn("test")
    }

    private fun verifyNotShowTooltip() {
        `when`(message.type).thenReturn(InAppMessageType.TOOLTIP.typeId)
        DisplayMessageRunnable(message, hostAppActivity).run()
        verify(hostAppActivity, never()).layoutInflater
    }

    private fun setupActivity() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id",
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
        `when`(message.messagePayload).thenReturn(payload)
        `when`(
            hostAppActivity
                .layoutInflater,
        ).thenReturn(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
    }

    private fun verifyShouldDisplayTooltip(mockLayout: FrameLayout? = null) {
        val runner = DisplayMessageRunnable(message, hostAppActivity, mockDisplay)
        runner.testLayout = mockLayout
        runner.run()
        verify(hostAppActivity).layoutInflater
    }
}
