package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.button.MaterialButton
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ImageUtilSpec
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import android.R.attr.button
import android.widget.CheckBox
import android.R.attr.button

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class BaseViewSpec : BaseTest() {
    private val hostAppActivity = Mockito.mock(Activity::class.java)
    private val mockMessage = Mockito.mock(Message::class.java)
    private val mockPayload = Mockito.mock(MessagePayload::class.java)
    private val mockSettings = Mockito.mock(MessageSettings::class.java)
    private val mockCtrlSettings = Mockito.mock(ControlSettings::class.java)
    private val mockDisplaySettings = Mockito.mock(DisplaySettings::class.java)
    private val mockResource = Mockito.mock(Resource::class.java)
    private val mockBtn = Mockito.mock(MessageButton::class.java)
    private var view: InAppMessageBaseView? = null

    @Before
    override fun setup() {
        super.setup()
        `when`(
            hostAppActivity
                .layoutInflater
        ).thenReturn(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
        `when`(mockMessage.getMessagePayload()).thenReturn(mockPayload)
        `when`(mockPayload.header).thenReturn("test")
        `when`(mockPayload.messageBody).thenReturn("test")
        `when`(mockPayload.messageSettings).thenReturn(mockSettings)
        `when`(mockSettings.controlSettings).thenReturn(mockCtrlSettings)
        `when`(mockSettings.displaySettings).thenReturn(mockDisplaySettings)
        `when`(mockPayload.resource).thenReturn(mockResource)
        view = hostAppActivity
            .layoutInflater
            .inflate(R.layout.in_app_message_full_screen, null) as InAppMessageBaseView
    }

    @Test
    fun `should set default when invalid header color`() {
        `when`(mockPayload.headerColor).thenReturn("invalid")
        view?.populateViewData(mockMessage)

        verifyDefault()
    }

    @Test
    fun `should set default when invalid body color`() {
        `when`(mockPayload.headerColor).thenReturn(WHITE_HEX)
        `when`(mockPayload.messageBodyColor).thenReturn("incorrect")
        view?.populateViewData(mockMessage)

        verifyDefault()
    }

    @Test
    fun `should set default when invalid bg color`() {
        `when`(mockPayload.headerColor).thenReturn(WHITE_HEX)
        `when`(mockPayload.messageBodyColor).thenReturn(WHITE_HEX)
        `when`(mockPayload.backgroundColor).thenReturn("failed")
        view?.populateViewData(mockMessage)

        verifyDefault()
    }

    @Test
    fun `should set default when invalid button text and bg color`() {
        `when`(mockPayload.headerColor).thenReturn("invalid")
        `when`(mockCtrlSettings.buttons).thenReturn(listOf(mockBtn))
        `when`(mockBtn.buttonTextColor).thenReturn("test")
        `when`(mockBtn.buttonTextColor).thenReturn("#")
        `when`(mockBtn.buttonBackgroundColor).thenReturn("#")
        view?.populateViewData(mockMessage)
        val button = view?.findViewById<MaterialButton>(R.id.message_single_button)
        button?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.parseColor("#1D1D1D"))
        button?.backgroundTintList shouldBeEqualTo ColorStateList.valueOf(Color.WHITE)
    }

    @Test
    fun `should set checkbox to visible`() {
        `when`(mockPayload.headerColor).thenReturn("#")
        `when`(mockDisplaySettings.optOut).thenReturn(true)
        view?.populateViewData(mockMessage)

        view?.findViewById<CheckBox>(R.id.opt_out_checkbox)?.visibility shouldBeEqualTo View.VISIBLE
    }

    @Test
    fun `should display image`() {
        verifyImageFetch(true)
    }

    @Test
    fun `should not display image with invalid url`() {
        verifyImageFetch(false)
    }

    @Test
    fun `should not display image with invalid url with null error`() {
        verifyImageFetch(false, isNull = true)
    }

    @Test
    fun `should not throw null pointer`() {
        verifyImageFetch(isValid = false, isException = true)
    }

    @Test
    fun `should not throw exception when valid picasso`() {
        `when`(mockResource.imageUrl).thenReturn("test valid URL")
        `when`(mockPayload.headerColor).thenReturn("#")
        ImageUtilSpec.setupValidPicasso()
        view?.populateViewData(mockMessage)
    }

    private fun verifyImageFetch(isValid: Boolean, isException: Boolean = false, isNull: Boolean = false) {
        ImageUtilSpec.IS_VALID = isValid
        ImageUtilSpec.IS_NULL = isNull
        `when`(mockResource.imageUrl).thenReturn("any url")
        `when`(mockPayload.headerColor).thenReturn("#")
        view?.picasso = ImageUtilSpec.setupMockPicasso(isException)
        view?.populateViewData(mockMessage)
        val imageView = view?.findViewById<ImageView>(R.id.message_image_view)

        if (isValid) {
            imageView?.visibility shouldBeEqualTo View.VISIBLE
        } else {
            imageView?.visibility shouldBeEqualTo View.GONE
        }
    }

    private fun verifyDefault() {
        view?.findViewById<TextView>(R.id.header_text)?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.BLACK)
        view?.findViewById<TextView>(R.id.message_body)?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.BLACK)
    }

    companion object {
        private const val WHITE_HEX = "#FFFFFF"
    }
}
