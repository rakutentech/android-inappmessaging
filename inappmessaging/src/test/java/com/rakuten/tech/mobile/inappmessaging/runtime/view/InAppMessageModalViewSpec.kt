
package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson.MessageMapper
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Resource
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InAppMessageModalViewSpec {

    private val view = spy(InAppMessageModalView(ApplicationProvider.getApplicationContext(), null))
    private val mockModal = mock(LinearLayout::class.java)

    @Before
    fun setup() {
        doReturn(mockModal).`when`(view).findModalLayout()
    }

    @Test
    fun `should call setBackgroundColor`() {
        doReturn(null).`when`(view).findViewById<Button>(anyInt())
        doReturn(null).`when`(view).findViewById<CheckBox>(anyInt())

        view.populateViewData(
            MessageMapper.mapFrom(
                TestDataHelper.createDummyMessage(
                    messagePayload = TestDataHelper.createDummyPayload(
                        header = "",
                        messageBody = "",
                        resource = Resource(imageUrl = "", cropType = 0),
                    ),
                ),
            ),
        )

        verify(mockModal).setBackgroundColor(anyInt())
    }

    @Test
    fun `should not call setBackgroundColor when modal layout is null`() {
        doReturn(null).`when`(view).findModalLayout()

        view.populateViewData(
            MessageMapper.mapFrom(
                TestDataHelper.createDummyMessage(),
            ),
        )

        verify(view, never()).setBackgroundColor(anyInt())
    }

    @Test
    fun `should not call setBackgroundColor when opacity is invalid`() {
        view.populateViewData(MessageMapper.mapFrom(TestDataHelper.createDummyMessage()).copy(backdropOpacity = -1f))
        verify(view, never()).setBackgroundColor(anyInt())

        view.populateViewData(MessageMapper.mapFrom(TestDataHelper.createDummyMessage()).copy(backdropOpacity = 1.5f))
        verify(view, never()).setBackgroundColor(anyInt())
    }

    @Test
    fun `should call setBackgroundColor when opacity is valid`() {
        view.populateViewData(MessageMapper.mapFrom(TestDataHelper.createDummyMessage()).copy(backdropOpacity = 0.3f))
        verify(view).setBackgroundColor(-1728053248)
    }
}
