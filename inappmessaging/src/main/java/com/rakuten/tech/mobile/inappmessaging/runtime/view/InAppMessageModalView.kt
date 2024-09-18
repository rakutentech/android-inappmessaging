package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.VisibleForTesting
import androidx.core.graphics.ColorUtils
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.ui.UiMessage

/**
 * This is a custom view that extends from InAppMessageBaseView.
 */
internal class InAppMessageModalView(
    context: Context,
    attrs: AttributeSet?,
) :
    InAppMessageBaseView(context, attrs) {

    /**
     * Sets campaign message data onto the view.
     */
    override fun populateViewData(uiMessage: UiMessage) {
        super.populateViewData(uiMessage)

        setCloseButton()
        setBackdropColor(uiMessage.backdropOpacity)
        findModalLayout()?.setBackgroundColor(bgColor)
    }

    @VisibleForTesting
    fun findModalLayout(): LinearLayout? = findViewById(R.id.modal)

    private fun setBackdropColor(opacity: Float?) {
        if (opacity == null ||
            opacity < 0 || opacity > 1) {
            // The default color(R.color.in_app_message_frame_light) will be set through XML.
            return
        }

        val blackWithAlpha = ColorUtils.setAlphaComponent(Color.BLACK, (0.6 * 255).toInt())
        this.setBackgroundColor(blackWithAlpha)
    }
}
