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
        // The default color(R.color.in_app_message_frame_light) will be set through XML.
        if (opacity == null ||
            opacity !in 0f..1f
        ) {
            return
        }

        val blackWithAlpha = ColorUtils.setAlphaComponent(Color.BLACK, (opacity * MAX_COLOR_ALPHA).toInt())
        this.setBackgroundColor(blackWithAlpha)
    }

    companion object {
        const val MAX_COLOR_ALPHA = 255
    }
}
