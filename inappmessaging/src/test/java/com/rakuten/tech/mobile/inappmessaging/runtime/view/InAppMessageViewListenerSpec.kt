package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.os.Build
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.Magnifier
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.coroutine.MessageActionsCoroutine
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

/**
 * Test class for ModalViewOnTouchListener.
 */
@Config(sdk = [Build.VERSION_CODES.Q])
@Suppress("LargeClass")
class InAppMessageViewListenerSpec : BaseTest() {
    private var motionEvent = Mockito.mock(MotionEvent::class.java)
    private var keyEvent = Mockito.mock(KeyEvent::class.java)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        When calling motionEvent!!.rawX itReturns 0f
        When calling motionEvent!!.rawY itReturns 0f
    }

    @Test
    fun `should not throw exception when argument is null`() {
        InAppMessageViewListener(null)
    }

    @Test
    fun `should call is checked once`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true))
        val mockView = Mockito.mock(CheckBox::class.java)
        When calling mockView.isChecked itReturns true
        When calling mockView.id itReturns R.id.opt_out_checkbox
        listener.onClick(mockView)

        Mockito.verify(mockView, Mockito.times(1)).isChecked
    }

    @Test
    fun `should not throw exception with non-checkbox click`() {
        val mockDisplayManager = Mockito.mock(DisplayManager::class.java)
        val mockCoroutine = Mockito.mock(MessageActionsCoroutine::class.java)

        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)
        val mockView = Mockito.mock(CheckBox::class.java)
        When calling mockView.id itReturns R.id.message_close_button
        When calling mockCoroutine.executeTask(message, R.id.message_close_button, false) itReturns true

        listener.onClick(mockView)
    }

    @Test
    fun `should return true on touch with action down`() {
        val mockView = Mockito.mock(View::class.java)
        val mockEvent = Mockito.mock(MotionEvent::class.java)
        val mockCheck = Mockito.mock(BuildVersionChecker::class.java)

        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        When calling mockCheck.isAndroidQAndAbove() itReturns true
        When calling mockEvent.actionMasked itReturns MotionEvent.ACTION_DOWN

        listener.onTouch(mockView, mockEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with action move`() {
        val mockView = Mockito.mock(View::class.java)
        val mockEvent = Mockito.mock(MotionEvent::class.java)
        val mockCheck = Mockito.mock(BuildVersionChecker::class.java)

        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        When calling mockCheck.isAndroidQAndAbove() itReturns true
        When calling mockEvent.actionMasked itReturns MotionEvent.ACTION_MOVE
        listener.onTouch(mockView, mockEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with action cancel`() {
        val mockView = Mockito.mock(View::class.java)
        val mockEvent = Mockito.mock(MotionEvent::class.java)
        val mockCheck = Mockito.mock(BuildVersionChecker::class.java)
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        When calling mockCheck.isAndroidQAndAbove() itReturns true
        When calling mockEvent.actionMasked itReturns MotionEvent.ACTION_CANCEL
        listener.onTouch(mockView, mockEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with action up`() {
        val mockView = Mockito.mock(View::class.java)
        val mockEvent = Mockito.mock(MotionEvent::class.java)
        val mockCheck = Mockito.mock(BuildVersionChecker::class.java)
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        When calling mockCheck.isAndroidQAndAbove() itReturns true
        When calling mockEvent.actionMasked itReturns MotionEvent.ACTION_UP
        listener.onTouch(mockView, mockEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with other action`() {
        val mockView = Mockito.mock(View::class.java)
        val mockEvent = Mockito.mock(MotionEvent::class.java)
        val mockCheck = Mockito.mock(BuildVersionChecker::class.java)
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        When calling mockCheck.isAndroidQAndAbove() itReturns true
        When calling mockEvent.actionMasked itReturns MotionEvent.ACTION_OUTSIDE

        listener.onTouch(mockView, mockEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with action move and null magnifier`() {
        val mockView = Mockito.mock(View::class.java)
        val mockEvent = Mockito.mock(MotionEvent::class.java)
        val mockCheck = Mockito.mock(BuildVersionChecker::class.java)

        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        When calling mockCheck.isAndroidQAndAbove() itReturns true
        When calling mockEvent.actionMasked itReturns MotionEvent.ACTION_MOVE
        listener.onTouch(mockView, mockEvent).shouldBeTrue()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return true on touch with action cancel and null magnifier`() {
        val mockView = Mockito.mock(View::class.java)
        val mockEvent = Mockito.mock(MotionEvent::class.java)
        val mockCheck = Mockito.mock(BuildVersionChecker::class.java)
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        When calling mockCheck.isAndroidQAndAbove() itReturns true
        When calling mockEvent.actionMasked itReturns MotionEvent.ACTION_CANCEL
        listener.onTouch(mockView, mockEvent).shouldBeTrue()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return true on touch with action up and null magnifier`() {
        val mockView = Mockito.mock(View::class.java)
        val mockEvent = Mockito.mock(MotionEvent::class.java)
        val mockCheck = Mockito.mock(BuildVersionChecker::class.java)
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        When calling mockCheck.isAndroidQAndAbove() itReturns true
        When calling mockEvent.actionMasked itReturns MotionEvent.ACTION_UP
        listener.onTouch(mockView, mockEvent).shouldBeTrue()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return true on touch with other action and null magnifier`() {
        val mockView = Mockito.mock(View::class.java)
        val mockEvent = Mockito.mock(MotionEvent::class.java)
        val mockCheck = Mockito.mock(BuildVersionChecker::class.java)
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        When calling mockCheck.isAndroidQAndAbove() itReturns true
        When calling mockEvent.actionMasked itReturns MotionEvent.ACTION_OUTSIDE

        listener.onTouch(mockView, mockEvent).shouldBeTrue()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return false for lower android version`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true))

        val mockView = Mockito.mock(View::class.java)
        val mockEvent = Mockito.mock(motionEvent::class.java)

        listener.onTouch(mockView, mockEvent).shouldBeFalse()
    }

    @Test
        fun `should return true with back button click`() {
        val mockDisplayManager = Mockito.mock(DisplayManager::class.java)
        val mockCoroutine = Mockito.mock(MessageActionsCoroutine::class.java)

        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)
        val mockView = Mockito.mock(CheckBox::class.java)

        When calling keyEvent.action itReturns KeyEvent.ACTION_UP
        When calling mockView.id itReturns R.id.message_close_button
        When calling mockCoroutine.executeTask(message,
                MessageActionsCoroutine.BACK_BUTTON, false) itReturns true

        listener.onKey(mockView, KeyEvent.KEYCODE_BACK, keyEvent).shouldBeTrue()
    }

    @Test
    fun `should return false with back button action down`() {
        val mockDisplayManager = Mockito.mock(DisplayManager::class.java)
        val mockCoroutine = Mockito.mock(MessageActionsCoroutine::class.java)

        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)
        val mockView = Mockito.mock(CheckBox::class.java)

        When calling keyEvent.action itReturns KeyEvent.ACTION_DOWN
        When calling mockView.id itReturns R.id.message_close_button
        When calling mockCoroutine.executeTask(message,
                MessageActionsCoroutine.BACK_BUTTON, false) itReturns true

        listener.onKey(mockView, KeyEvent.KEYCODE_BACK, keyEvent).shouldBeFalse()
    }

    @Test
    fun `should return false with null event`() {
        val mockDisplayManager = Mockito.mock(DisplayManager::class.java)
        val mockCoroutine = Mockito.mock(MessageActionsCoroutine::class.java)

        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)
        val mockView = Mockito.mock(CheckBox::class.java)

        listener.onKey(mockView, KeyEvent.KEYCODE_BACK, null).shouldBeFalse()
    }

    @Test
    fun `should return false with null event and not back button`() {
        val mockDisplayManager = Mockito.mock(DisplayManager::class.java)
        val mockCoroutine = Mockito.mock(MessageActionsCoroutine::class.java)

        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)
        val mockView = Mockito.mock(CheckBox::class.java)

        listener.onKey(mockView, KeyEvent.KEYCODE_BREAK, null).shouldBeFalse()
    }

    @Test
    fun `should return false with not back button`() {
        val mockDisplayManager = Mockito.mock(DisplayManager::class.java)
        val mockCoroutine = Mockito.mock(MessageActionsCoroutine::class.java)

        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)
        val mockView = Mockito.mock(CheckBox::class.java)
        When calling keyEvent.action itReturns KeyEvent.ACTION_UP

        listener.onKey(mockView, KeyEvent.KEYCODE_BREAK, keyEvent).shouldBeFalse()
    }

    @Test
    fun `should return false with not back button and action down`() {
        val mockDisplayManager = Mockito.mock(DisplayManager::class.java)
        val mockCoroutine = Mockito.mock(MessageActionsCoroutine::class.java)

        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)
        val mockView = Mockito.mock(CheckBox::class.java)
        When calling keyEvent.action itReturns KeyEvent.ACTION_DOWN

        listener.onKey(mockView, KeyEvent.KEYCODE_BREAK, keyEvent).shouldBeFalse()
    }
}
