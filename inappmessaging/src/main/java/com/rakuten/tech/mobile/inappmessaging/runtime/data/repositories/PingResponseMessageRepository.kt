package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants

/**
 * Contains all downloaded messages from Ping Message Mixer. Also exposed internal api which can get
 * these messages.
 */
internal abstract class PingResponseMessageRepository : MessageRepository {
    // Server returned UTC time in last `/Ping` request response.
    var lastPingMillis: Long = 0

    companion object {
        private var instance: PingResponseMessageRepository = PingResponseMessageRepositoryImpl()

        fun instance(): PingResponseMessageRepository = instance
    }

    private class PingResponseMessageRepositoryImpl : PingResponseMessageRepository() {
        // LinkedHashMap can preserve the message insertion order.
        // Map - Key: Campaign ID, Value: Message object
        private var messages: MutableMap<String, Message> = LinkedHashMap()

        /**
         * Replacing all new messages to the [messageList].
         * If messageList empty, IllegalArgumentException will be thrown.
         */
        @Throws(IllegalArgumentException::class)
        override fun replaceAllMessages(messageList: List<Message>?) {
            requireNotNull(messageList) { InAppMessagingConstants.ARGUMENT_IS_NULL_EXCEPTION }
            messages = LinkedHashMap()
            for (message in messageList) {
                messages[message.getCampaignId()!!] = message
            }
        }

        /**
         * This method returns a copy of all messages are in the current repository.
         */
        override fun getAllMessagesCopy(): List<Message> = ArrayList(messages.values)
    }
}
