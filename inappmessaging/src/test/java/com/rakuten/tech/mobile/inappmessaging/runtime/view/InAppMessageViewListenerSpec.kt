package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.app.Activity
import android.os.Build
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.Magnifier
import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.coroutine.MessageActionsCoroutine
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.DisplaySettings
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageSettings
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.annotation.Config

/**
 * Test class for InAppMessageViewListener.
 */
open class InAppMessageViewListenerSpec : BaseTest() {
    internal val mockView = Mockito.mock(View::class.java)
    internal val mockDisplayManager = Mockito.mock(DisplayManager::class.java)
    internal val mockCoroutine = Mockito.mock(MessageActionsCoroutine::class.java)
    internal val mockEventScheduler = Mockito.mock(EventMessageReconciliationScheduler::class.java)
    internal val mockActivity = Mockito.mock(Activity::class.java)
    internal val mockInApp = Mockito.mock(InAppMessaging::class.java)
    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    @Before
    override fun setup() {
        super.setup()
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    @After
    override fun tearDown() {
        super.tearDown()
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }
}

@Config(sdk = [Build.VERSION_CODES.Q])
class InAppMessageViewListenerOnClickSpec : InAppMessageViewListenerSpec() {
    private val mockCheckbox = Mockito.mock(CheckBox::class.java)
    private val mockMessage = Mockito.mock(Message::class.java)
    private val mockPayload = Mockito.mock(MessagePayload::class.java)
    private val mockSettings = Mockito.mock(MessageSettings::class.java)
    private val mockDispSettings = Mockito.mock(DisplaySettings::class.java)
    private val instance = createMockListener(mockMessage)

    @Test
    fun `should call is checked once`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true))
        `when`(mockCheckbox.id).thenReturn(R.id.opt_out_checkbox)
        `when`(mockCheckbox.isChecked).thenReturn(true)
        listener.onClick(mockCheckbox)

        Mockito.verify(mockCheckbox).isChecked
    }

    @Test
    fun `should not throw exception with non-checkbox click`() {
        val message = ValidTestMessage("1", true)
        val listener = createMockListener(message)
        val mockView = Mockito.mock(CheckBox::class.java)
        `when`(mockView.id).thenReturn(R.id.message_close_button)
        `when`(mockCoroutine.executeTask(message, ImpressionType.EXIT, false)).thenReturn(true)
        `when`(mockInApp.getRegisteredActivity()).thenReturn(mockActivity)

        listener.onClick(mockView)
    }

//    @Test
//    fun `should start worker with zero delay due to null values`() {
//        `when`(mockCoroutine.executeTask(mockMessage, ImpressionType.EXIT, false)).thenReturn(true)
//        `when`(mockMessage.getMessagePayload()).thenReturn(null)
//        instance.handleMessage(ImpressionType.EXIT)
//        Mockito.verify(mockEventScheduler).startEventMessageReconciliationWorker(anyOrNull(), eq(0L))
//
//        `when`(mockMessage.getMessagePayload()).thenReturn(mockPayload)
//        `when`(mockPayload.messageSettings).thenReturn(null)
//        instance.handleMessage(ImpressionType.EXIT)
//        Mockito.verify(mockEventScheduler, times(2)).startEventMessageReconciliationWorker(anyOrNull(), eq(0L))
//
//        `when`(mockPayload.messageSettings).thenReturn(mockSettings)
//        `when`(mockSettings.displaySettings).thenReturn(Mockito.mock(DisplaySettings::class.java))
//        instance.handleMessage(ImpressionType.EXIT)
//        Mockito.verify(mockEventScheduler, times(3)).startEventMessageReconciliationWorker(anyOrNull(), eq(0L))
//    }

    @Test
    fun `should start worker with valid delay due to null values`() {
        `when`(mockCoroutine.executeTask(mockMessage, ImpressionType.EXIT, false)).thenReturn(true)
        `when`(mockMessage.getMessagePayload()).thenReturn(mockPayload)
        `when`(mockPayload.messageSettings).thenReturn(mockSettings)
        `when`(mockSettings.displaySettings).thenReturn(mockDispSettings)
        `when`(mockDispSettings.delay).thenReturn(3000)
        instance.handleMessage(ImpressionType.EXIT)
        Mockito.verify(mockEventScheduler).startEventMessageReconciliationWorker(anyOrNull(), eq(3000L))
    }

    @Test
    fun `should use 0 delay for null url`() {
        val tooltip = Tooltip("target", "top-center")
        val message = ValidTestMessage("1", type = InAppMessageType.TOOLTIP.typeId, tooltip = tooltip)
        verifyDisplayManagerCall(message, R.id.message_tooltip_image_view)
    }

    @Test
    fun `should use 0 delay for null config`() {
        val message = ValidTestMessage("1", type = InAppMessageType.TOOLTIP.typeId)
        verifyDisplayManagerCall(message, R.id.message_tooltip_image_view)
    }

    @Test
    fun `should use 0 delay for normal campaign`() {
        val message = ValidTestMessage("1")
        verifyDisplayManagerCall(message, R.id.message_tooltip_image_view, isNullId = true)
    }

    @Test
    fun `should use delay for non content`() {
        val tooltip = Tooltip("target", "top-center", url = "testurl", autoDisappear = 5)
        val message = ValidTestMessage("1", type = InAppMessageType.TOOLTIP.typeId, tooltip = tooltip)
        verifyDisplayManagerCall(message, R.id.message_close_button, 5)
    }

    @Test
    fun `should use 0 delay for non content and null config`() {
        val message = ValidTestMessage("1", type = InAppMessageType.TOOLTIP.typeId)
        verifyDisplayManagerCall(message, R.id.message_close_button)
    }

    private fun verifyDisplayManagerCall(
        message: ValidTestMessage,
        id: Int,
        delay: Int = 0,
        isNullId: Boolean = false
    ) {
        val listener = createMockListener(message)
        runBlocking {
            listener.handleClick(id, Dispatchers.Unconfined, Dispatchers.Unconfined)
        }
        if (isNullId) {
            Mockito.verify(mockDisplayManager).removeMessage(anyOrNull(), any(), eq(delay), isNull())
        } else {
            Mockito.verify(mockDisplayManager).removeMessage(anyOrNull(), any(), eq(delay), eq("1"))
        }
    }

    private fun createMockListener(message: Message) =
        InAppMessageViewListener(
            message = message,
            messageCoroutine = mockCoroutine,
            displayManager = mockDisplayManager,
            eventScheduler = mockEventScheduler,
            inApp = mockInApp
        )
}

@Config(sdk = [Build.VERSION_CODES.Q])
@SuppressWarnings("LargeClass")
class InAppMessageViewListenerOnTouchSpec : InAppMessageViewListenerSpec() {
    private val mockMotionEvent = Mockito.mock(MotionEvent::class.java)
    private val mockCheck = Mockito.mock(BuildVersionChecker::class.java)

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    @Before
    override fun setup() {
        super.setup()
        `when`(mockMotionEvent!!.rawX).thenReturn(0f)
        `when`(mockMotionEvent.rawY).thenReturn(0f)
    }

    @Test
    fun `should return true on touch with action down and onClick listener`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_DOWN)
        `when`(mockView.performClick()).thenReturn(true)

        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with action move and onClick listener`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_MOVE)
        `when`(mockView.performClick()).thenReturn(true)

        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with action cancel and onClick listener`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_CANCEL)
        `when`(mockView.performClick()).thenReturn(true)

        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with action up and onClick listener`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_UP)
        `when`(mockView.performClick()).thenReturn(true)

        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with other action and onClick listener`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_OUTSIDE)
        `when`(mockView.performClick()).thenReturn(true)

        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with action move, null magnifier, and onClick listener`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_MOVE)
        `when`(mockView.performClick()).thenReturn(true)

        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return true on touch with action cancel, null magnifier, and onClick listener`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_CANCEL)
        `when`(mockView.performClick()).thenReturn(true)

        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return true on touch with action up, null magnifier, and onClick listener`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_UP)
        `when`(mockView.performClick()).thenReturn(true)

        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return true on touch with other action, null magnifier, and onClick listener`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_OUTSIDE)
        `when`(mockView.performClick()).thenReturn(true)

        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return true on touch with action down`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_DOWN)

        listener.onTouch(mockView, mockMotionEvent).shouldBeFalse()
    }

    @Test
    fun `should return true on touch with action move`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_MOVE)
        listener.onTouch(mockView, mockMotionEvent).shouldBeFalse()
    }

    @Test
    fun `should return true on touch with action cancel`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_CANCEL)
        listener.onTouch(mockView, mockMotionEvent).shouldBeFalse()
    }

    @Test
    fun `should return true on touch with action up`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_UP)
        listener.onTouch(mockView, mockMotionEvent).shouldBeFalse()
    }

    @Test
    fun `should return true on touch with other action`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_OUTSIDE)

        listener.onTouch(mockView, mockMotionEvent).shouldBeFalse()
    }

    @Test
    fun `should return true on touch with action move and null magnifier`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_MOVE)
        listener.onTouch(mockView, mockMotionEvent).shouldBeFalse()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return true on touch with action cancel and null magnifier`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_CANCEL)
        listener.onTouch(mockView, mockMotionEvent).shouldBeFalse()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return true on touch with action up and null magnifier`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_UP)
        listener.onTouch(mockView, mockMotionEvent).shouldBeFalse()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return true on touch with other action and null magnifier`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_OUTSIDE)

        listener.onTouch(mockView, mockMotionEvent).shouldBeFalse()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return false for lower android version`() {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true))

        listener.onTouch(mockView, mockMotionEvent).shouldBeFalse()
    }
}

@Config(sdk = [Build.VERSION_CODES.Q])
class InAppMessageViewListenerOnKeySpec : InAppMessageViewListenerSpec() {

    private val keyEvent = Mockito.mock(KeyEvent::class.java)

    @Test
    fun `should return true with back button click`() {
        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)

        `when`(keyEvent.action).thenReturn(KeyEvent.ACTION_UP)
        `when`(mockView.id).thenReturn(R.id.message_close_button)
        `when`(mockCoroutine.executeTask(message,
                ImpressionType.EXIT, false)).thenReturn(true)

        listener.onKey(mockView, KeyEvent.KEYCODE_BACK, keyEvent).shouldBeTrue()
    }

    @Test
    fun `should return false with back button action down`() {
        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)

        `when`(keyEvent.action).thenReturn(KeyEvent.ACTION_DOWN)
        `when`(mockView.id).thenReturn(R.id.message_close_button)
        `when`(mockCoroutine.executeTask(message,
                ImpressionType.EXIT, false)).thenReturn(true)

        listener.onKey(mockView, KeyEvent.KEYCODE_BACK, keyEvent).shouldBeFalse()
    }

    @Test
    fun `should return false with null event`() {
        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)

        listener.onKey(mockView, KeyEvent.KEYCODE_BACK, null).shouldBeFalse()
    }

    @Test
    fun `should return false with null event and not back button`() {
        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)

        listener.onKey(mockView, KeyEvent.KEYCODE_BREAK, null).shouldBeFalse()
    }

    @Test
    fun `should return false with not back button`() {
        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)
        `when`(keyEvent.action).thenReturn(KeyEvent.ACTION_UP)

        listener.onKey(mockView, KeyEvent.KEYCODE_BREAK, keyEvent).shouldBeFalse()
    }

    @Test
    fun `should return false with not back button and action down`() {
        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)
        `when`(keyEvent.action).thenReturn(KeyEvent.ACTION_DOWN)

        listener.onKey(mockView, KeyEvent.KEYCODE_BREAK, keyEvent).shouldBeFalse()
    }

//    @Test
    fun `should return true on false coroutine`() {
        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message = message,
                messageCoroutine = mockCoroutine,
                displayManager = mockDisplayManager,
                eventScheduler = mockEventScheduler,
                inApp = mockInApp)

        `when`(keyEvent.action).thenReturn(KeyEvent.ACTION_UP)
        `when`(mockView.id).thenReturn(R.id.message_close_button)
        `when`(mockCoroutine.executeTask(message,
                ImpressionType.EXIT, false)).thenReturn(false)
        `when`(mockInApp.getRegisteredActivity()).thenReturn(mockActivity)

        listener.onKey(mockView, KeyEvent.KEYCODE_BACK, keyEvent).shouldBeTrue()

        Mockito.verify(mockDisplayManager).removeMessage(any())
    }
}
