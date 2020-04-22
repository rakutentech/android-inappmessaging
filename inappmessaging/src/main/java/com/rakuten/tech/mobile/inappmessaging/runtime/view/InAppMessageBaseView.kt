package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.material.button.MaterialButton
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageButton
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import kotlinx.android.synthetic.main.close_button.view.*
import kotlinx.android.synthetic.main.in_app_message_modal.view.*
import kotlinx.android.synthetic.main.message_buttons.view.*
import kotlinx.android.synthetic.main.message_image_view.view.*
import kotlinx.android.synthetic.main.message_scrollview.view.*
import kotlinx.android.synthetic.main.opt_out_checkbox.view.*

/**
 * Base class of all custom views.
 */
@SuppressWarnings("PMD.ExcessiveImports")
internal open class InAppMessageBaseView(context: Context, attrs: AttributeSet?) :
        FrameLayout(context, attrs), InAppMessageView {

    init {
        id = R.id.in_app_message_base_view
    }

    protected var bgColor = 0
    protected var imageUrl: String? = null
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
    override fun populateViewData(message: Message, imageAspectRatio: Float) {
        this.headerColor = Color.parseColor(message.getMessagePayload()?.headerColor)
        this.messageBodyColor = Color.parseColor(message.getMessagePayload()?.messageBodyColor)
        this.bgColor = Color.parseColor(message.getMessagePayload()?.backgroundColor)
        this.header = message.getMessagePayload()?.header
        this.messageBody = message.getMessagePayload()?.messageBody
        this.buttons = message.getMessagePayload()?.messageSettings?.controlSettings?.buttons
        this.imageUrl = message.getMessagePayload()?.resource?.imageUrl
        this.listener = InAppMessageViewListener(message)
        this.imageAspectRatio = imageAspectRatio
        this.displayOptOut = message.getMessagePayload()?.messageSettings?.displaySettings?.optOut!!
        bindViewData()
    }

    /**
     * This method binds data to view.
     */
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE") // No need to check for casting type.
    // Warning: NPath complexity > 200. Explanation: Empty checks are OK.
    private fun bindViewData() {
        bindImage()
        bindText()
        bindButtons()
        bindCheckBox()
    }

    /**
     * This method binds data to buttons.
     */
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    private fun bindButtons() {
        // Set onClick listener to close button.
        val closeButton = message_close_button
        closeButton.setOnClickListener(this.listener)
        if (this.buttons?.size == 1) { // NOPMD
            // Set bigger layout_margin if there's only one button.
            val view = message_single_button
            if (view is MaterialButton) {
                setButtonInfo(view, this.buttons!![0])
            }
        } else // NOPMD
        // Set bigger layout_margin if there's only one button.
        {
            if (buttons?.size == 2) { // NOPMD
                setButtonInfo(message_button_left as MaterialButton, this.buttons!![0])
                setButtonInfo(message_button_right as MaterialButton, this.buttons!![1])
            }
        }
    }

    /**
     * This method binds data and listener to checkbox.
     */
    private fun bindCheckBox() {
        // Display opt-out checkbox.
        if (this.displayOptOut) {
            val checkBox = opt_out_checkbox
            checkBox.setOnClickListener(this.listener)
            checkBox.visibility = View.VISIBLE
        }
    }

    /**
     * This method binds image to view.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun bindImage() { // Display image.
        if (!this.imageUrl.isNullOrEmpty()) {
            val draweeView = message_image_view
            if (draweeView != null) {
                draweeView.setOnTouchListener(this.listener)
                // Building a DraweeController to handle animations.
                // Image should be already downloaded and cached in memory. Fresco library will look for the
                // cached image by URI.
                draweeView.controller = Fresco.newDraweeControllerBuilder()
                        .setUri(this.imageUrl)
                        .setAutoPlayAnimations(true)
                        .build()
                draweeView.aspectRatio = this.imageAspectRatio
                draweeView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * This method adds button information, then set it to visible.
     */
    private fun setButtonInfo(buttonView: MaterialButton, button: MessageButton) {
        buttonView.text = button.buttonText
        buttonView.setTextColor(Color.parseColor(button.buttonTextColor))
        buttonView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(button.buttonBackgroundColor))
        // Button stroke color equals to button text color.
        buttonView.strokeColor = ColorStateList.valueOf(Color.parseColor(button.buttonTextColor))
        buttonView.setOnClickListener(this.listener)
        buttonView.visibility = View.VISIBLE
        message_buttons.visibility = View.VISIBLE
    }

    /**
     * This method binds data to message header.
     */
    @SuppressLint("ClickableViewAccessibility")
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    @Suppress("LongMethod")
    private fun bindText() {
        if (!header.isNullOrEmpty() || !messageBody.isNullOrEmpty()) {
            val scrollView = message_scrollview
            if (scrollView != null) {
                scrollView.visibility = View.VISIBLE
            }
        }
        if (!header.isNullOrEmpty()) {
            val headerTextView = header_text
            if (headerTextView != null) {
                headerTextView.text = header
                headerTextView.setTextColor(headerColor)
                headerTextView.setOnTouchListener(listener)
                headerTextView.visibility = View.VISIBLE
            }
        }
        if (!messageBody.isNullOrEmpty()) {
            val messageBodyTextView = message_body
            if (messageBodyTextView != null) {
                messageBodyTextView.text = messageBody
                messageBodyTextView.setTextColor(messageBodyColor)
                messageBodyTextView.setOnTouchListener(listener)
                messageBodyTextView.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val IMAGE_WEIGHT_LANDSCAPE = 0.5f
    }
}
