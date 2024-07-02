package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.text.Layout
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.annotation.VisibleForTesting
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.ui.UiMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageButton
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ResourceUtils
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ViewUtil
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlin.math.sqrt
import java.lang.Exception
import kotlin.math.round

/**
 * Base class of all custom views.
 */
@SuppressWarnings("LargeClass", "TooManyFunctions")
internal open class InAppMessageBaseView(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs), InAppMessageView {

    init {
        id = R.id.in_app_message_base_view
    }

    var bgColor = 0
        private set
    var listener: InAppMessageViewListener? = null
        private set
    private var imageUrl: String? = null
    private var headerColor = 0
    private var messageBodyColor = 0
    private var header: String? = null
    private var messageBody: String? = null
    private var buttons = emptyList<MessageButton>()
    private var displayOptOut = false
    private var isDismissable: Boolean = true

    @VisibleForTesting
    internal var picasso: Picasso? = null

    @VisibleForTesting
    internal var mockContext: Context? = null

    /**
     * Sets campaign message data onto the view.
     */
    override fun populateViewData(uiMessage: UiMessage) {
        setColor(uiMessage)
        this.header = uiMessage.headerText
        this.messageBody = uiMessage.bodyText
        this.buttons = uiMessage.buttons
        this.imageUrl = uiMessage.imageUrl
        this.listener = InAppMessageViewListener(uiMessage)
        this.displayOptOut = uiMessage.displaySettings.isOptedOut
        this.isDismissable = uiMessage.shouldShowUpperCloseButton
        bindViewData()
        this.tag = uiMessage.id
    }

    private fun setColor(messageUiModel: UiMessage) {
        try {
            this.headerColor = Color.parseColor(messageUiModel.headerColor)
            this.messageBodyColor = Color.parseColor(messageUiModel.bodyColor)
            this.bgColor = Color.parseColor(messageUiModel.backgroundColor)
        } catch (e: IllegalArgumentException) {
            // values are from backend
            InAppLogger(TAG).error(e.message)
            // change to default
            this.headerColor = Color.BLACK
            this.messageBodyColor = Color.BLACK
            this.bgColor = Color.WHITE
        }
    }

    /**
     * This method binds data to view.
     */
    // Warning: NPath complexity > 200. Explanation: Empty checks are OK.
    private fun bindViewData() {
        bindImage()
        bindText()
        bindButtons()
        bindCheckBox()

        // for handling back button press
        this.isFocusableInTouchMode = true
        requestFocus()
        setOnKeyListener(listener)
    }

    /**
     * This method binds data to buttons.
     */
    private fun bindButtons() {
        // Set onClick listener to close button.
        findViewById<ImageButton>(R.id.message_close_button)?.let { closeButton ->
            if (isDismissable) {
                closeButton.setOnClickListener(this.listener)
            } else {
                closeButton.visibility = View.GONE
            }
        }
        setupButton()
    }

    private fun setupButton() {
        when (this.buttons.size) {
            1 -> {
                // Set bigger layout_margin if there's only one button.
                findViewById<MaterialButton>(R.id.message_single_button)?.let {
                    setButtonInfo(it, this.buttons[0])
                }
            }
            2 -> {
                // Set bigger layout_margin if there's only one button.
                findViewById<MaterialButton>(R.id.message_button_left)?.let { setButtonInfo(it, this.buttons[0]) }
                findViewById<MaterialButton>(R.id.message_button_right)?.let { setButtonInfo(it, this.buttons[1]) }
            }
            else -> Any()
        }
    }

    /**
     * This method binds data and listener to checkbox.
     */
    private fun bindCheckBox() {
        // Display opt-out checkbox.
        if (this.displayOptOut) {
            findViewById<CheckBox>(R.id.opt_out_checkbox)?.let { checkBox ->
                val brightness = getBrightness(bgColor)
                if (brightness < BRIGHTNESS_LEVEL) {
                    checkBox.buttonTintList = ColorStateList.valueOf(Color.WHITE)
                    checkBox.setTextColor(Color.WHITE)
                }
                checkBox.setOnClickListener(this.listener)
                checkBox.visibility = View.VISIBLE
            }
        }
    }

    /**
     * This method binds image to view.
     */
    @Suppress("ClickableViewAccessibility", "TooGenericExceptionCaught", "LongMethod")
    private fun bindImage() { // Display image.
        if (!this.imageUrl.isNullOrEmpty()) {
            // load the image then display the view
            this.visibility = GONE
            findViewById<ImageView>(R.id.message_image_view)?.let { imgView ->
                imgView.setOnTouchListener(this.listener)
                try {
                    val callback = object : Callback {
                        override fun onSuccess() {
                            imgView.visibility = VISIBLE
                            this@InAppMessageBaseView.visibility = VISIBLE
                        }

                        override fun onError(e: Exception?) {
                            InAppLogger(TAG).debug(e?.cause, "Downloading image failed $imageUrl")
                        }
                    }
                    (picasso ?: Picasso.get()).load(this.imageUrl)
                        .priority(Picasso.Priority.HIGH)
                        .resize(ViewUtil.getDisplayWidth(context), 0)
                        .onlyScaleDown()
                        .centerInside()
                        .into(imgView, callback)
                } catch (ex: Exception) {
                    InAppLogger(TAG).debug(ex, "Downloading image failed $imageUrl")
                }
            }
        }
    }

    /**
     * This method adds button information, then set it to visible.
     */
    private fun setButtonInfo(buttonView: MaterialButton, button: MessageButton) {
        buttonView.text = button.buttonText
        buttonView.hyphenationFrequency = getHyphenationFreq()
        val textColor = setTextColor(button, buttonView)
        val bgColor = setBgColor(button, buttonView)

        setButtonBorder(buttonView, bgColor, textColor)

        buttonView.setOnClickListener(this.listener)

        getFont(BUTTON_FONT)?.let { buttonView.typeface = it }
        buttonView.visibility = VISIBLE
        findViewById<LinearLayout>(R.id.message_buttons)?.visibility = VISIBLE
    }

    private fun setBgColor(button: MessageButton, buttonView: MaterialButton): Int {
        val bgColor = try {
            Color.parseColor(button.buttonBackgroundColor)
        } catch (e: IllegalArgumentException) {
            // values are from backend
            InAppLogger(TAG).error(e.message)
            // set to default color
            Color.WHITE
        }

        buttonView.backgroundTintList = ColorStateList.valueOf(bgColor)
        return bgColor
    }

    private fun setTextColor(button: MessageButton, buttonView: MaterialButton): Int {
        val textColor = try {
            Color.parseColor(button.buttonTextColor)
        } catch (e: IllegalArgumentException) {
            // values are from backend
            InAppLogger(TAG).error(e.message)
            // set to default color
            Color.parseColor("#1D1D1D")
        }
        buttonView.setTextColor(textColor)
        return textColor
    }

    /**
     * This method binds data to message header.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun bindText() {
        bindHeader()
        bindBody()
    }

    private fun bindBody() {
        if (!messageBody.isNullOrEmpty()) {
            findViewById<TextView>(R.id.message_body)?.apply {
                text = messageBody
                setTextColor(messageBodyColor)
                setOnTouchListener(listener)
                visibility = VISIBLE
                hyphenationFrequency = getHyphenationFreq() // Word break
                val font = getFont(BODY_FONT)
                if (font != null) {
                    typeface = font
                }
            }
        }
    }

    private fun bindHeader() {
        if (!header.isNullOrEmpty() || !messageBody.isNullOrEmpty()) {
            findViewById<NestedScrollView>(R.id.message_scrollview)?.visibility = VISIBLE
        }
        if (!header.isNullOrEmpty()) {
            findViewById<TextView>(R.id.header_text)?.let { textView ->
                textView.text = header
                textView.setTextColor(headerColor)
                textView.setOnTouchListener(listener)
                textView.visibility = VISIBLE
                textView.hyphenationFrequency = getHyphenationFreq() // Word break
                val font = getFont(HEADER_FONT)
                if (font != null) {
                    textView.typeface = font
                }
            }
        }
    }

    // Set close button to black background if the campaign background color is dark.
    internal fun setCloseButton(button: ImageButton? = null) {
        if (isDismissable) {
            val brightness = getBrightness(bgColor)
            if (brightness < BRIGHTNESS_LEVEL) {
                (button ?: findViewById(R.id.message_close_button))
                    ?.setImageResource(R.drawable.close_button_white)
            }
        }
    }

    // Brightness is computed based on [Darel Rex Finley's HSP Colour Model](http://alienryderflex.com/hsp.html).
    // Computed value is from 0 (black) to 255 (white), and is considered dark if less than 130.
    private fun getBrightness(color: Int): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return sqrt((red * red * RED_DEGREE) + (green * green * GREEN_DEGREE) + (blue * blue * BLUE_DEGREE)).toInt()
    }

    // Set button's border based on similarity between its background color and campaign's background color.
    // The method calculates a distance between colors based on the low-cost approximation algorithm
    // from [](https://www.compuphase.com/cmetric.htm).
    // The distance value is compared against a threshold constant to decide if the colors
    // are visually similar or not.
    @VisibleForTesting
    internal fun setButtonBorder(buttonView: MaterialButton, buttonBackgroundColor: Int, defaultStrokeColor: Int) {
        val distance = computeDistance(buttonBackgroundColor)

        if (distance <= DIST_THRESHOLD) {
            buttonView.strokeWidth = resources.getDimension(R.dimen.modal_button_border_stroke_width).toInt()
            if (buttonBackgroundColor == Color.WHITE) {
                val strokeColor = resources.getColorStateList(R.color.modal_border_color_light_grey, context.theme)
                buttonView.strokeColor = strokeColor
            } else {
                buttonView.strokeColor = ColorStateList.valueOf(defaultStrokeColor)
            }
        }
    }

    private fun computeDistance(buttonBackgroundColor: Int): Int {
        val redMean = (Color.red(bgColor) + Color.red(buttonBackgroundColor)) / 2.0
        val dRed = Color.red(bgColor) - Color.red(buttonBackgroundColor)
        val dGreen = Color.green(bgColor) - Color.green(buttonBackgroundColor)
        val dBlue = Color.blue(bgColor) - Color.blue(buttonBackgroundColor)
        return round(
            sqrt(
                (2 + redMean / COLOR_RANGE) * dRed * dRed + GREEN_MULTI * dGreen * dGreen +
                    (2 + (COLOR_MAX - redMean) / COLOR_RANGE) * dBlue * dBlue,
            ),
        ).toInt()
    }

    private fun getFont(name: String): Typeface? {
        val ctx = mockContext ?: context
        val strId = ResourceUtils.getResourceIdentifier(ctx, name, "string")
        if (strId > 0) {
            try {
                return ResourceUtils.getFont(
                    ctx,
                    ResourceUtils.getResourceIdentifier(ctx, ctx.getString(strId), "font"),
                )
            } catch (rex: Resources.NotFoundException) {
                InAppLogger(TAG).debug(rex.cause, "Font file is not found. Will revert to default font.")
            }
        }

        return null
    }

    @SuppressLint("InlinedApi")
    private fun getHyphenationFreq(): Int {
        return if (BuildVersionChecker.isAndroidTAndAbove()) {
            Layout.HYPHENATION_FREQUENCY_FULL_FAST
        } else {
            Layout.HYPHENATION_FREQUENCY_FULL
        }
    }

    companion object {
        private const val TAG = "IAM_BaseView"
        private const val BUTTON_FONT = "iam_custom_font_button"
        private const val HEADER_FONT = "iam_custom_font_header"
        private const val BODY_FONT = "iam_custom_font_body"
        private const val BRIGHTNESS_LEVEL = 130
        private const val DIST_THRESHOLD = 15
        private const val COLOR_RANGE = 256
        private const val COLOR_MAX = 255
        private const val RED_DEGREE = 0.241
        private const val GREEN_DEGREE = 0.691
        private const val BLUE_DEGREE = 0.068
        private const val GREEN_MULTI = 4
    }
}
