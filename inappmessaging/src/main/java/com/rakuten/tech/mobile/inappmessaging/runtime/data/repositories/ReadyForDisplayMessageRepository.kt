package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants

/**
 * Contains all messages are ready for display, but not yet displayed.
 */
internal abstract class ReadyForDisplayMessageRepository : ReadyMessageRepository {

    companion object {
        private var instance: ReadyForDisplayMessageRepository = ReadyForDisplayMessageRepositoryImpl()

        fun instance(): ReadyForDisplayMessageRepository = instance
    }

    private class ReadyForDisplayMessageRepositoryImpl : ReadyForDisplayMessageRepository() {
        // Oldest message should be displayed first, Deque offers the flexibility to add object to head or tail.
        private val messages: MutableList<Message> = ArrayList()

        /**
         * Replacing with a new list of messages.
         * IllegalArgumentException will be thrown if messageList is empty.
         */
        @Throws(IllegalArgumentException::class)
        override fun replaceAllMessages(messageList: List<Message>?) {
            require(messageList != null) { InAppMessagingConstants.ARGUMENT_IS_NULL_EXCEPTION }
            // Preventing race condition.
            synchronized(messages) {
                messages.clear()
                messages.addAll(messageList)
            }
        }

        /**
         * This method returns a copy of the message list.
         */
        override fun getAllMessagesCopy(): List<Message> = ArrayList(messages)

        /**
         * This method removes a message based on argument campaignId.
         */
        override fun removeMessage(campaignId: String) {
            synchronized(messages) {
                messages.removeAll { message ->
                    message.getCampaignId() == campaignId
                }
            }
        }
    }
}
