package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import androidx.test.core.app.ApplicationProvider
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.soloader.SoLoader
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.ControlSettings
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageButton
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageSettings
import kotlinx.android.synthetic.main.message_buttons.view.*
import kotlinx.android.synthetic.main.message_scrollview.view.*
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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
        Fresco.initialize(context)
        When calling hostAppActivity
                .layoutInflater itReturns LayoutInflater.from(ApplicationProvider.getApplicationContext())
        When calling mockMessage.getMessagePayload() itReturns mockPayload
        When calling mockPayload.header itReturns "test"
        When calling mockPayload.messageBody itReturns "test"
        view = hostAppActivity
                .layoutInflater
                .inflate(R.layout.in_app_message_full_screen, null) as InAppMessageBaseView
    }

    @Test
    fun `should not crash when null payload`() {
        When calling mockMessage.getMessagePayload() itReturns null

        view?.populateViewData(mockMessage, 1f)
    }

    @Test
    fun `should set default when null header color`() {
        When calling mockPayload.headerColor itReturns null
        view?.populateViewData(mockMessage, 1f)

        verifyDefault()
    }

    @Test
    fun `should set default when null body color`() {
        When calling mockPayload.headerColor itReturns WHITE_HEX
        When calling mockPayload.messageBodyColor itReturns null
        view?.populateViewData(mockMessage, 1f)

        verifyDefault()
    }

    @Test
    fun `should set default when null bg color`() {
        When calling mockPayload.headerColor itReturns WHITE_HEX
        When calling mockPayload.messageBodyColor itReturns WHITE_HEX
        When calling mockPayload.backgroundColor itReturns null
        view?.populateViewData(mockMessage, 1f)

        verifyDefault()
    }

    @Test
    fun `should set default when invalid button text and bg color`() {
        When calling mockPayload.messageSettings itReturns mockSettings
        When calling mockSettings.controlSettings itReturns mockCtrlSettings
        When calling mockCtrlSettings.buttons itReturns listOf(mockBtn)
        When calling mockBtn.buttonTextColor itReturns "test"
        When calling mockBtn.buttonTextColor itReturns "#"
        When calling mockBtn.buttonBackgroundColor itReturns "#"
        view?.populateViewData(mockMessage, 1f)

        view?.message_single_button?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.parseColor("#1D1D1D"))
        view?.message_single_button?.backgroundTintList shouldBeEqualTo ColorStateList.valueOf(Color.WHITE)
    }

    private fun verifyDefault() {
        view?.header_text?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.BLACK)
        view?.message_body?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.BLACK)
    }

    companion object {
        private const val WHITE_HEX = "#FFFFFF"
    }
}
