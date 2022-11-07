package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.CampaignData
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import org.json.JSONObject
import java.lang.ClassCastException
import java.lang.Integer.max

internal abstract class CampaignRepository {
    val messages: LinkedHashMap<String, Message> = linkedMapOf()
    var lastSyncMillis: Long? = null

    /**
     * Syncs [messageList] with server.
     */
    abstract fun syncWith(messageList: List<Message>, timestampMillis: Long)

    /**
     * Updates the [Message.isOptedOut] as true for the provided campaign.
     */
    abstract fun optOutCampaign(campaign: Message): Message?

    /**
     * Decrements the number of [Message.impressionsLeft] for provided campaign Id in the repository.
     */
    abstract fun decrementImpressions(id: String): Message?

    /**
     * Increments the number of [Message.impressionsLeft] for provided campaign Id in the repository.
     */
    abstract fun incrementImpressions(id: String): Message?

    /**
     * Clears messages for last user.
     */
    abstract fun clearMessages()

    companion object {
        private var instance: CampaignRepository = CampaignRepositoryImpl()

        internal const val IAM_USER_CACHE = "IAM_user_cache"
        private const val TAG = "IAM_CampaignRepo"

        fun instance(): CampaignRepository = instance
    }

    private class CampaignRepositoryImpl : CampaignRepository() {
        init {
            loadCachedData()
        }

        override fun syncWith(messageList: List<Message>, timestampMillis: Long) {
            lastSyncMillis = timestampMillis
            loadCachedData() // ensure we're using latest cache data for syncing below
            val oldList = LinkedHashMap(messages) // copy

            messages.clear()
            for (newCampaign in messageList) {
                if (newCampaign.getCampaignId().isEmpty()) {
                    continue
                }

                val updatedCampaign = updateCampaign(newCampaign, oldList)
                messages[updatedCampaign.getCampaignId()] = updatedCampaign
            }
            saveDataToCache()
        }

        private fun updateCampaign(newCampaign: Message, oldList: LinkedHashMap<String, Message>): Message {
            var updatedCampaign = newCampaign
            val oldCampaign = oldList[newCampaign.getCampaignId()]
            if (oldCampaign != null) {
                updatedCampaign = Message.updatedMessage(
                    updatedCampaign,
                    asOptedOut = oldCampaign.isOptedOut == true
                )

                var newImpressionsLeft = oldCampaign.impressionsLeft ?: oldCampaign.getMaxImpressions()
                val isMaxImpressionsEdited = oldCampaign.getMaxImpressions() != newCampaign.getMaxImpressions()
                if (isMaxImpressionsEdited) {
                    newImpressionsLeft += newCampaign.getMaxImpressions() - oldCampaign.getMaxImpressions()
                }
                newImpressionsLeft = max(0, newImpressionsLeft)
                updatedCampaign = Message.updatedMessage(updatedCampaign, impressionsLeft = newImpressionsLeft)
            }
            return updatedCampaign
        }

        override fun clearMessages() {
            messages.clear()
            saveDataToCache()
        }

        override fun optOutCampaign(campaign: Message): Message? {
            val localCampaign = messages[campaign.getCampaignId()]
            if (localCampaign == null) {
                InAppLogger(TAG).debug(
                    "Campaign (${campaign.getCampaignId()}) could not be updated -" +
                        "not found in the repository"
                )
                return null
            }

            val updatedCampaign = Message.updatedMessage(localCampaign, asOptedOut = true)
            messages[campaign.getCampaignId()] = updatedCampaign

            if (!campaign.isTest()) {
                saveDataToCache()
            }
            return updatedCampaign
        }

        override fun decrementImpressions(id: String): Message? {
            val campaign = messages[id] ?: return null
            return updateImpressions(
                campaign,
                max(0, (campaign.impressionsLeft ?: campaign.getMaxImpressions()) - 1)
            )
        }

        // For testing purposes
        override fun incrementImpressions(id: String): Message? {
            val campaign = messages[id] ?: return null
            return updateImpressions(
                campaign,
                (campaign.impressionsLeft ?: campaign.getMaxImpressions()) + 1
            )
        }

        @SuppressWarnings("TooGenericExceptionCaught")
        private fun loadCachedData() {
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                messages.clear()
                try {
                    val jsonObject = JSONObject(retrieveData())
                    for (key in jsonObject.keys()) {
                        messages[key] = Gson().fromJson(
                            jsonObject.getJSONObject(key).toString(), CampaignData::class.java
                        )
                    }
                } catch (ex: Exception) {
                    InAppLogger(TAG).debug(ex.cause, "Invalid JSON format for $IAM_USER_CACHE data")
                }
            }
        }

        private fun retrieveData(): String {
            return InAppMessaging.instance().getHostAppContext()?.let { ctx ->
                PreferencesUtil.getString(
                    context = ctx,
                    name = InAppMessaging.getPreferencesFile(),
                    key = IAM_USER_CACHE,
                    defValue = ""
                )
            }.orEmpty()
        }

        private fun saveDataToCache() {
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                InAppMessaging.instance().getHostAppContext()?.let {
                    PreferencesUtil.putString(
                        context = it,
                        name = InAppMessaging.getPreferencesFile(),
                        key = IAM_USER_CACHE,
                        value = Gson().toJson(messages)
                    )
                } ?: InAppLogger(TAG).debug("failed saving response data")
            }
        }

        private fun updateImpressions(campaign: Message, newValue: Int): Message {
            val updatedCampaign = Message.updatedMessage(campaign, impressionsLeft = newValue)
            messages[campaign.getCampaignId()] = updatedCampaign

            saveDataToCache()
            return updatedCampaign
        }
    }
}
