package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.CampaignData
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import org.json.JSONObject
import java.lang.ClassCastException

/**
 * Contains all downloaded messages from Ping Message Mixer. Also exposed internal api which can get
 * these messages.
 */
internal abstract class CampaignMessageRepository : MessageRepository {
    // Server returned UTC time in last `/Ping` request response.
    var lastSyncMillis: Long = 0

    companion object {
        private var instance: CampaignMessageRepository = CampaignMessageRepositoryImpl()

        @VisibleForTesting
        internal const val PING_RESPONSE_KEY = "ping_response_list"
        private const val TAG = "IAM_PingResponseRepo"

        internal var isInitialLaunch = false

        fun instance(): CampaignMessageRepository = instance
    }

    private class CampaignMessageRepositoryImpl : CampaignMessageRepository() {
        // LinkedHashMap can preserve the message insertion order.
        // Map - Key: Campaign ID, Value: Message object
        private val messages = LinkedHashMap<String, Message>()
        private val appLaunchList = LinkedHashMap<String, Boolean>()
        private var user = ""

        init {
            loadCachedData(true)
        }

        /**
         * Replacing all new messages to the [messageList].
         * If messageList empty, IllegalArgumentException will be thrown.
         */
        override fun syncWith(messageList: List<Message>, timestampMillis: Long) {
            lastSyncMillis = timestampMillis

            loadCachedData()

            messages.clear()
            appLaunchList.clear()
            for (message in messageList) {
                if (message.getCampaignId().isEmpty()) {
                    continue
                }
                messages[message.getCampaignId()] = message
                if (message.getTriggers()?.size == 1) {
                    appLaunchList[message.getCampaignId()] = isInitialLaunch
                }
            }
            isInitialLaunch = false
            saveDataToCache()
        }

        /**
         * This method returns a copy of all messages are in the current repository.
         */
        override fun getAllMessagesCopy(): List<Message> {
            loadCachedData()
            return ArrayList(messages.values)
        }

        override fun clearMessages() {
            messages.clear()
            saveDataToCache()
        }

        // TODO: Should remove
        override fun incrementTimesClosed(messageList: List<Message>) {
            loadCachedData()
            messages.filter { m -> messageList.any { it.getCampaignId() == m.key } }
                .forEach { it.value.incrementTimesClosed() }
            saveDataToCache()
        }

        override fun shouldDisplayAppLaunchCampaign(id: String): Boolean {
            val result = appLaunchList[id]
            appLaunchList[id] = false // it should not be displayed again in current session
            return result ?: false
        }

        /**
         * Opts out the campaign and updates the repository.
         * TODO
         */
        fun optOutCampaign(campaign: Message): Message? {
            return null
        }

        /**
         * Decrements number of impressionsLeft for provided campaign id in the repository.
         * TODO
         */
        fun decrementImpressionsLeftInCampaign(id: String): Message? {
            return null
        }

        /**
         * Increments number of impressionsLeft for provided campaign id in the repository.
         * TODO
         */
        fun incrementImpressionsLeftInCampaign(id: String): Message? {
            return null
        }

        @SuppressWarnings("LongMethod", "TooGenericExceptionCaught")
        private fun loadCachedData(onLaunch: Boolean = false) {
            // check if caching is enabled and if there are changes in user info
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
                            key = PING_RESPONSE_KEY,
                            defValue = ""
                        )
                    }.orEmpty()
                } catch (ex: ClassCastException) {
                    Logger(TAG).debug(ex.cause, "Incorrect type for $PING_RESPONSE_KEY data")
                    ""
                }
                messages.clear()
                try {
                    val jsonObject = JSONObject(listString)
                    for (key in jsonObject.keys()) {
                        val campaign = Gson().fromJson(
                            jsonObject.getJSONObject(key).toString(), CampaignData::class.java
                        )
                        // manual setting since not part of constructor
                        campaign.timesClosed = jsonObject.getJSONObject(key).getInt("timesClosed")
                        messages[key] = campaign
                    }
                } catch (ex: Exception) {
                    Logger(TAG).debug(ex.cause, "Invalid JSON format for $PING_RESPONSE_KEY data")
                }
            }
        }

        private fun saveDataToCache() {
            // check if caching is enabled to update persistent data
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                // save updated message list
                InAppMessaging.instance().getHostAppContext()?.let {
                    PreferencesUtil.putString(
                        context = it,
                        name = InAppMessaging.getPreferencesFile(),
                        key = PING_RESPONSE_KEY,
                        value = Gson().toJson(messages)
                    )
                } ?: Logger(TAG).debug("failed saving response data")
            }
        }
    }
}
