package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ViewUtil
import kotlinx.android.synthetic.main.close_button.view.*
import kotlinx.android.synthetic.main.in_app_message_slide_up.view.*

/**
 * This is a custom view that extends from InAppMessageBaseView.
 */
internal class InAppMessageSlideUpView(
    context: Context,
    attrs: AttributeSet?
) :
    InAppMessageBaseView(context, attrs) {

    /**
     * Populating view data according to Slide Up view.
     */
    override fun populateViewData(message: Message, imageAspectRatio: Float) {
        super.populateViewData(message, imageAspectRatio)

        // Override image from white background to black background.
        (message_close_button as ImageButton).setImageResource(R.drawable.close_button_black_background)

        val constraintLayout = slide_up

        // Setting background color.
        constraintLayout.setBackgroundColor(bgColor)
        // Start animation based on direction.
        constraintLayout.startAnimation(
                ViewUtil.getSlidingAnimation(
                        context,
                        SlideFromDirectionType.getById(
                                message.getMessagePayload()?.messageSettings?.displaySettings?.slideFrom!!)))
        // Set listener for special handling of the invisible constraints(button) click.
        slide_up.setOnClickListener(listener)
    }
}
