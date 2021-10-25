package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message

/**
 * This is the custom view which represents Full Screen message.
 */
internal class InAppMessageFullScreenView(
    context: Context,
    attrs: AttributeSet?
) :
    InAppMessageBaseView(context, attrs) {

    /**
     * Populating view data.
     */
    override fun populateViewData(message: Message) {
        super.populateViewData(message)
        if (imageUrl.isNullOrEmpty()) {
            // If no image, use @drawable/close_button_black_background.
            findViewById<ImageButton>(R.id.message_close_button)
                ?.setImageResource(R.drawable.close_button_black_background)

            // If no image, add empty text spacer on top.
            findViewById<View>(R.id.view_spacer)?.visibility = View.VISIBLE
        }
        setBackgroundColor(bgColor)
    }
}
