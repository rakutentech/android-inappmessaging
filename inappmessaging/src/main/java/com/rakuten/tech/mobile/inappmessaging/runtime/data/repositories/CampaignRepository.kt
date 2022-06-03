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
import java.lang.Integer.max

internal interface CampaignRepositoryType {
    val messages: List<Message>

    val lastSyncMillis: Long?

    /**
     * Syncs [messages] with server.
     */
    fun syncWith(messageList: List<Message>, timestampMillis: Long)

    /**
     * Updates the [Message.isOptedOut] as true for the provided campaign.
     */
    fun optOutCampaign(campaign: Message): Message?

    /**
     * Decrements the number of [Message.impressionsLeft] for provided campaign Id in the repository.
     */
    fun decrementImpressions(id: String): Message?

    /**
     * Increments the number of [Message.impressionsLeft] for provided campaign Id in the repository.
     */
    fun incrementImpressions(id: String): Message?

    /**
     * Clears [messages] for last user.
     */
    fun clearMessages()
}

@SuppressWarnings("UnnecessaryAbstractClass")
internal abstract class CampaignRepository : CampaignRepositoryType {
    companion object {
        private var instance: CampaignRepository = CampaignRepositoryImpl()

        @VisibleForTesting
        internal const val IAM_USER_CACHE = "IAM_user_cache"
        private const val TAG = "IAM_CampaignRepo"

        fun instance(): CampaignRepository = instance
    }

    private class CampaignRepositoryImpl : CampaignRepository() {

        // LinkedHashMap can preserve the message insertion order.
        // Map - Key: Campaign ID, Value: Message object
        private val messagesHashMap = LinkedHashMap<String, Message>()
        private var user = ""

        init {
            loadCachedData(true)
        }

        override val messages: List<Message>
            get() {
                loadCachedData()
                return ArrayList(messagesHashMap.values)
            }

        override var lastSyncMillis: Long? = null
            private set

        @SuppressWarnings("LongMethod", "NestedBlockDepth")
        override fun syncWith(messageList: List<Message>, timestampMillis: Long) {
            lastSyncMillis = timestampMillis
            loadCachedData()
            val oldList = messages

            messagesHashMap.clear()
            val retainImpressionsLeftValue = true
            for (newCampaign in messageList) {
                if (newCampaign.getCampaignId().isEmpty()) {
                    continue
                }

                var updatedCampaign = newCampaign
                val oldCampaign = oldList.firstOrNull { it.getCampaignId() == newCampaign.getCampaignId() }
                if (oldCampaign != null) {
                    updatedCampaign = Message.updatedMessage(updatedCampaign, asOptedOut = oldCampaign.isOptedOut!!)

                    if (retainImpressionsLeftValue) {
                        var newImpressionsLeft = oldCampaign.impressionsLeft!!
                        val wasMaxImpressionsEdited = oldCampaign.getMaxImpressions() != newCampaign.getMaxImpressions()
                        if (wasMaxImpressionsEdited) {
                            newImpressionsLeft += newCampaign.getMaxImpressions() - oldCampaign.getMaxImpressions()
                        }
                        updatedCampaign = Message.updatedMessage(updatedCampaign, impressionsLeft = newImpressionsLeft)
                    }
                }
                messagesHashMap[updatedCampaign.getCampaignId()] = updatedCampaign
            }
            saveDataToCache()
        }

        override fun clearMessages() {
            messagesHashMap.clear()
            saveDataToCache()
        }

        override fun optOutCampaign(campaign: Message): Message? {
            loadCachedData()

            val localCampaign = findCampaign(campaign.getCampaignId())
            if (localCampaign == null) {
                Logger(TAG).debug(
                    "Campaign (${campaign.getCampaignId()}) could not be updated -" +
                        "not found in the repository"
                )
                return null
            }

            val updatedCampaign = Message.updatedMessage(localCampaign, asOptedOut = true)
            messagesHashMap[campaign.getCampaignId()] = updatedCampaign

            if (!campaign.isTest()) {
                saveDataToCache()
            }
            return updatedCampaign
        }

        override fun decrementImpressions(id: String): Message? {
            val campaign = findCampaign(id) ?: return null
            return updateImpressions(campaign, max(0, campaign.impressionsLeft!! - 1))
        }

        override fun incrementImpressions(id: String): Message? {
            val campaign = findCampaign(id) ?: return null
            return updateImpressions(campaign, campaign.impressionsLeft!! + 1)
        }

        @SuppressWarnings("LongMethod", "TooGenericExceptionCaught")
        fun loadCachedData(onLaunch: Boolean = false) {
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
                            key = IAM_USER_CACHE,
                            defValue = ""
                        )
                    }.orEmpty()
                } catch (ex: ClassCastException) {
                    Logger(TAG).debug(ex.cause, "Incorrect type for $IAM_USER_CACHE data")
                    ""
                }

                messagesHashMap.clear()
                try {
                    val jsonObject = JSONObject(listString)
                    for (key in jsonObject.keys()) {
                        val campaign = Gson().fromJson(
                            jsonObject.getJSONObject(key).toString(),
                            CampaignData::class.java
                        )
                        messagesHashMap[key] = campaign
                    }
                } catch (ex: Exception) {
                    Logger(TAG).debug(ex.cause, "Invalid JSON format for $IAM_USER_CACHE data")
                }
            }
        }

        private fun saveDataToCache() {
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                InAppMessaging.instance().getHostAppContext()?.let {
                    PreferencesUtil.putString(
                        context = it,
                        name = InAppMessaging.getPreferencesFile(),
                        key = IAM_USER_CACHE,
                        value = Gson().toJson(messagesHashMap)
                    )
                } ?: Logger(TAG).debug("failed saving response data")
            }
        }

        private fun findCampaign(id: String): Message? {
            loadCachedData()
            return messagesHashMap[id]
        }

        private fun updateImpressions(campaign: Message, newValue: Int): Message? {
            val localCampaign = findCampaign(campaign.getCampaignId())
            if (localCampaign == null) {
                Logger(TAG).debug(
                    "Campaign (${campaign.getCampaignId()}) could not be updated -" +
                        "not found in the repository"
                )
                return null
            }

            val updatedCampaign = Message.updatedMessage(campaign, impressionsLeft = newValue)
            messagesHashMap[campaign.getCampaignId()] = updatedCampaign

            saveDataToCache()
            return updatedCampaign
        }
    }
}