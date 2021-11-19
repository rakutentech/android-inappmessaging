package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.button.MaterialButton
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageButton
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
import timber.log.Timber
import kotlin.math.sqrt

/**
 * Base class of all custom views.
 */
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
    private var buttons: List<MessageButton>? = null
    private var imageAspectRatio = 0f
    private var displayOptOut = false

    /**
     * Sets campaign message data onto the view.
     */
    @SuppressWarnings("LongMethod")
    override fun populateViewData(message: Message, imageAspectRatio: Float) {
        try {
            this.headerColor = Color.parseColor(message.getMessagePayload().headerColor)
            this.messageBodyColor = Color.parseColor(message.getMessagePayload().messageBodyColor)
            this.bgColor = Color.parseColor(message.getMessagePayload().backgroundColor)
        } catch (e: IllegalArgumentException) {
            // values are from backend
            Timber.tag(TAG).e(e)
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
        this.imageAspectRatio = imageAspectRatio
        this.displayOptOut = message.getMessagePayload().messageSettings.displaySettings.optOut
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
    private fun bindButtons() {
        // Set onClick listener to close button.
        val closeButton = findViewById<ImageButton>(R.id.message_close_button)
        closeButton?.setOnClickListener(this.listener)
        if (this.buttons?.size == 1) {
            // Set bigger layout_margin if there's only one button.
            findViewById<MaterialButton>(R.id.message_single_button)?.let {
                setButtonInfo(it, this.buttons!![0])
            }
        } else if (buttons?.size == 2) {
            // Set bigger layout_margin if there's only one button.
            findViewById<MaterialButton>(R.id.message_button_left)?.let { setButtonInfo(it, this.buttons!![0]) }
            findViewById<MaterialButton>(R.id.message_button_right)?.let { setButtonInfo(it, this.buttons!![1]) }
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
    @SuppressLint("ClickableViewAccessibility")
    private fun bindImage() { // Display image.
        if (!this.imageUrl.isNullOrEmpty()) {
            findViewById<SimpleDraweeView>(R.id.message_image_view)?.let {
                it.setOnTouchListener(this.listener)
                // Building a DraweeController to handle animations.
                // Image should be already downloaded and cached in memory. Fresco library will look for the
                // cached image by URI.
                it.controller = Fresco.newDraweeControllerBuilder()
                        .setUri(this.imageUrl)
                        .setAutoPlayAnimations(true)
                        .build()
                it.aspectRatio = this.imageAspectRatio
                it.visibility = View.VISIBLE
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
            Timber.tag(TAG).e(e)
            // set to default color
            Color.parseColor("#1D1D1D")
        }
        buttonView.setTextColor(textColor)

        val bgColor = try {
            Color.parseColor(button.buttonBackgroundColor)
        } catch (e: IllegalArgumentException) {
            // values are from backend
            Timber.tag(TAG).e(e)
            // set to default color
            Color.WHITE
        }

        buttonView.backgroundTintList = ColorStateList.valueOf(bgColor)
        // Button stroke color equals to button text color.
        buttonView.strokeColor = ColorStateList.valueOf(textColor)
        buttonView.setOnClickListener(this.listener)

        val fontId = context.resources.getIdentifier("iam_custom_font_button", "font", context.packageName)
        val font = retrieveFontTypeFace(fontId)
        font?.let { buttonView.typeface = it }
        buttonView.visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.message_buttons)?.visibility = View.VISIBLE
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
        val fontId = context.resources.getIdentifier("iam_custom_font_text", "font", context.packageName)
        val font = retrieveFontTypeFace(fontId)
        if (!header.isNullOrEmpty()) {
            findViewById<TextView>(R.id.header_text)?.let {
                it.text = header
                it.setTextColor(headerColor)
                it.setOnTouchListener(listener)
                it.visibility = View.VISIBLE
                font?.let { font -> it.typeface = font }
            }
        }
        if (!messageBody.isNullOrEmpty()) {
            findViewById<TextView>(R.id.message_body)?.let {
                it.text = messageBody
                it.setTextColor(messageBodyColor)
                it.setOnTouchListener(listener)
                it.visibility = View.VISIBLE
                font?.let { font ->
                    it.typeface = font
                }
            }
        }
    }

    // Set close button to black background if the campaign background color is dark.
    // Brightness is computed based on [Darel Rex Finley's HSP Colour Model](http://alienryderflex.com/hsp.html).
    // Computed value is from 0 (black) to 255 (white), and is considered dark if less than 130.
    @SuppressWarnings("MagicNumber")
    internal fun setCloseButton(button: ImageButton? = null) {
        val red = Color.red(bgColor)
        val green = Color.green(bgColor)
        val blue = Color.blue(bgColor)
        val brightness = sqrt((red * red * .241) + (green * green * .691) + (blue * blue * .068)).toInt()
        if (brightness < 130) {
            (button ?: findViewById(R.id.message_close_button))
                ?.setImageResource(R.drawable.close_button_white)
        }
    }

    @SuppressLint("NewApi")
    private fun retrieveFontTypeFace(fontId: Int) = when {
        fontId <= 0 -> null
        BuildVersionChecker.instance().isAndroidOAndAbove() -> context.resources.getFont(fontId)
        else -> ResourcesCompat.getFont(context, fontId)
    }

    companion object {
        private const val TAG = "IAM_BaseView"
    }
}
