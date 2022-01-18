package com.rakuten.tech.mobile.inappmessaging.runtime.view

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message

/**
 * Interface for the InApp custom views to implement.
 */
internal interface InAppMessageView {
    /**
     * This method sets campaign message data int the custom view classes.
     * Message must not be null.
     */
    fun populateViewData(message: Message)
}
