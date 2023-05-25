package com.rakuten.tech.mobile.inappmessaging.runtime.view

import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message

/**
 * Interface for the InApp custom views to implement.
 */
internal fun interface InAppMessageView {
    /**
     * This method sets campaign message data int the custom view classes.
     * Message must not be null.
     */
    fun populateViewData(message: Message)
}
