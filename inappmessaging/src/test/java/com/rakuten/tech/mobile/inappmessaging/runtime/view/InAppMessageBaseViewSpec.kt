package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.button.MaterialButton
import com.nhaarman.mockitokotlin2.never
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ImageUtilSpec
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.verification.VerificationMode
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import android.widget.CheckBox
import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.text.Layout
import androidx.core.widget.NestedScrollView
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson.MessageMapper
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ResourceUtils
import org.amshove.kluent.*
import org.junit.Ignore
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(RobolectricTestRunner::class)
@Ignore("base class")
open class InAppMessageBaseViewSpec : BaseTest() {
    private val hostAppActivity = mock(Activity::class.java)
    internal var view: InAppMessageBaseView? = null
    internal var expectedHyphenation = Layout.HYPHENATION_FREQUENCY_NONE
    internal val message = MessageMapper.mapFrom(TestDataHelper.createDummyMessage())

    @Before
    override fun setup() {
        super.setup()
        `when`(hostAppActivity.layoutInflater)
            .thenReturn(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
        view = hostAppActivity.layoutInflater.inflate(R.layout.in_app_message_full_screen, null)
            as InAppMessageBaseView
        expectedHyphenation = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            Layout.HYPHENATION_FREQUENCY_FULL_FAST
        } else {
            Layout.HYPHENATION_FREQUENCY_FULL
        }
    }

    companion object {
        internal const val WHITE_HEX = "#FFFFFF"
        internal const val BLACK_HEX = "#000000"
    }
}

class InAppMessageBaseViewBodySpec : InAppMessageBaseViewSpec() {

    @Test
    fun `should not bind view body when message body is null`() {
        view?.populateViewData(message.copy(bodyText = null))

        view?.findViewById<TextView>(R.id.message_body)?.visibility shouldNotBeEqualTo View.VISIBLE
    }

    @Test
    fun `should not bind view body when message body is empty`() {
        view?.populateViewData(message.copy(bodyText = null))

        view?.findViewById<TextView>(R.id.message_body)?.visibility shouldNotBeEqualTo View.VISIBLE
    }
}

class InAppMessageBaseViewHeaderSpec : InAppMessageBaseViewSpec() {

    @Test
    fun `should not bind view header when message header and body are null`() {
        view?.populateViewData(
            message.copy(
                headerText = null,
                bodyText = null,
            ),
        )

        view?.findViewById<NestedScrollView>(R.id.message_scrollview)?.visibility shouldNotBeEqualTo View.VISIBLE
        view?.findViewById<TextView>(R.id.header_text)?.visibility shouldNotBeEqualTo View.VISIBLE
    }

    @Test
    fun `should not bind view header when message header and body are empty`() {
        view?.populateViewData(
            message.copy(
                headerText = "",
                bodyText = "",
            ),
        )

        view?.findViewById<NestedScrollView>(R.id.message_scrollview)?.visibility shouldNotBeEqualTo View.VISIBLE
        view?.findViewById<TextView>(R.id.header_text)?.visibility shouldNotBeEqualTo View.VISIBLE
    }

    @Test
    fun `should show scrollview when header is valid`() {
        view?.populateViewData(
            message.copy(
                headerText = "abc",
                bodyText = "",
            ),
        )

        view?.findViewById<NestedScrollView>(R.id.message_scrollview)?.visibility shouldBeEqualTo View.VISIBLE
        view?.findViewById<TextView>(R.id.header_text)?.visibility shouldBeEqualTo View.VISIBLE
    }

    @Test
    fun `should show scrollview when body is valid`() {
        view?.populateViewData(
            message.copy(
                headerText = "",
                bodyText = "abc",
            ),
        )

        view?.findViewById<NestedScrollView>(R.id.message_scrollview)?.visibility shouldBeEqualTo View.VISIBLE
    }
}

class InAppMessageBaseViewCheckBoxSpec : InAppMessageBaseViewSpec() {
    @Test
    fun `should set check box to white color`() {
        verifyCheckBox(BLACK_HEX, Color.WHITE)
    }

    @Test
    fun `should set check box to black color`() {
        verifyCheckBox(WHITE_HEX, Color.BLACK)
    }

    private fun verifyCheckBox(color: String, expectedColor: Int) {
        view?.populateViewData(
            message.copy(
                headerColor = color,
                bodyColor = color,
                backgroundColor = color,
                displaySettings = message.displaySettings.copy(isOptedOut = true),
            ),
        )

        view?.findViewById<CheckBox>(R.id.opt_out_checkbox)?.textColors?.defaultColor shouldBeEqualTo expectedColor
    }

    @Test
    fun `should set checkbox to visible`() {
        view?.populateViewData(
            message.copy(
                headerColor = "#",
                displaySettings = message.displaySettings.copy(isOptedOut = true),
            ),
        )

        view?.findViewById<CheckBox>(R.id.opt_out_checkbox)?.visibility shouldBeEqualTo View.VISIBLE
    }
}

class InAppMessageBaseViewImageSpec : InAppMessageBaseViewSpec() {
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
        ImageUtilSpec.setupValidPicasso()
        view?.populateViewData(
            message.copy(
                imageUrl = "test valid UR",
                headerColor = "#",
            ),
        )
    }

    private fun verifyImageFetch(isValid: Boolean, isException: Boolean = false, isNull: Boolean = false) {
        ImageUtilSpec.IS_VALID = isValid
        ImageUtilSpec.IS_NULL = isNull

        view?.picasso = ImageUtilSpec.setupMockPicasso(isException)
        view?.populateViewData(
            message.copy(
                imageUrl = "any url",
                headerColor = "#",
            ),
        )

        val imageView = view?.findViewById<ImageView>(R.id.message_image_view)
        if (isValid) {
            imageView?.visibility shouldBeEqualTo View.VISIBLE
        } else {
            imageView?.visibility shouldBeEqualTo View.GONE
        }
    }
}

class InAppMessageBaseViewBorderSpec : InAppMessageBaseViewSpec() {
    @Test
    fun `should set button border for identical bg colors`() {
        view?.populateViewData(
            message.copy(
                headerColor = WHITE_HEX,
                bodyColor = WHITE_HEX,
                backgroundColor = "#AA00BB",
            ),
        )

        val mockButton = mock(MaterialButton::class.java)
        view?.setButtonBorder(mockButton, Color.parseColor("#AA00BB"), Color.BLACK)
        verify(mockButton, times(1)).setStrokeColor(ColorStateList.valueOf(Color.BLACK))
        verify(mockButton, times(1)).setStrokeWidth(any())
    }

    @Test
    fun `should set button border for similar bg colors`() {
        view?.populateViewData(
            message.copy(
                headerColor = WHITE_HEX,
                bodyColor = WHITE_HEX,
                backgroundColor = "#AA00BB",
            ),
        )

        val mockButton = mock(MaterialButton::class.java)
        view?.setButtonBorder(mockButton, Color.parseColor("#AA05BB"), Color.BLACK)
        verify(mockButton, times(1)).setStrokeColor(ColorStateList.valueOf(Color.BLACK))
        verify(mockButton, times(1)).setStrokeWidth(any())
    }

    @Test
    fun `should set grey button border for white bg colors`() {
        view?.populateViewData(
            message.copy(
                headerColor = WHITE_HEX,
                bodyColor = WHITE_HEX,
                backgroundColor = WHITE_HEX,
            ),
        )

        val mockButton = mock(MaterialButton::class.java)
        view?.setButtonBorder(mockButton, Color.parseColor(WHITE_HEX), Color.BLACK)
        val expectedColor = view?.resources?.getColorStateList(
            R.color.modal_border_color_light_grey, view!!.context.theme,
        )
        verify(mockButton, times(1)).setStrokeColor(expectedColor)
        verify(mockButton, times(1)).setStrokeWidth(any())
    }

    @Test
    fun `should not set button border for different bg colors`() {
        view?.populateViewData(
            message.copy(
                headerColor = WHITE_HEX,
                bodyColor = WHITE_HEX,
                backgroundColor = WHITE_HEX,
            ),
        )

        val mockButton = mock(MaterialButton::class.java)
        view?.setButtonBorder(mockButton, Color.parseColor("#AA00BB"), Color.BLACK)
        verify(mockButton, never()).setStrokeColor(any())
        verify(mockButton, never()).setStrokeWidth(any())
    }
}

class InAppMessageBaseViewColorSpec : InAppMessageBaseViewSpec() {
    @Test
    @Config(sdk = [Build.VERSION_CODES.S, Build.VERSION_CODES.TIRAMISU])
    fun `should set default when invalid header color`() {
        view?.populateViewData(
            message.copy(
                headerColor = "invalid",
            ),
        )

        verifyDefault()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S, Build.VERSION_CODES.TIRAMISU])
    fun `should set default when invalid body color`() {
        view?.populateViewData(
            message.copy(
                headerColor = WHITE_HEX,
                bodyColor = "incorrect",
            ),
        )

        verifyDefault()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S, Build.VERSION_CODES.TIRAMISU])
    fun `should set default when invalid bg color`() {
        view?.populateViewData(
            message.copy(
                headerColor = WHITE_HEX,
                bodyColor = WHITE_HEX,
                backgroundColor = "failed",
            ),
        )

        verifyDefault()
    }

    @Test
    fun `should set default when invalid button text and bg color`() {
        view?.populateViewData(
            message.copy(
                headerColor = "invalid",
                buttons = listOf(
                    MessageButton(
                        buttonBackgroundColor = "#",
                        buttonTextColor = "test",
                        buttonBehavior = OnClickBehavior(2, null),
                        buttonText = "test",
                    ),
                ),
            ),
        )

        val button = view?.findViewById<MaterialButton>(R.id.message_single_button)
        button?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.parseColor("#1D1D1D"))
        button?.backgroundTintList shouldBeEqualTo ColorStateList.valueOf(Color.WHITE)
    }

    @Test
    fun `should set close button to black background`() {
        verifyCloseButton(BLACK_HEX, times(1))
    }

    @Test
    fun `should not set close button to black background`() {
        verifyCloseButton(WHITE_HEX, never())
    }

    private fun verifyCloseButton(color: String, mode: VerificationMode) {
        view?.populateViewData(
            message.copy(
                headerColor = color,
                bodyColor = color,
                backgroundColor = color,
                shouldShowUpperCloseButton = true,
            ),
        )

        val mockButton = mock(ImageButton::class.java)
        view?.setCloseButton(mockButton)
        verify(mockButton, mode).setImageResource(R.drawable.close_button_white)
    }

    private fun verifyDefault() {
        val header = view?.findViewById<TextView>(R.id.header_text)
        header?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.BLACK)
        header?.hyphenationFrequency shouldBe expectedHyphenation

        val body = view?.findViewById<TextView>(R.id.message_body)
        body?.textColors shouldBeEqualTo ColorStateList.valueOf(Color.BLACK)
        body?.hyphenationFrequency shouldBe expectedHyphenation
    }
}

class InAppMessageBaseViewTextSpec : InAppMessageBaseViewSpec() {
    @Test
    fun `should set correct font typeface`() {
        val mockTypeface = setMock()

        view?.findViewById<TextView>(R.id.header_text)?.typeface shouldBeEqualTo mockTypeface
        view?.findViewById<TextView>(R.id.message_body)?.typeface shouldBeEqualTo mockTypeface
        view?.findViewById<MaterialButton>(R.id.message_single_button)?.typeface shouldBeEqualTo mockTypeface
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `should set correct font for lower API`() {
        val mockFont = mock(Typeface::class.java)
        setMock(lower = true, font = mockFont)

        view?.findViewById<TextView>(R.id.header_text)?.typeface shouldBeEqualTo mockFont
        view?.findViewById<TextView>(R.id.message_body)?.typeface shouldBeEqualTo mockFont
        view?.findViewById<MaterialButton>(R.id.message_single_button)?.typeface shouldBeEqualTo mockFont

        ResourceUtils.mockFont = null
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `should not crash for lower API with no mock`() {
        setMock(lower = true)

        view?.findViewById<TextView>(R.id.header_text)?.typeface.shouldNotBeNull()
        view?.findViewById<TextView>(R.id.message_body)?.typeface.shouldNotBeNull()
        view?.findViewById<MaterialButton>(R.id.message_single_button)?.typeface.shouldNotBeNull()
    }

    @Test
    fun `should not crash if get string failed`() {
        setMock(true)

        view?.findViewById<TextView>(R.id.header_text)?.typeface.shouldNotBeNull()
        view?.findViewById<TextView>(R.id.message_body)?.typeface.shouldNotBeNull()
        view?.findViewById<MaterialButton>(R.id.message_single_button)?.typeface.shouldNotBeNull()
    }

    @Test
    fun `should not crash if font id is invalid`() {
        setMock(id = 0)

        view?.findViewById<TextView>(R.id.header_text)?.typeface.shouldNotBeNull()
        view?.findViewById<TextView>(R.id.message_body)?.typeface.shouldNotBeNull()
        view?.findViewById<MaterialButton>(R.id.message_single_button)?.typeface.shouldNotBeNull()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S, Build.VERSION_CODES.TIRAMISU])
    fun `should set button hyphenation`() {
        setMock()

        val btn = view?.findViewById<MaterialButton>(R.id.message_single_button)
        btn?.hyphenationFrequency shouldBe expectedHyphenation
    }

    @SuppressWarnings("LongMethod")
    private fun setMock(inv: Boolean = false, id: Int = 1, lower: Boolean = false, font: Typeface? = null): Typeface? {
        val mockContext = mock(Context::class.java)
        val mockResource = mock(Resources::class.java)
        val mockTypeface = mock(Typeface::class.java)
        view?.mockContext = mockContext
        `when`(mockContext.resources).thenReturn(mockResource)
        `when`(mockContext.packageName).thenReturn("test")
        `when`(mockResource.getIdentifier(any(), eq("string"), any())).thenReturn(1)
        `when`(mockResource.getIdentifier(any(), eq("font"), any())).thenReturn(id)
        if (inv) {
            `when`(mockContext.getString(any())).thenThrow(Resources.NotFoundException())
        } else {
            `when`(mockContext.getString(any())).thenReturn("testfont")
        }
        if (lower) {
            ResourceUtils.mockFont = font
        } else {
            `when`(mockResource.getFont(any())).thenReturn(mockTypeface)
        }
        view?.populateViewData(
            message.copy(
                headerColor = "#",
                buttons = listOf(
                    MessageButton(
                        buttonBackgroundColor = "#",
                        buttonTextColor = "#",
                        buttonBehavior = OnClickBehavior(2, null),
                        buttonText = "test",
                    ),
                ),
            ),
        )
        return mockTypeface
    }
}
