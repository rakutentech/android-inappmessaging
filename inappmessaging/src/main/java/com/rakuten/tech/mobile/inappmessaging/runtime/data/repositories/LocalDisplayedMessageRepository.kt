package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import java.util.Calendar
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

/**
 * Contains all displayed messages and their time stamps. To avoid clients modifying data in an
 * unexpected manner, some necessary utility methods are provided.
 */
internal interface LocalDisplayedMessageRepository {

    /**
     * This method adds a message campaign ID with time stamp in the repository.
     * Throws IllegalArgumentException if argument empty.
     */
    @Throws(IllegalArgumentException::class)
    fun addMessage(message: Message?)

    /**
     * Return the number of times this message has been displayed in this session.
     * When message is null or message's campaignId is empty, return 0.
     */
    fun numberOfTimesDisplayed(message: Message): Int

    /**
     * This method removes all stored messages for testing.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun clearMessages()

    companion object {
        private var instance: LocalDisplayedMessageRepository = LocalDisplayedMessageRepositoryImpl()

        fun instance() = instance
    }

    private class LocalDisplayedMessageRepositoryImpl : LocalDisplayedMessageRepository {
        // Displayed message campaign ID and a list of the epoch time in UTC this message was displayed.
        // Such as:
        // {5bf41c52-e4c0-4cb2-9183-df429e84d681, [1537309879557,1537309879557,1537309879557]}
        private val messages = HashMap<String, List<Long>>()

        @Throws(IllegalArgumentException::class)
        override fun addMessage(message: Message?) {
            requireNotNull(message) { InAppMessagingConstants.ARGUMENT_IS_NULL_EXCEPTION }
            require(!message.getCampaignId().isNullOrEmpty()) { InAppMessagingConstants.ARGUMENT_IS_EMPTY_EXCEPTION }

            // Add a message to repository with time stamp.
            val campaignId = message.getCampaignId()
            val currentTimeMilliUtc = Calendar.getInstance().timeInMillis
            val timeStamps: List<Long>
            // Prevents race condition.
            synchronized(messages) {
                if (!messages.containsKey(campaignId)) {
                    timeStamps = ArrayList()
                    timeStamps.add(currentTimeMilliUtc)
                    messages[campaignId!!] = timeStamps
                } else {
                    timeStamps = ArrayList(messages[campaignId]!!)
                    timeStamps.add(currentTimeMilliUtc)
                    messages[campaignId!!] = timeStamps
                }
            }
        }

        override fun numberOfTimesDisplayed(message: Message): Int {

            synchronized(messages) {
                // Prevents race condition.
                val messageTimeStampList = messages[message.getCampaignId()]
                return messageTimeStampList?.size ?: 0
            }
        }

        override fun clearMessages() {
            if (messages.isNotEmpty()) {
                messages.clear()
            }
        }
    }
}
