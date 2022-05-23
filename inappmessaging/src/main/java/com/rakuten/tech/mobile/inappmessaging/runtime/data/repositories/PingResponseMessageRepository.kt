package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.CampaignData
import com.rakuten.tech.mobile.inappmessaging.runtime.fromJson
import com.rakuten.tech.mobile.inappmessaging.runtime.mapAdapter
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import com.squareup.moshi.Moshi
import org.json.JSONObject
import java.lang.ClassCastException

/**
 * Contains all downloaded messages from Ping Message Mixer. Also exposed internal api which can get
 * these messages.
 */
internal abstract class PingResponseMessageRepository : MessageRepository {
    // Server returned UTC time in last `/Ping` request response.
    var lastPingMillis: Long = 0

    companion object {
        private var instance: PingResponseMessageRepository = PingResponseMessageRepositoryImpl()

        @VisibleForTesting
        internal const val PING_RESPONSE_KEY = "ping_response_list"
        private const val TAG = "IAM_PingResponseRepo"

        internal var isInitialLaunch = false

        fun instance(): PingResponseMessageRepository = instance
    }

    private class PingResponseMessageRepositoryImpl : PingResponseMessageRepository() {
        // LinkedHashMap can preserve the message insertion order.
        // Map - Key: Campaign ID, Value: Message object
        private val messages = LinkedHashMap<String, Message>()
        private val appLaunchList = LinkedHashMap<String, Boolean>()
        private var user = ""

        init {
            checkAndResetMap(true)
        }

        /**
         * Replacing all new messages to the [messageList].
         * If messageList empty, IllegalArgumentException will be thrown.
         */
        override fun replaceAllMessages(messageList: List<Message>) {
            checkAndResetMap()

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

        @SuppressWarnings("LongMethod", "TooGenericExceptionCaught")
        private fun checkAndResetMap(onLaunch: Boolean = false) {
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
                        val campaign = Moshi.Builder().build().fromJson<CampaignData>(
                            data = jsonObject.getJSONObject(key).toString()
                        )
                        // manual setting since not part of constructor
                        campaign?.let {
                            it.timesClosed = jsonObject.getJSONObject(key).getInt("timesClosed")
                            messages[key] = it
                        }
                    }
                } catch (ex: Exception) {
                    Logger(TAG).debug(ex.cause, "Invalid JSON format for $PING_RESPONSE_KEY data")
                }
            }
        }

        private fun saveUpdatedMap() {
            // check if caching is enabled to update persistent data
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                val adapter = Moshi.Builder().build().mapAdapter<String, Message>()
                // save updated message list
                InAppMessaging.instance().getHostAppContext()?.let {
                    PreferencesUtil.putString(
                        context = it,
                        name = InAppMessaging.getPreferencesFile(),
                        key = PING_RESPONSE_KEY,
                        value = adapter.toJson(messages)
                    )
                } ?: Logger(TAG).debug("failed saving response data")
            }
        }
    }
}
