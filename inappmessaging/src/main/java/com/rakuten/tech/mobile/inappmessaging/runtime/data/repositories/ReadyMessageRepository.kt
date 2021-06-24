package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message

/**
 * Sub-interface of MessageRepository, contains getNextMessage(), and removeMessage(Message).
 */
internal interface ReadyMessageRepository {
    /**
     * This method replaces with a new list of messages with [messageList].
     * Throws IllegalArgumentException if messageList argument is empty.
     */
    fun replaceAllMessages(messageList: List<Message>)

    /**
     * This method returns a copy of all the messages.
     */
    fun getAllMessagesCopy(): List<Message>

    /**
     * Removing a message from the repository.
     * @param campaignId id of the message to be removed.
     * @param shouldIncrementTimesClosed if true, the number of times closed while in queue is incremented.
     */
    fun removeMessage(campaignId: String, shouldIncrementTimesClosed: Boolean = false)

    /**
     * Clears all message from the repository. This is called during session update due to user info update,
     * or when clearing of queued (ready for display) messages.
     * @param shouldIncrementTimesClosed if true, the number of times closed while in queue is incremented.
     */
    fun clearMessages(shouldIncrementTimesClosed: Boolean = false)
}
