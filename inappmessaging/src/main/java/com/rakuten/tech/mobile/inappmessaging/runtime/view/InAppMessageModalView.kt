package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.content.Context
import android.util.AttributeSet
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import kotlinx.android.synthetic.main.in_app_message_modal.view.*

/**
 * This is a custom view that extends from InAppMessageBaseView.
 */
internal class InAppMessageModalView(
    context: Context,
    attrs: AttributeSet?
) :
    InAppMessageBaseView(context, attrs) {

    /**
     * Sets campaign message data onto the view.
     */
    override fun populateViewData(message: Message, imageAspectRatio: Float) {
        super.populateViewData(message, imageAspectRatio)
        modal.setBackgroundColor(bgColor)
    }
}
