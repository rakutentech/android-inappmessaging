package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import kotlinx.android.synthetic.main.close_button.view.*
import kotlinx.android.synthetic.main.in_app_message_full_screen.view.*

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
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    override fun populateViewData(message: Message) {
        super.populateViewData(message)
        if (imageUrl.isNullOrEmpty()) {
            // If no image, use @drawable/close_button_black_background.
            (message_close_button as ImageButton).setImageResource(R.drawable.close_button_black_background)

            // If no image, add empty text spacer on top.
            view_spacer.visibility = View.VISIBLE
        }
        setBackgroundColor(bgColor)
    }
}
