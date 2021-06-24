package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.RestrictTo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message

/**
 * Message Repositories interface.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal interface MessageRepository {
    /**
     * This method replaces a new list of messages into repository.
     */
    fun replaceAllMessages(messageList: List<Message>)

    /**
     * This method returns a copy of all messages in the repository.
     */
    fun getAllMessagesCopy(): List<Message>

    /**
     * Clears all message from the repository.
     * This is done during session update due to user info update.
     */
    fun clearMessages()

    /**
     * Increments the number of times closed while in queue (ready for display)
     * for all the messages in the [messageList].
     */
    fun incrementTimesClosed(messageList: List<Message>)

    /**
     * Checks if campaign with only App Launch event should still be displayed.
     */
    fun shouldDisplayAppLaunchCampaign(id: String): Boolean
}
