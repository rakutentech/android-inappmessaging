package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.inappmessaging.runtime.InApp
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import timber.log.Timber
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
     * Sets the last message campaign ID in the repository which was closed after unregistering activity.
     */
    fun setRemovedMessage(id: String?)

    /**
     * Return the number of times this message has been displayed in this session.
     * When message is null or message's campaignId is empty, return 0.
     */
    fun numberOfTimesDisplayed(message: Message): Int

    /**
     * Returns the number of times the campaign ID was closed after unregistering activity.
     */
    fun numberOfTimesClosed(id: String): Int

    /**
     * This method removes all stored messages.
     * This is done during session update due to user info update.
     */
    fun clearMessages()

    companion object {
        private var instance: LocalDisplayedMessageRepository = LocalDisplayedMessageRepositoryImpl()
        @VisibleForTesting
        internal const val LOCAL_DISPLAYED_KEY = "local_displayed_list"
        @VisibleForTesting
        internal const val LOCAL_DISPLAYED_CLOSED_KEY = "local_displayed_closed"
        @VisibleForTesting
        internal const val LOCAL_DISPLAYED_CLOSED_LIST_KEY = "local_displayed_closed_list"
        private const val TAG = "IAM_LocalDisplayRepo"

        internal var isInitialLaunch = false

        fun instance() = instance
    }

    private class LocalDisplayedMessageRepositoryImpl : LocalDisplayedMessageRepository {
        // Displayed message campaign ID and a list of the epoch time in UTC this message was displayed.
        // Such as:
        // {5bf41c52-e4c0-4cb2-9183-df429e84d681, [1537309879557,1537309879557,1537309879557]}
        private val messages = ConcurrentHashMap<String, List<Long>>()
        private val removedMessages = ConcurrentHashMap<String, Int>()
        private var removedMessage = ""
        private var user = ""

        init {
            checkAndResetMap(true)
        }

        @SuppressWarnings("LongMethod")
        override fun addMessage(message: Message) {
            if (message.getCampaignId().isNullOrEmpty()) {
                InApp.errorCallback?.let {
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
                    messages[campaignId!!] = timeStamps
                } else {
                    val timeStamps = ArrayList(messages[campaignId]!!)
                    timeStamps.add(Calendar.getInstance().timeInMillis)
                    messages[campaignId!!] = timeStamps
                }
                saveUpdatedMap()
            }
        }

        override fun setRemovedMessage(id: String?) {
            checkAndResetMap()
            removedMessage = id ?: ""
            saveUpdatedMap()
        }

        override fun numberOfTimesClosed(id: String): Int {
            synchronized(removedMessages) {
                if (isInitialLaunch && removedMessage.isNotEmpty()) {
                    isInitialLaunch = false
                    // only increment if campaign is removed then relaunch
                    removedMessages[removedMessage] = removedMessages.getOrElse(removedMessage) { 0 } + 1
                    saveUpdatedMap()
                } else {
                    checkAndResetMap()
                }
                return removedMessages[id] ?: 0
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
            messages.clear()
            removedMessages.clear()
            removedMessage = ""
            saveUpdatedMap()
        }

        @SuppressWarnings("LongMethod", "ComplexMethod", "TooGenericExceptionCaught")
        private fun checkAndResetMap(onLaunch: Boolean = false) {
            // check if caching is enabled and if there are changes in user info
            if (InAppMessaging.instance().isLocalCachingEnabled() &&
                    (onLaunch || user != AccountRepository.instance().userInfoHash)) {
                user = AccountRepository.instance().userInfoHash
                // reset message list from cached using updated user info
                val sharedPref = InAppMessaging.instance().getSharedPref()

                resetDisplayed(sharedPref)
                resetRemovedMessages(sharedPref)
                resetRemovedMessage(sharedPref)
            }
        }

        private fun resetRemovedMessage(sharedPref: SharedPreferences?) {
            removedMessage = try {
                sharedPref?.getString(LOCAL_DISPLAYED_CLOSED_KEY, "") ?: ""
            } catch (ex: ClassCastException) {
                Timber.tag(TAG).d(ex.cause, "Incorrect type for $LOCAL_DISPLAYED_CLOSED_KEY data")
                ""
            }
        }

        private fun resetRemovedMessages(sharedPref: SharedPreferences?) {
            val removedList = try {
                sharedPref?.getString(LOCAL_DISPLAYED_CLOSED_LIST_KEY, "") ?: ""
            } catch (ex: ClassCastException) {
                Timber.tag(TAG).d(ex.cause, "Incorrect type for $LOCAL_DISPLAYED_CLOSED_LIST_KEY data")
                ""
            }
            removedMessages.clear()
            if (removedList.isNotEmpty()) {
                val type = object : TypeToken<HashMap<String, Int>>() {}.type
                try {
                    removedMessages.putAll(Gson().fromJson(removedList, type))
                } catch (ex: Exception) {
                    Timber.tag(TAG).d(ex.cause, "Incorrect JSON format for $LOCAL_DISPLAYED_CLOSED_LIST_KEY data")
                }
            }
        }

        private fun resetDisplayed(sharedPref: SharedPreferences?) {
            val listString = try {
                sharedPref?.getString(LOCAL_DISPLAYED_KEY, "") ?: ""
            } catch (ex: ClassCastException) {
                Timber.tag(TAG).d(ex.cause, "Incorrect type for $LOCAL_DISPLAYED_KEY data")
                ""
            }
            messages.clear()
            if (listString.isNotEmpty()) {
                val type = object : TypeToken<HashMap<String, List<Long>>>() {}.type
                try {
                    messages.putAll(Gson().fromJson(listString, type))
                } catch (ex: Exception) {
                    Timber.tag(TAG).d(ex.cause, "Incorrect JSON format for $LOCAL_DISPLAYED_KEY data")
                }
            }
        }

        private fun saveUpdatedMap() {
            // check if caching is enabled to update persistent data
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                // reset message list from cached using updated user info
                val editor = InAppMessaging.instance().getSharedPref()?.edit()
                editor?.putString(LOCAL_DISPLAYED_KEY, Gson().toJson(messages))
                        ?.putString(LOCAL_DISPLAYED_CLOSED_KEY, removedMessage)
                        ?.putString(LOCAL_DISPLAYED_CLOSED_LIST_KEY, Gson().toJson(removedMessages))
                        ?.apply() ?: Timber.tag(TAG).d("failed saving displayed data")
            }
        }
    }
}
