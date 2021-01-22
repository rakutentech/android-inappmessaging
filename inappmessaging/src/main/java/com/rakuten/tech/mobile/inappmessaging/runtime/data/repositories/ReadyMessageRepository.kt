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
    @Throws(IllegalArgumentException::class)
    fun replaceAllMessages(messageList: List<Message>?)

    /**
     * This method returns a copy of all the messages.
     */
    fun getAllMessagesCopy(): List<Message>

    /**
     * Removing a message from the repository.
     */
    fun removeMessage(campaignId: String): Message

    /**
     * Clears all message from the repository.
     * This is done during session update due to user info update.
     */
    fun clearMessages()
}
