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
import com.squareup.picasso.Picasso
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.IllegalStateException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
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
        initializePicassoInstance()
        `when`(hostAppActivity
                .layoutInflater).thenReturn(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
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
    fun `should display image`() {
        val imageUrl =
            "https://en.wikipedia.org/wiki/Android_(operating_system)#/media/File:Android-robot-googleplex-2008.jpg"
        `when`(mockResource.imageUrl).thenReturn(imageUrl)
        `when`(mockPayload.headerColor).thenReturn("#")
        view?.populateViewData(mockMessage, 100, 100)

        // val imageView = view?.findViewById<ImageView>(R.id.message_image_view)
        // imageView?.visibility shouldBeEqualTo View.VISIBLE
    }

    @Test
    fun `should not display image with invalid url`() {
        val imageUrl = "invalid_url"
        `when`(mockResource.imageUrl).thenReturn(imageUrl)
        `when`(mockPayload.headerColor).thenReturn("#")
        view?.populateViewData(mockMessage)
        val imageView = view?.findViewById<ImageView>(R.id.message_image_view)

        imageView?.visibility shouldBeEqualTo View.GONE
    }

    private fun verifyDefault() {
        view?.findViewById<TextView>(R.id.header_text)?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.BLACK)
        view?.findViewById<TextView>(R.id.message_body)?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.BLACK)
    }

    private fun initializePicassoInstance() {
        try {
            val picasso = Picasso.Builder(ApplicationProvider.getApplicationContext()).build()
            Picasso.setSingletonInstance(picasso)
        } catch (ignored: IllegalStateException) {
            // Picasso instance was already initialized
        }
    }

    companion object {
        private const val WHITE_HEX = "#FFFFFF"
    }
}
