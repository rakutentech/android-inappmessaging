package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.CampaignData
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.json.JSONArray
import timber.log.Timber
import java.lang.ClassCastException

/**
 * Contains all messages are ready for display, but not yet displayed.
 */
internal abstract class ReadyForDisplayMessageRepository : ReadyMessageRepository {

    companion object {
        private var instance: ReadyForDisplayMessageRepository = ReadyForDisplayMessageRepositoryImpl()
        @VisibleForTesting
        internal const val READY_DISPLAY_KEY = "ready_display_list"
        private const val TAG = "IAM_ReadyDisplayRepo"

        fun instance(): ReadyForDisplayMessageRepository = instance
    }

    private class ReadyForDisplayMessageRepositoryImpl : ReadyForDisplayMessageRepository() {
        // Oldest message should be displayed first, Deque offers the flexibility to add object to head or tail.
        private val messages: MutableList<Message> = ArrayList()
        private var user = ""

        init {
            synchronized(messages) {
                checkAndResetList(true)
            }
        }

        @Throws(IllegalArgumentException::class)
        override fun replaceAllMessages(messageList: List<Message>?) {
            require(messageList != null) { InAppMessagingConstants.ARGUMENT_IS_NULL_EXCEPTION }
            // Preventing race condition.
            synchronized(messages) {
                messages.clear()
                messages.addAll(messageList)
                saveUpdatedList()
            }
        }

        override fun getAllMessagesCopy(): List<Message> {
            synchronized(messages) {
                checkAndResetList()
                return ArrayList(messages)
            }
        }

        override fun removeMessage(campaignId: String, shouldIncrementTimesClosed: Boolean) {
            synchronized(messages) {
                // check if caching is enabled and if there are changes in user info
                checkAndResetList()
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
                saveUpdatedList()
            }
        }

        override fun clearMessages(shouldIncrementTimesClosed: Boolean) {
            synchronized(messages) {
                if (shouldIncrementTimesClosed) {
                    PingResponseMessageRepository.instance().incrementTimesClosed(messages)
                }
                messages.clear()
                saveUpdatedList()
            }
        }

        private fun checkAndResetList(onLaunch: Boolean = false) {
            if (InAppMessaging.instance().isLocalCachingEnabled() &&
                    (onLaunch || user != AccountRepository.instance().userInfoHash)) {
                user = AccountRepository.instance().userInfoHash
                // reset message list from cached using updated user info
                val listString = try {
                    InAppMessaging.instance().getSharedPref()?.getString(READY_DISPLAY_KEY, "") ?: ""
                } catch (ex: ClassCastException) {
                    Timber.tag(TAG).d(ex.cause, "Incorrect type for $READY_DISPLAY_KEY data")
                    ""
                }

                messages.clear()
                if (listString.isNotEmpty()) {
                    val jsonArray = JSONArray(listString)
                    for (i in 0 until jsonArray.length()) {
                        messages.add(Gson().fromJson(jsonArray.getJSONObject(i).toString(), CampaignData::class.java))
                    }
                }
            }
        }

        private fun saveUpdatedList() {
            // check if caching is enabled to update persistent data
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                // reset updated message list
                InAppMessaging.instance().getSharedPref()?.edit()?.putString(READY_DISPLAY_KEY,
                        Gson().toJson(messages))?.apply() ?: Timber.tag(TAG).d("failed saving ready data")
            }
        }
    }
}
