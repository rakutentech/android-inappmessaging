package com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

internal object MockPicasso {
    @SuppressWarnings("LongMethod")
    fun init(returnType: MockPicassoReturnType): Picasso {
        val picasso = mock(Picasso::class.java)
        val requestCreator = mock(RequestCreator::class.java)
        `when`(picasso.load(ArgumentMatchers.anyString())).thenReturn(requestCreator)
        `when`(requestCreator.priority(any())).thenReturn(requestCreator)
        `when`(requestCreator.resize(any(), any())).thenReturn(requestCreator)
        `when`(requestCreator.onlyScaleDown()).thenReturn(requestCreator)
        `when`(requestCreator.centerInside()).thenReturn(requestCreator)

        when (returnType) {
            MockPicassoReturnType.GENERIC_EXCEPTION ->
                `when`(requestCreator.into(any(), any())).thenThrow(NullPointerException("test error"))
            MockPicassoReturnType.CALLBACK_ERROR ->
                doAnswer { setCallback(it.getArgument(1), returnType) }.`when`(requestCreator).into(any(), any())
            else -> {
                doAnswer { setCallback(it.getArgument(0)) }.`when`(requestCreator).fetch(any())
                doAnswer { setCallback(it.getArgument(1), returnType) }.`when`(requestCreator).into(any(), any())
            }
        }
        return picasso
    }

    private fun setCallback(callback: Callback, returnType: MockPicassoReturnType? = null) {
        if (returnType == MockPicassoReturnType.CALLBACK_ERROR) {
            callback.onError(null)
        } else {
            callback.onSuccess()
        }
    }
}

internal enum class MockPicassoReturnType {
    GENERIC_EXCEPTION,
    CALLBACK_SUCCESS,
    CALLBACK_ERROR
}
