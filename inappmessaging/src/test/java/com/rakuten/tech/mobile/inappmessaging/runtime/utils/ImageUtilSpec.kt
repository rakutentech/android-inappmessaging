package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doAnswer
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class ImageUtilSpec {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val callback = Mockito.mock(Callback::class.java)

    @Test
    fun `should call error for invalid url`() {
        IS_VALID = false
        verifyFetch(false)
    }

    @Test
    fun `should call error will null exception for invalid url`() {
        IS_VALID = false
        IS_NULL = true
        verifyFetch(false)
    }

    @Test
    fun `should call success for valid url`() {
        IS_VALID = true
        verifyFetch(true)
    }

    @Test
    fun `should not throw exception when using valid picasso`() {
        setupValidPicasso()
        ImageUtil.fetchImage("any url", callback, context)
    }

    private fun verifyFetch(isValid: Boolean) {
        ImageUtil.fetchImage("any url", callback, context, setupMockPicasso())
        if (isValid) {
            Mockito.verify(callback).onSuccess()
        } else {
            Mockito.verify(callback).onError(anyOrNull())
        }
    }

    companion object {
        internal var IS_VALID = false
        internal var IS_NULL = false

        @SuppressWarnings("SwallowedException")
        internal fun setupValidPicasso() {
            val picasso = Picasso.Builder(ApplicationProvider.getApplicationContext()).build()
            try {
                Picasso.setSingletonInstance(picasso)
            } catch (ex: Exception) {
                // ignore
            }
        }

        @SuppressWarnings("LongMethod")
        internal fun setupMockPicasso(isException: Boolean = false): Picasso {
            val picasso = Mockito.mock(Picasso::class.java)
            val requestCreator = Mockito.mock(RequestCreator::class.java)
            `when`(picasso.load(ArgumentMatchers.anyString())).thenReturn(requestCreator)
            `when`(requestCreator.priority(any())).thenReturn(requestCreator)
            `when`(requestCreator.resize(any(), any())).thenReturn(requestCreator)
            `when`(requestCreator.onlyScaleDown()).thenReturn(requestCreator)
            `when`(requestCreator.centerInside()).thenReturn(requestCreator)
            if (isException) {
                `when`(requestCreator.into(any(), any())).thenThrow(NullPointerException("test error"))
            } else {
                doAnswer {
                    setCallback(it.getArgument(0))
                }.`when`(requestCreator).fetch(any())
                doAnswer {
                    setCallback(it.getArgument(1))
                }.`when`(requestCreator).into(any(), any())
            }

            return picasso
        }

        private fun setCallback(callback: Callback) {
            when {
                IS_VALID -> callback.onSuccess()
                IS_NULL -> callback.onError(null)
                else -> callback.onError(IllegalArgumentException("error test"))
            }
        }
    }
}
