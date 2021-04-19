package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import timber.log.Timber
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
     * This method removes all stored messages.
     * This is done during session update due to user info update.
     */
    fun clearMessages()

    companion object {
        private var instance: LocalDisplayedMessageRepository = LocalDisplayedMessageRepositoryImpl()
        private const val LOCAL_DISPLAYED_KEY = "local_displayed_list"
        private const val TAG = "IAM_LocalEventRepo"

        fun instance() = instance
    }

    private class LocalDisplayedMessageRepositoryImpl : LocalDisplayedMessageRepository {
        // Displayed message campaign ID and a list of the epoch time in UTC this message was displayed.
        // Such as:
        // {5bf41c52-e4c0-4cb2-9183-df429e84d681, [1537309879557,1537309879557,1537309879557]}
        private val messages = HashMap<String, List<Long>>()
        private var user = ""

        init {
            checkAndResetMap(true)
        }

        @Throws(IllegalArgumentException::class)
        override fun addMessage(message: Message?) {
            requireNotNull(message) { InAppMessagingConstants.ARGUMENT_IS_NULL_EXCEPTION }
            require(!message.getCampaignId().isNullOrEmpty()) { InAppMessagingConstants.ARGUMENT_IS_EMPTY_EXCEPTION }

            // Add a message to repository with time stamp.
            val campaignId = message.getCampaignId()

            // Prevents race condition.
            synchronized(messages) {
                checkAndResetMap()

                if (!messages.containsKey(campaignId)) {
                    val timeStamps = ArrayList<Long>()
                    timeStamps.add(Calendar.getInstance().timeInMillis)
                    messages[campaignId!!] = timeStamps
                } else {
                    val timeStamps = ArrayList(messages[campaignId]!!)
                    timeStamps.add(Calendar.getInstance().timeInMillis)
                    messages[campaignId!!] = timeStamps
                }
                saveUpdatedMap()
            }
        }

        override fun numberOfTimesDisplayed(message: Message): Int {
            synchronized(messages) {
                checkAndResetMap()
                // Prevents race condition.
                val messageTimeStampList = messages[message.getCampaignId()]
                return messageTimeStampList?.size ?: 0
            }
        }

        override fun clearMessages() {
            if (messages.isNotEmpty()) {
                messages.clear()
                saveUpdatedMap()
            }
        }

        private fun checkAndResetMap(onLaunch: Boolean = false) {
            // check if caching is enabled and if there are changes in user info
            if (InAppMessaging.instance().isLocalCachingEnabled() &&
                    (onLaunch || user != AccountRepository.instance().userInfoHash)) {
                user = AccountRepository.instance().userInfoHash
                // reset message list from cached using updated user info
                val listString = InAppMessaging.instance().getEncryptedSharedPref()?.getString(LOCAL_DISPLAYED_KEY, "")
                        ?: ""
                messages.clear()
                if (listString.isNotEmpty()) {
                    val type = object : TypeToken<HashMap<String, List<Long>>>() {}.type
                    messages.putAll(Gson().fromJson(listString, type))
                }
            }
        }

        private fun saveUpdatedMap() {
            // check if caching is enabled to update persistent data
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                // reset message list from cached using updated user info
                InAppMessaging.instance().getEncryptedSharedPref()?.edit()?.putString(LOCAL_DISPLAYED_KEY,
                        Gson().toJson(messages))?.apply() ?: Timber.tag(TAG).d("failed saving map")
            }
        }
    }
}
