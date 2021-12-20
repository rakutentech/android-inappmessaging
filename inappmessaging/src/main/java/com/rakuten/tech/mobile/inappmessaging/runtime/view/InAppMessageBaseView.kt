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
import android.widget.ImageView
import androidx.annotation.VisibleForTesting
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageButton
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ViewUtil
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import timber.log.Timber
import java.lang.Exception

/**
 * Base class of all custom views.
 */
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
    private var displayOptOut = false
    @VisibleForTesting
    internal var picasso: Picasso? = null

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
        when (this.buttons?.size) {
            1 -> {
                // Set bigger layout_margin if there's only one button.
                findViewById<MaterialButton>(R.id.message_single_button)?.let {
                    setButtonInfo(it, this.buttons!![0])
                }
            }
            2 -> {
                // Set bigger layout_margin if there's only one button.
                findViewById<MaterialButton>(R.id.message_button_left)?.let { setButtonInfo(it, this.buttons!![0]) }
                findViewById<MaterialButton>(R.id.message_button_right)?.let { setButtonInfo(it, this.buttons!![1]) }
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
            findViewById<ImageView>(R.id.message_image_view)?.let {
                it.setOnTouchListener(this.listener)
                try {
                    val callback = object : Callback {
                        override fun onSuccess() {
                            it.visibility = VISIBLE
                            this@InAppMessageBaseView.visibility = VISIBLE
                        }

                        override fun onError(e: Exception?) {
                            Timber.tag(TAG).d(e?.cause, "Downloading image failed $imageUrl")
                        }
                    }
                    (picasso ?: Picasso.get()).load(this.imageUrl)
                        .priority(Picasso.Priority.HIGH)
                        .resize(ViewUtil.getDisplayWidth(context), 0)
                        .onlyScaleDown()
                        .centerInside()
                        .into(it, callback)
                } catch (ex: Exception) {
                    Timber.tag(TAG).d(ex, "Downloading image failed $imageUrl")
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
        if (!header.isNullOrEmpty()) {
            findViewById<TextView>(R.id.header_text)?.let {
                it.text = header
                it.setTextColor(headerColor)
                it.setOnTouchListener(listener)
                it.visibility = View.VISIBLE
            }
        }
        if (!messageBody.isNullOrEmpty()) {
            findViewById<TextView>(R.id.message_body)?.let {
                it.text = messageBody
                it.setTextColor(messageBodyColor)
                it.setOnTouchListener(listener)
                it.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val TAG = "IAM_BaseView"
    }
}
