package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.graphics.Rect
import android.view.View
import com.nhaarman.mockitokotlin2.any
import com.rakuten.tech.mobile.inappmessaging.runtime.extensions.isVisible
import com.rakuten.tech.mobile.inappmessaging.runtime.extensions.show
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ViewExtensionsSpec {
    private val mockView = Mockito.mock(View::class.java)

    @Test
    fun `isVisible() should return false when isShown is false`() {
        `when`(mockView.isShown).thenReturn(false)

        mockView.isVisible().shouldBeFalse()
    }

    @Test
    fun `isVisible() should return true when visible`() {
        `when`(mockView.isShown).thenReturn(true)
        `when`(mockView.getGlobalVisibleRect(any())).thenReturn(true)

        mockView.isVisible(Rect(0, 0, 10, 10)).shouldBeTrue()
    }

    @Test
    fun `isVisible() should return true when not visible`() {
        `when`(mockView.isShown).thenReturn(true)
        `when`(mockView.getGlobalVisibleRect(any())).thenReturn(false)

        mockView.isVisible().shouldBeFalse()
    }

    @Test
    fun `show() should set visibility to VISIBLE`() {
        mockView.show().shouldBeEqualTo(View.VISIBLE)
    }

    @Test
    fun `show() should set visibility to INVISIBLE`() {
        mockView.show(false).shouldBeEqualTo(View.INVISIBLE)
    }
}