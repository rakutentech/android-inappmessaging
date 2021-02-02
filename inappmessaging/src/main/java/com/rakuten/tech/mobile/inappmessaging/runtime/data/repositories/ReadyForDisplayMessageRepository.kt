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

        @Throws(IllegalArgumentException::class)
        override fun replaceAllMessages(messageList: List<Message>?) {
            require(messageList != null) { InAppMessagingConstants.ARGUMENT_IS_NULL_EXCEPTION }
            // Preventing race condition.
            synchronized(messages) {
                messages.clear()
                messages.addAll(messageList)
            }
        }

        override fun getAllMessagesCopy(): List<Message> = ArrayList(messages)

        override fun removeMessage(campaignId: String, shouldIncrementTimesClosed: Boolean) {
            synchronized(messages) {
                messages.removeAll { message ->
                    if (message.getCampaignId() == campaignId) {
                        // messages contain unique message (no two message have the same campaign id)
                        if (shouldIncrementTimesClosed) {
                            PingResponseMessageRepository.instance().incrementTimesClosed(listOf(message))
                        }
                        true
                    } else {
                        false
                    }
                }
            }
        }

        override fun clearMessages(shouldIncrementTimesClosed: Boolean) {
            synchronized(messages) {
                if (shouldIncrementTimesClosed) {
                    PingResponseMessageRepository.instance().incrementTimesClosed(messages)
                }
                messages.clear()
            }
        }
    }
}
