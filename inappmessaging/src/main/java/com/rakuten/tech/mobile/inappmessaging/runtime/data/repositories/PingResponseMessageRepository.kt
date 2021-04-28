package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.CampaignData
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.json.JSONObject

/**
 * Contains all downloaded messages from Ping Message Mixer. Also exposed internal api which can get
 * these messages.
 */
internal abstract class PingResponseMessageRepository : MessageRepository {
    // Server returned UTC time in last `/Ping` request response.
    var lastPingMillis: Long = 0

    companion object {
        private var instance: PingResponseMessageRepository = PingResponseMessageRepositoryImpl()
        private const val PING_RESPONSE_KEY = "ping_response_list"

        internal var isInitialLaunch = false

        fun instance(): PingResponseMessageRepository = instance
    }

    private class PingResponseMessageRepositoryImpl : PingResponseMessageRepository() {
        // LinkedHashMap can preserve the message insertion order.
        // Map - Key: Campaign ID, Value: Message object
        private var messages: MutableMap<String, Message> = LinkedHashMap()
        private var appLaunchList: MutableMap<String, Boolean> = LinkedHashMap()
        private var user = ""

        init {
            checkAndResetMap(true)
        }

        /**
         * Replacing all new messages to the [messageList].
         * If messageList empty, IllegalArgumentException will be thrown.
         */
        @Throws(IllegalArgumentException::class)
        override fun replaceAllMessages(messageList: List<Message>?) {
            requireNotNull(messageList) { InAppMessagingConstants.ARGUMENT_IS_NULL_EXCEPTION }
            checkAndResetMap()

            messages = LinkedHashMap()
            appLaunchList = LinkedHashMap()
            for (message in messageList) {
                messages[message.getCampaignId()!!] = message
                if (message.getTriggers()?.size == 1) {
                    appLaunchList[message.getCampaignId()!!] = isInitialLaunch
                }
            }
            isInitialLaunch = false
            saveUpdatedMap()
        }

        /**
         * This method returns a copy of all messages are in the current repository.
         */
        override fun getAllMessagesCopy(): List<Message> {
            checkAndResetMap()
            return ArrayList(messages.values)
        }

        override fun clearMessages() {
            messages.clear()
            saveUpdatedMap()
        }

        override fun incrementTimesClosed(messageList: List<Message>) {
            checkAndResetMap()
            messages.filter { m -> messageList.any { it.getCampaignId() == m.key } }
                    .forEach { it.value.incrementTimesClosed() }
            saveUpdatedMap()
        }

        override fun shouldDisplayAppLaunchCampaign(id: String): Boolean {
            val result = appLaunchList[id]
            appLaunchList[id] = false // it should not be displayed again in current session
            return result ?: false
        }

        private fun checkAndResetMap(onLaunch: Boolean = false) {
            // check if caching is enabled and if there are changes in user info
            if (InAppMessaging.instance().isLocalCachingEnabled() &&
                    (onLaunch || user != AccountRepository.instance().userInfoHash)) {
                user = AccountRepository.instance().userInfoHash
                // reset message list from cached using updated user info
                val listString = InAppMessaging.instance().getEncryptedSharedPref()?.getString(PING_RESPONSE_KEY, "")
                        ?: ""
                messages.clear()
                if (listString.isNotEmpty()) {
                    val jsonObject = JSONObject(listString)
                    for (key in jsonObject.keys()) {
                        val campaign = Gson().fromJson(
                                jsonObject.getJSONObject(key).toString(), CampaignData::class.java)
                        // manual setting since not part of constructor
                        campaign.timesClosed = jsonObject.getJSONObject(key).getInt("timesClosed")
                        messages[key] = campaign
                    }
                }
            }
        }

        private fun saveUpdatedMap() {
            // check if caching is enabled to update persistent data
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                // save updated message list
                InAppMessaging.instance().getEncryptedSharedPref()?.edit()?.putString(PING_RESPONSE_KEY,
                        Gson().toJson(messages))?.apply()
            }
        }
    }
}
