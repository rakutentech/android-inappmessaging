package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.CampaignData
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.ImpressionManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import org.json.JSONArray
import java.lang.ClassCastException

/**
 * Contains all messages are ready for display, but not yet displayed.
 */
@SuppressWarnings("UnnecessaryAbstractClass")
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

        override fun replaceAllMessages(messageList: List<Message>) {
            // Preventing race condition.
            synchronized(messages) {
                messages.clear()
                ImpressionManager.impressionMap.clear() // clear impression map when ready messages are replaced
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
                ImpressionManager.impressionMap.clear() // clear impression map when ready messages are cleared
                saveUpdatedList()
            }
        }

        @SuppressWarnings("TooGenericExceptionCaught", "LongMethod")
        private fun checkAndResetList(onLaunch: Boolean = false) {
            if (InAppMessaging.instance().isLocalCachingEnabled() &&
                (onLaunch || user != AccountRepository.instance().userInfoHash)
            ) {
                user = AccountRepository.instance().userInfoHash
                // reset message list from cached using updated user info
                val listString = try {
                    InAppMessaging.instance().getHostAppContext()?.let { ctx ->
                        PreferencesUtil.getString(
                            context = ctx,
                            name = InAppMessaging.getPreferencesFile(),
                            key = READY_DISPLAY_KEY,
                            defValue = ""
                        )
                    }.orEmpty()
                } catch (ex: ClassCastException) {
                    InAppLogger(TAG).debug(ex.cause, "Incorrect type for $READY_DISPLAY_KEY data")
                    ""
                }

                messages.clear()
                try {
                    val jsonArray = JSONArray(listString)
                    for (i in 0 until jsonArray.length()) {
                        messages.add(Gson().fromJson(jsonArray.getJSONObject(i).toString(), CampaignData::class.java))
                    }
                } catch (ex: Exception) {
                    InAppLogger(TAG).debug(ex.cause, "Invalid JSON format for $READY_DISPLAY_KEY data")
                }
            }
        }

        private fun saveUpdatedList() {
            // check if caching is enabled to update persistent data
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                // reset updated message list
                InAppMessaging.instance().getHostAppContext()?.let { ctx ->
                    PreferencesUtil.putString(
                        context = ctx,
                        name = InAppMessaging.getPreferencesFile(),
                        key = READY_DISPLAY_KEY,
                        value = Gson().toJson(messages)
                    )
                } ?: InAppLogger(TAG).debug("failed saving ready data")
            }
        }
    }
}
