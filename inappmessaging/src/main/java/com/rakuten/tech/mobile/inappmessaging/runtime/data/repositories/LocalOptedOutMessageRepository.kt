package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.WorkerThread
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message

/**
 * This class contains opted out messages that user chose not to see it again.
 */
@WorkerThread
internal interface LocalOptedOutMessageRepository {

    /**
     * Adding a message to the opted out message repository.
     */
    fun addMessage(message: Message)

    /**
     * This method checks if message exists in the the opted out repository.
     */
    fun hasMessage(messageCampaignId: String?): Boolean

    companion object {
        private var instance: LocalOptedOutMessageRepository = LocalOptedOutMessageRepositoryImpl()

        fun instance() = instance
    }

    private class LocalOptedOutMessageRepositoryImpl : LocalOptedOutMessageRepository {
        private val optedOutMessages = HashSet<String?>()

        override fun addMessage(message: Message) {
            synchronized(optedOutMessages) {
                optedOutMessages.add(message.getCampaignId())
            }
        }

        override fun hasMessage(messageCampaignId: String?): Boolean {
            synchronized(optedOutMessages) {
                return optedOutMessages.contains(messageCampaignId)
            }
        }
    }
}
