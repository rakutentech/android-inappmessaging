package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.facebook.soloader.SoLoader
import com.google.android.material.button.MaterialButton
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.ControlSettings
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageButton
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageSettings
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class BaseViewSpec : BaseTest() {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val hostAppActivity = Mockito.mock(Activity::class.java)
    private val mockMessage = Mockito.mock(Message::class.java)
    private val mockPayload = Mockito.mock(MessagePayload::class.java)
    private val mockSettings = Mockito.mock(MessageSettings::class.java)
    private val mockCtrlSettings = Mockito.mock(ControlSettings::class.java)
    private val mockBtn = Mockito.mock(MessageButton::class.java)
    private var view: InAppMessageBaseView? = null

    @Before
    override fun setup() {
        super.setup()
        SoLoader.setInTestMode()
        `when`(hostAppActivity
                .layoutInflater).thenReturn(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
        `when`(mockMessage.getMessagePayload()).thenReturn(mockPayload)
        `when`(mockPayload.header).thenReturn("test")
        `when`(mockPayload.messageBody).thenReturn("test")
        view = hostAppActivity
                .layoutInflater
                .inflate(R.layout.in_app_message_full_screen, null) as InAppMessageBaseView
    }

    @Test
    fun `should not crash when null payload`() {
        `when`(mockMessage.getMessagePayload()).thenReturn(null)

        view?.populateViewData(mockMessage)
    }

    @Test
    fun `should set default when null header color`() {
        `when`(mockPayload.headerColor).thenReturn(null)
        view?.populateViewData(mockMessage)

        verifyDefault()
    }

    @Test
    fun `should set default when null body color`() {
        `when`(mockPayload.headerColor).thenReturn(WHITE_HEX)
        `when`(mockPayload.messageBodyColor).thenReturn(null)
        view?.populateViewData(mockMessage)

        verifyDefault()
    }

    @Test
    fun `should set default when null bg color`() {
        `when`(mockPayload.headerColor).thenReturn(WHITE_HEX)
        `when`(mockPayload.messageBodyColor).thenReturn(WHITE_HEX)
        `when`(mockPayload.backgroundColor).thenReturn(null)
        view?.populateViewData(mockMessage)

        verifyDefault()
    }

    @Test
    fun `should set default when invalid button text and bg color`() {
        `when`(mockPayload.messageSettings).thenReturn(mockSettings)
        `when`(mockSettings.controlSettings).thenReturn(mockCtrlSettings)
        `when`(mockCtrlSettings.buttons).thenReturn(listOf(mockBtn))
        `when`(mockBtn.buttonTextColor).thenReturn("test")
        `when`(mockBtn.buttonTextColor).thenReturn("#")
        `when`(mockBtn.buttonBackgroundColor).thenReturn("#")
        view?.populateViewData(mockMessage)
        val button = view?.findViewById<MaterialButton>(R.id.message_single_button)
        button?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.parseColor("#1D1D1D"))
        button?.backgroundTintList shouldBeEqualTo ColorStateList.valueOf(Color.WHITE)
    }

    private fun verifyDefault() {
        view?.findViewById<TextView>(R.id.header_text)?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.BLACK)
        view?.findViewById<TextView>(R.id.message_body)?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.BLACK)
    }

    companion object {
        private const val WHITE_HEX = "#FFFFFF"
    }
}
