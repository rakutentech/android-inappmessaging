package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.ScrollView
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import org.amshove.kluent.shouldBeFalse
import org.junit.Test
import org.mockito.Mockito
import org.mockito.verification.VerificationMode
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.M])
class CustomOnTouchListenerSpec {

    private val mockMotionEvent = Mockito.mock(MotionEvent::class.java)
    private val mockDisplay = Mockito.mock(DisplayManager::class.java)
    private val mockView = Mockito.mock(View::class.java)
    private val mockScroll = Mockito.mock(ScrollView::class.java)
    private val listener = object : CustomOnTouchListener() {}

    @Test
    fun `should not call display manager when action down`() {
        Mockito.`when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_DOWN)
        verifyDisplayManager(mockView, never())
    }

    @Test
    fun `should not call display manager when action move`() {
        Mockito.`when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_MOVE)
        verifyDisplayManager(mockView, never())
    }

    @Test
    fun `should not call display manager when action cancel and not scroll`() {
        Mockito.`when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_CANCEL)
        verifyDisplayManager(mockView, never())
    }

    @Test
    fun `should not call display manager when action up and not scroll`() {
        Mockito.`when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_UP)
        verifyDisplayManager(mockView, never())
    }

    @Test
    fun `should not call display manager when action cancel and scroll`() {
        Mockito.`when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_CANCEL)
        verifyDisplayManager(mockScroll, times(1))
    }

    @Test
    fun `should not call display manager when action up and scroll`() {
        Mockito.`when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_UP)
        verifyDisplayManager(mockScroll, times(1))
    }

    @Test
    fun `should not crash for non-mock display manager`() {
        Mockito.`when`(mockMotionEvent.actionMasked).thenReturn(MotionEvent.ACTION_UP)
        listener.onTouch(mockScroll, mockMotionEvent).shouldBeFalse()
    }

    private fun verifyDisplayManager(view: View, mode: VerificationMode) {
        listener.displayManager = mockDisplay
        listener.onTouch(view, mockMotionEvent).shouldBeFalse()
        verify(mockDisplay, mode).removeHiddenTargets(any())
        verify(mockDisplay, mode).displayMessage()
    }
}
