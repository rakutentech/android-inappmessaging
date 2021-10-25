package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message

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
    override fun populateViewData(message: Message) {
        super.populateViewData(message)
        findViewById<LinearLayout>(R.id.modal)?.setBackgroundColor(bgColor)
    }
}
