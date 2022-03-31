package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import java.lang.ClassCastException
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
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
     */
    fun addMessage(message: Message)

    /**
     * Return the number of times this message has been displayed in this session.
     * When message is null or message's campaignId is empty, return 0.
     */
    fun numberOfTimesDisplayed(message: Message): Int

    /**
     * Return the number of times this message has been displayed in this session
     * after the last ping request response.
     *
     * @param message the given InApp campaign message.
     *
     * @return the number of times this message has been displayed,
     * when message is null or message's campaignId is empty, return 0.
     */
    fun numberOfDisplaysAfterPing(message: Message): Int

    /**
     * This method removes all stored messages.
     * This is done during session update due to user info update.
     */
    fun clearMessages()

    companion object {
        private var instance: LocalDisplayedMessageRepository = LocalDisplayedMessageRepositoryImpl()

        @VisibleForTesting
        internal const val LOCAL_DISPLAYED_KEY = "local_displayed_list"
        private const val TAG = "IAM_LocalDisplayRepo"

        fun instance() = instance
    }

    @SuppressWarnings("TooManyFunctions")
    private class LocalDisplayedMessageRepositoryImpl : LocalDisplayedMessageRepository {
        // Displayed message campaign ID and a list of the epoch time in UTC this message was displayed.
        // Such as:
        // {5bf41c52-e4c0-4cb2-9183-df429e84d681, [1537309879557,1537309879557,1537309879557]}
        private val messages = ConcurrentHashMap<String, List<Long>>()
        private var user = ""

        init {
            checkAndResetMap(true)
        }

        @SuppressWarnings("LongMethod")
        override fun addMessage(message: Message) {
            if (message.getCampaignId().isEmpty()) {
                InAppMessaging.errorCallback?.let {
                    it(InAppMessagingException("In-App Messaging storing campaign failed due to invalid value"))
                }
                return
            }

            // Add a message to repository with time stamp.
            val campaignId = message.getCampaignId()

            // Prevents race condition.
            synchronized(messages) {
                checkAndResetMap()

                if (!messages.containsKey(campaignId)) {
                    val timeStamps = ArrayList<Long>()
                    timeStamps.add(Calendar.getInstance().timeInMillis)
                    messages[campaignId] = timeStamps
                } else {
                    val timeStamps = ArrayList(messages[campaignId]!!)
                    timeStamps.add(Calendar.getInstance().timeInMillis)
                    messages[campaignId] = timeStamps
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

        /**
         * {@inheritDoc}.
         */
        override fun numberOfDisplaysAfterPing(message: Message): Int {
            synchronized(messages) {
                val lastPingMillis = PingResponseMessageRepository.instance().lastPingMillis
                val messageTimeStampList = messages[message.getCampaignId()]?.filter {
                    it >= lastPingMillis
                } ?: mutableListOf()
                return messageTimeStampList.size
            }
        }

        override fun clearMessages() {
            messages.clear()
            saveUpdatedMap()
        }

        @SuppressWarnings("LongMethod", "ComplexMethod", "TooGenericExceptionCaught")
        private fun checkAndResetMap(onLaunch: Boolean = false) {
            // check if caching is enabled and if there are changes in user info
            if (InAppMessaging.instance().isLocalCachingEnabled() &&
                (onLaunch || user != AccountRepository.instance().userInfoHash)
            ) {
                user = AccountRepository.instance().userInfoHash
                // reset message list from cached using updated user info
                resetDisplayed()
            }
        }

        @SuppressWarnings("TooGenericExceptionCaught", "LongMethod")
        private fun resetDisplayed() {
            val listString = try {
                InAppMessaging.instance().getHostAppContext()?.let { it ->
                    PreferencesUtil.getString(
                        it,
                        InAppMessaging.getPreferencesFile(),
                        LOCAL_DISPLAYED_KEY,
                        ""
                    )
                } ?: ""
            } catch (ex: ClassCastException) {
                Logger(TAG).debug(ex.cause, "Incorrect JSON format for $LOCAL_DISPLAYED_KEY data")
                ""
            }
            messages.clear()
            if (listString.isNotEmpty()) {
                val type = object : TypeToken<HashMap<String, List<Long>>>() {}.type
                try {
                    messages.putAll(Gson().fromJson(listString, type))
                } catch (ex: Exception) {
                    Logger(TAG).debug(ex.cause, "Incorrect JSON format for $LOCAL_DISPLAYED_KEY data")
                }
            }
        }

        private fun saveUpdatedMap() {
            // check if caching is enabled to update persistent data
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                // reset message list from cached using updated user info
                InAppMessaging.instance().getHostAppContext()?.let {
                    PreferencesUtil.putString(
                        it,
                        InAppMessaging.getPreferencesFile(),
                        LOCAL_DISPLAYED_KEY,
                        Gson().toJson(messages)
                    )
                } ?: Logger(TAG).debug("failed saving displayed data")
            }
        }
    }
}
