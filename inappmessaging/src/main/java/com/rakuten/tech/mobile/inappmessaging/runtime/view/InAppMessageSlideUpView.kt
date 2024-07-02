package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.ui.UiMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ViewUtil

/**
 * This is a custom view that extends from InAppMessageBaseView.
 */
internal class InAppMessageSlideUpView(
    context: Context,
    attrs: AttributeSet?,
) :
    InAppMessageBaseView(context, attrs) {

    /**
     * Populating view data according to Slide Up view.
     */
    override fun populateViewData(uiMessage: UiMessage) {
        super.populateViewData(uiMessage)

        setCloseButton()
        val constraintLayout = findViewById<ConstraintLayout>(R.id.slide_up)

        // Setting background color.
        constraintLayout.setBackgroundColor(bgColor)
        // Start animation based on direction.
        ViewUtil.getSlidingAnimation(
            context,
            SlideFromDirectionType.getById(
                uiMessage.displaySettings.slideFrom,
            ),
        )?.let { animation ->
            constraintLayout.startAnimation(animation)
        }
        // Set listener for special handling of the invisible constraints(button) click.
        constraintLayout.setOnClickListener(listener)
    }
}
