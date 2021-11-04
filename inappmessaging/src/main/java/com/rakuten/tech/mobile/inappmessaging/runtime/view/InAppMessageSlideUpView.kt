package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ViewUtil

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
    override fun populateViewData(message: Message) {
        super.populateViewData(message)

        // Override image from white background to black background.
        findViewById<ImageButton>(R.id.message_close_button)
            ?.setImageResource(R.drawable.close_button_black_background)

        val constraintLayout = findViewById<ConstraintLayout>(R.id.slide_up)

        // Setting background color.
        constraintLayout?.setBackgroundColor(bgColor)
        // Start animation based on direction.
        val animation = ViewUtil.getSlidingAnimation(
                context,
                SlideFromDirectionType.getById(
                    message.getMessagePayload().messageSettings.displaySettings.slideFrom
                ))
        animation?.let {
            constraintLayout?.startAnimation(it)
        }
        // Set listener for special handling of the invisible constraints(button) click.
        constraintLayout?.setOnClickListener(listener)
    }
}
