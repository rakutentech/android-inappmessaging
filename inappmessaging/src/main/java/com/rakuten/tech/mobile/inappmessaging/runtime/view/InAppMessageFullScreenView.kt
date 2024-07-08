package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.content.Context
import android.util.AttributeSet
import com.rakuten.tech.mobile.inappmessaging.runtime.data.ui.UiMessage

/**
 * This is the custom view which represents Full Screen message.
 */
internal class InAppMessageFullScreenView(
    context: Context,
    attrs: AttributeSet?,
) :
    InAppMessageBaseView(context, attrs) {

    /**
     * Populating view data.
     */
    override fun populateViewData(uiMessage: UiMessage) {
        super.populateViewData(uiMessage)

        setCloseButton()
        setBackgroundColor(bgColor)
    }
}
