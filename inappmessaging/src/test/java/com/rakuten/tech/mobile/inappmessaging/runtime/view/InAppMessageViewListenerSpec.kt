package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.app.Activity
import android.os.Build
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.Magnifier
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.coroutine.MessageActionsCoroutine
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.DisplaySettings
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageSettings
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
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
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
open class InAppMessageViewListenerSpec : BaseTest() {
    internal val mockView = Mockito.mock(View::class.java)
    internal val mockDisplayManager = Mockito.mock(DisplayManager::class.java)
    internal val mockCoroutine = Mockito.mock(MessageActionsCoroutine::class.java)
    internal val mockEventScheduler = Mockito.mock(EventMessageReconciliationScheduler::class.java)
    internal val mockActivity = Mockito.mock(Activity::class.java)
    internal val mockInApp = Mockito.mock(InAppMessaging::class.java)

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    override fun setup() {
        super.setup()
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }
}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.Q])
class InAppMessageViewListenerOnClickSpec : InAppMessageViewListenerSpec() {
    private val mockCheckbox = Mockito.mock(CheckBox::class.java)

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
        `when`(mockCoroutine.executeTask(message, R.id.message_close_button, false)).thenReturn(true)
        `when`(mockInApp.getRegisteredActivity()).thenReturn(mockActivity)

        listener.onClick(mockView)
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

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.Q])
class InAppMessageViewListenerOnTouchSpec : InAppMessageViewListenerSpec() {
    private val mockMotionEvent = Mockito.mock(MotionEvent::class.java)
    private val mockCheck = Mockito.mock(BuildVersionChecker::class.java)

    @Before
    override fun setup() {
        super.setup()
        `when`(mockMotionEvent!!.rawX).thenReturn(0f)
        `when`(mockMotionEvent.rawY).thenReturn(0f)
    }

    @Test
    fun `should return true on touch with action down and onClick listener`() {
        val listener = setupListener(MotionEvent.ACTION_DOWN)

        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with action move and onClick listener`() {
        val listener = setupListener(MotionEvent.ACTION_MOVE)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with action cancel and onClick listener`() {
        val listener = setupListener(MotionEvent.ACTION_CANCEL)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with action up and onClick listener`() {
        val listener = setupListener(MotionEvent.ACTION_UP)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with other action and onClick listener`() {
        val listener = setupListener(MotionEvent.ACTION_OUTSIDE)

        listener.magnifier = Mockito.mock(Magnifier::class.java)
        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
    }

    @Test
    fun `should return true on touch with action move, null magnifier, and onClick listener`() {
        val listener = setupListener(MotionEvent.ACTION_MOVE)

        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return true on touch with action cancel, null magnifier, and onClick listener`() {
        val listener = setupListener(MotionEvent.ACTION_CANCEL)

        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return true on touch with action up, null magnifier, and onClick listener`() {
        val listener = setupListener(MotionEvent.ACTION_UP)

        listener.onTouch(mockView, mockMotionEvent).shouldBeTrue()
        listener.magnifier.shouldBeNull()
    }

    @Test
    fun `should return true on touch with other action, null magnifier, and onClick listener`() {
        val listener = setupListener(MotionEvent.ACTION_OUTSIDE)

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

    private fun setupListener(action: Int): InAppMessageViewListener {
        val listener = InAppMessageViewListener(ValidTestMessage("1", true), buildChecker = mockCheck)

        `when`(mockCheck.isAndroidQAndAbove()).thenReturn(true)
        `when`(mockMotionEvent.actionMasked).thenReturn(action)
        `when`(mockView.performClick()).thenReturn(true)
        return listener
    }
}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.Q])
class InAppMessageViewListenerOnKeySpec : InAppMessageViewListenerSpec() {

    private val keyEvent = Mockito.mock(KeyEvent::class.java)

    @Test
    fun `should return true with back button click`() {
        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)

        `when`(keyEvent.action).thenReturn(KeyEvent.ACTION_UP)
        `when`(mockView.id).thenReturn(R.id.message_close_button)
        `when`(
            mockCoroutine.executeTask(
                message,
                MessageActionsCoroutine.BACK_BUTTON, false
            )
        ).thenReturn(true)

        listener.onKey(mockView, KeyEvent.KEYCODE_BACK, keyEvent).shouldBeTrue()
    }

    @Test
    fun `should return false with back button click`() {
        val message = ValidTestMessage("1", true, isCampaignDismissable = false)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)

        `when`(keyEvent.action).thenReturn(KeyEvent.ACTION_UP)
        `when`(mockView.id).thenReturn(R.id.message_close_button)
        `when`(
            mockCoroutine.executeTask(
                message,
                MessageActionsCoroutine.BACK_BUTTON, false
            )
        ).thenReturn(true)

        listener.onKey(mockView, KeyEvent.KEYCODE_BACK, keyEvent).shouldBeFalse()
    }

    @Test
    fun `should return false with back button action down`() {
        val message = ValidTestMessage("1", true)
        val listener = InAppMessageViewListener(message, mockCoroutine, mockDisplayManager)

        `when`(keyEvent.action).thenReturn(KeyEvent.ACTION_DOWN)
        `when`(mockView.id).thenReturn(R.id.message_close_button)
        `when`(
            mockCoroutine.executeTask(
                message,
                MessageActionsCoroutine.BACK_BUTTON, false
            )
        ).thenReturn(true)

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
        val listener = InAppMessageViewListener(
            message = message,
            messageCoroutine = mockCoroutine,
            displayManager = mockDisplayManager,
            eventScheduler = mockEventScheduler,
            inApp = mockInApp
        )

        `when`(keyEvent.action).thenReturn(KeyEvent.ACTION_UP)
        `when`(mockView.id).thenReturn(R.id.message_close_button)
        `when`(mockCoroutine.executeTask(message, MessageActionsCoroutine.BACK_BUTTON, false)).thenReturn(false)
        `when`(mockInApp.getRegisteredActivity()).thenReturn(mockActivity)

        listener.onKey(mockView, KeyEvent.KEYCODE_BACK, keyEvent).shouldBeTrue()

        Mockito.verify(mockDisplayManager).removeMessage(any(), any(), any(), any())
    }
}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class InAppMessageViewListenerHandleSpec : InAppMessageViewListenerSpec() {
    private val mockMessage = Mockito.mock(Message::class.java)
    private val mockPayload = Mockito.mock(MessagePayload::class.java)
    private val mockSettings = Mockito.mock(MessageSettings::class.java)
    private val mockDispSettings = Mockito.mock(DisplaySettings::class.java)
    private val instance = createMockListener(mockMessage)

    @Test
    fun `should start worker with zero delay due to null value`() {
        `when`(mockCoroutine.executeTask(mockMessage, R.id.message_close_button, false)).thenReturn(true)
        `when`(mockMessage.getMessagePayload()).thenReturn(mockPayload)
        `when`(mockPayload.messageSettings).thenReturn(mockSettings)
        `when`(mockSettings.displaySettings).thenReturn(Mockito.mock(DisplaySettings::class.java))
        instance.handleMessage(R.id.message_close_button)
        Mockito.verify(mockEventScheduler).startReconciliationWorker(anyOrNull(), eq(0L))
    }

    @Test
    fun `should start worker with valid delay due to null values`() {
        `when`(mockCoroutine.executeTask(mockMessage, R.id.message_close_button, false)).thenReturn(true)
        `when`(mockMessage.getMessagePayload()).thenReturn(mockPayload)
        `when`(mockPayload.messageSettings).thenReturn(mockSettings)
        `when`(mockSettings.displaySettings).thenReturn(mockDispSettings)
        `when`(mockDispSettings.delay).thenReturn(3000)
        instance.handleMessage(R.id.message_close_button)
        Mockito.verify(mockEventScheduler).startReconciliationWorker(anyOrNull(), eq(3000L))
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
