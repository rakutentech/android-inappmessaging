package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.app.Activity
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.imageview.ShapeableImageView
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.PositionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ImageUtilSpec
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class InAppMessagingTooltipViewSpec {

    private val hostAppActivity = Mockito.mock(Activity::class.java)
    private val mockMessage = Mockito.mock(Message::class.java)
    private val mockPayload = Mockito.mock(MessagePayload::class.java)
    private val mockSettings = Mockito.mock(MessageSettings::class.java)
    private val mockCtrlSettings = Mockito.mock(ControlSettings::class.java)
    private val mockDisplaySettings = Mockito.mock(DisplaySettings::class.java)
    private val mockResource = Mockito.mock(Resource::class.java)
    private val mockTooltip = Mockito.mock(Tooltip::class.java)
    private var view: InAppMessagingTooltipView? = null

    @Before
    fun setup() {
        Mockito.`when`(
            hostAppActivity
                .layoutInflater
        ).thenReturn(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
        Mockito.`when`(mockMessage.getMessagePayload()).thenReturn(mockPayload)
        Mockito.`when`(mockMessage.getTooltipConfig()).thenReturn(mockTooltip)
        Mockito.`when`(mockPayload.backgroundColor).thenReturn("#000000")
        Mockito.`when`(mockPayload.resource).thenReturn(mockResource)
        view = hostAppActivity
            .layoutInflater
            .inflate(R.layout.in_app_message_tooltip, null) as InAppMessagingTooltipView
    }

    @Test
    fun `should set default type for invalid position value`() {
        Mockito.`when`(mockTooltip.position).thenReturn("invalid-pos")
        view?.populateViewData(mockMessage)
        view?.type shouldBeEqualTo PositionType.BOTTOM_CENTER
    }

    @Test
    fun `should show image`() {
        Mockito.`when`(mockTooltip.position).thenReturn("top-center")
        verifyImageFetch(true)
        view?.type shouldBeEqualTo PositionType.TOP_CENTER
    }

    private fun verifyImageFetch(isValid: Boolean, isException: Boolean = false, isNull: Boolean = false) {
        ImageUtilSpec.IS_VALID = isValid
        ImageUtilSpec.IS_NULL = isNull
        Mockito.`when`(mockResource.imageUrl).thenReturn("any url")
        Mockito.`when`(mockPayload.headerColor).thenReturn("#")
        view?.picasso = ImageUtilSpec.setupMockPicasso(isException)
        view?.populateViewData(mockMessage)
        val imageView = view?.findViewById<ShapeableImageView>(R.id.message_tooltip_image_view)

        if (isValid) {
            imageView?.visibility shouldBeEqualTo View.INVISIBLE
        } else {
            imageView?.visibility shouldBeEqualTo View.GONE
        }
    }
}
