package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
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
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageButton
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
@SuppressWarnings("LargeClass")
internal open class InAppMessageBaseView(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs), InAppMessageView {

    init {
        id = R.id.in_app_message_base_view
    }

    protected var bgColor = 0
    private var imageUrl: String? = null
    protected var listener: InAppMessageViewListener? = null
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
    @SuppressWarnings("LongMethod")
    override fun populateViewData(message: Message) {
        try {
            this.headerColor = Color.parseColor(message.getMessagePayload().headerColor)
            this.messageBodyColor = Color.parseColor(message.getMessagePayload().messageBodyColor)
            this.bgColor = Color.parseColor(message.getMessagePayload().backgroundColor)
        } catch (e: IllegalArgumentException) {
            // values are from backend
            InAppLogger(TAG).error(e.message)
            // change to default
            this.headerColor = Color.BLACK
            this.messageBodyColor = Color.BLACK
            this.bgColor = Color.WHITE
        }
        this.header = message.getMessagePayload().header
        this.messageBody = message.getMessagePayload().messageBody
        this.buttons = message.getMessagePayload().messageSettings.controlSettings.buttons
        this.imageUrl = message.getMessagePayload().resource.imageUrl
        this.listener = InAppMessageViewListener(message)
        this.displayOptOut = message.getMessagePayload().messageSettings.displaySettings.optOut
        this.isDismissable = message.isCampaignDismissable()
        bindViewData()
        this.tag = message.getCampaignId()
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
    @SuppressWarnings("LongMethod")
    private fun bindButtons() {
        // Set onClick listener to close button.
        val closeButton = findViewById<ImageButton>(R.id.message_close_button)
        closeButton?.let {
            if (isDismissable) {
                it.setOnClickListener(this.listener)
            } else {
                it.visibility = View.GONE
            }
        }

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
            val checkBox = findViewById<CheckBox>(R.id.opt_out_checkbox)
            checkBox?.setOnClickListener(this.listener)
            checkBox?.visibility = View.VISIBLE
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
    @SuppressWarnings("LongMethod")
    private fun setButtonInfo(buttonView: MaterialButton, button: MessageButton) {
        buttonView.text = button.buttonText
        val textColor = try {
            Color.parseColor(button.buttonTextColor)
        } catch (e: IllegalArgumentException) {
            // values are from backend
            InAppLogger(TAG).error(e.message)
            // set to default color
            Color.parseColor("#1D1D1D")
        }
        buttonView.setTextColor(textColor)

        val bgColor = try {
            Color.parseColor(button.buttonBackgroundColor)
        } catch (e: IllegalArgumentException) {
            // values are from backend
            InAppLogger(TAG).error(e.message)
            // set to default color
            Color.WHITE
        }

        buttonView.backgroundTintList = ColorStateList.valueOf(bgColor)
        setButtonBorder(buttonView, bgColor, textColor)

        buttonView.setOnClickListener(this.listener)

        getFont(BUTTON_FONT)?.let { buttonView.typeface = it }
        buttonView.visibility = VISIBLE
        findViewById<LinearLayout>(R.id.message_buttons)?.visibility = VISIBLE
    }

    /**
     * This method binds data to message header.
     */
    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("LongMethod")
    private fun bindText() {
        if (!header.isNullOrEmpty() || !messageBody.isNullOrEmpty()) {
            findViewById<NestedScrollView>(R.id.message_scrollview)?.visibility = View.VISIBLE
        }
        if (!header.isNullOrEmpty()) {
            findViewById<TextView>(R.id.header_text)?.let { textView ->
                textView.text = header
                textView.setTextColor(headerColor)
                textView.setOnTouchListener(listener)
                textView.visibility = View.VISIBLE
                getFont(HEADER_FONT)?.let { font ->
                    textView.typeface = font
                }
            }
        }
        if (!messageBody.isNullOrEmpty()) {
            findViewById<TextView>(R.id.message_body)?.let { textView ->
                textView.text = messageBody
                textView.setTextColor(messageBodyColor)
                textView.setOnTouchListener(listener)
                textView.visibility = View.VISIBLE
                getFont(BODY_FONT)?.let { font ->
                    textView.typeface = font
                }
            }
        }
    }

    // Set close button to black background if the campaign background color is dark.
    // Brightness is computed based on [Darel Rex Finley's HSP Colour Model](http://alienryderflex.com/hsp.html).
    // Computed value is from 0 (black) to 255 (white), and is considered dark if less than 130.
    @SuppressWarnings("MagicNumber")
    internal fun setCloseButton(button: ImageButton? = null) {
        if (isDismissable) {
            val red = Color.red(bgColor)
            val green = Color.green(bgColor)
            val blue = Color.blue(bgColor)
            val brightness = sqrt((red * red * .241) + (green * green * .691) + (blue * blue * .068)).toInt()
            if (brightness < 130) {
                (button ?: findViewById(R.id.message_close_button))
                    ?.setImageResource(R.drawable.close_button_white)
            }
        }
    }

    // Set button's border based on similarity between its background color and campaign's background color.
    // The method calculates a distance between colors based on the low-cost approximation algorithm
    // from [](https://www.compuphase.com/cmetric.htm).
    // The distance value is compared against a threshold constant to decide if the colors
    // are visually similar or not.
    @SuppressWarnings("MagicNumber", "LongMethod")
    @VisibleForTesting
    internal fun setButtonBorder(
        buttonView: MaterialButton,
        buttonBackgroundColor: Int,
        defaultStrokeColor: Int
    ) {
        val redMean = (Color.red(bgColor) + Color.red(buttonBackgroundColor)) / 2.0
        val dRed = Color.red(bgColor) - Color.red(buttonBackgroundColor)
        val dGreen = Color.green(bgColor) - Color.green(buttonBackgroundColor)
        val dBlue = Color.blue(bgColor) - Color.blue(buttonBackgroundColor)
        val distance = round(
            sqrt(
                (2 + redMean / 256) * dRed * dRed + 4 * dGreen * dGreen + (2 + (255 - redMean) / 256) * dBlue * dBlue
            )
        ).toInt()

        if (distance <= 15) {
            buttonView.strokeWidth =
                resources.getDimension(R.dimen.modal_button_border_stroke_width).toInt()
            if (buttonBackgroundColor == Color.WHITE) {
                val strokeColor = resources.getColorStateList(R.color.modal_border_color_light_grey, context.theme)
                buttonView.strokeColor = strokeColor
            } else {
                buttonView.strokeColor = ColorStateList.valueOf(defaultStrokeColor)
            }
        }
    }

    private fun getFont(name: String): Typeface? {
        val ctx = mockContext ?: context
        val strId = ResourceUtils.getResourceIdentifier(ctx, name, "string")
        if (strId > 0) {
            try {
                return ResourceUtils.getFont(
                    ctx,
                    ResourceUtils.getResourceIdentifier(ctx, ctx.getString(strId), "font")
                )
            } catch (rex: Resources.NotFoundException) {
                InAppLogger(TAG).debug(rex.cause, "Font file is not found. Will revert to default font.")
            }
        }

        return null
    }

    companion object {
        private const val TAG = "IAM_BaseView"
        private const val BUTTON_FONT = "iam_custom_font_button"
        private const val HEADER_FONT = "iam_custom_font_header"
        private const val BODY_FONT = "iam_custom_font_body"
    }
}
