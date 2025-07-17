package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppError
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppErrorLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import org.json.JSONObject
import java.lang.Integer.max

internal abstract class CampaignRepository {
    val messages: LinkedHashMap<String, Message> = linkedMapOf()
    var lastSyncMillis: Long? = null

    /**
     * Syncs [messageList] with server.
     */
    abstract fun syncWith(messageList: List<Message>, timestampMillis: Long, ignoreTooltips: Boolean = false)

    /**
     * Updates the [Message.isOptedOut] as true for the provided campaign.
     */
    abstract fun optOutCampaign(id: String): Message?

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

    @SuppressWarnings("kotlin:S6515")
    companion object {
        private var instance: CampaignRepository = CampaignRepositoryImpl()

        internal const val IAM_USER_CACHE = "IAM_user_cache"
        private const val TAG = "IAM_CampaignRepo"

        fun instance(): CampaignRepository = instance
    }

    @SuppressWarnings(
        "TooManyFunctions",
    )
    private class CampaignRepositoryImpl : CampaignRepository() {
        init {
            loadCachedData()
        }

        override fun syncWith(messageList: List<Message>, timestampMillis: Long, ignoreTooltips: Boolean) {
            InAppLogger(TAG).info("syncWith start")
            lastSyncMillis = timestampMillis
            loadCachedData() // ensure we're using latest cache data for syncing below
            val oldList = LinkedHashMap(messages) // copy

            messages.clear()
            for (newCampaign in messageList.filterMessages(ignoreTooltips)) {
                val updatedCampaign = updateCampaign(newCampaign, oldList)
                messages[updatedCampaign.campaignId] = updatedCampaign
            }
            saveDataToCache()
            InAppLogger(TAG).info("syncWith end")
        }

        private fun List<Message>.filterMessages(ignoreTooltips: Boolean): List<Message> {
            return this.filterNot {
                it.campaignId.isEmpty() || (it.type == InAppMessageType.TOOLTIP.typeId && ignoreTooltips)
            }
        }

        private fun updateCampaign(newCampaign: Message, oldList: LinkedHashMap<String, Message>): Message {
            val oldCampaign = oldList[newCampaign.campaignId]
            if (oldCampaign != null) {
                newCampaign.isOptedOut = (oldCampaign.isOptedOut == true)

                var newImpressionsLeft = oldCampaign.impressionsLeft ?: oldCampaign.maxImpressions
                val isMaxImpressionsEdited = oldCampaign.maxImpressions != newCampaign.maxImpressions
                if (isMaxImpressionsEdited) {
                    newImpressionsLeft += newCampaign.maxImpressions - oldCampaign.maxImpressions
                }
                newImpressionsLeft = max(0, newImpressionsLeft)
                newCampaign.impressionsLeft = newImpressionsLeft
            }
            return newCampaign
        }

        override fun clearMessages() {
            messages.clear()
            saveDataToCache()
        }

        override fun optOutCampaign(id: String): Message? {
            InAppLogger(TAG).debug("campaign: $id}")
            val localCampaign = messages[id]
            if (localCampaign == null) {
                InAppLogger(TAG).debug("campaign not found in repository")
                return null
            }
            val updatedCampaign = localCampaign.apply { isOptedOut = true }
            if (!updatedCampaign.isTest) {
                saveDataToCache()
            }
            return updatedCampaign
        }

        override fun decrementImpressions(id: String): Message? {
            InAppLogger(TAG).debug("campaign: $id")
            val campaign = messages[id] ?: return null
            return updateImpressions(
                campaign,
                max(0, (campaign.impressionsLeft ?: campaign.maxImpressions) - 1),
            )
        }

        // For testing purposes
        override fun incrementImpressions(id: String): Message? {
            val campaign = messages[id] ?: return null
            return updateImpressions(
                campaign,
                (campaign.impressionsLeft ?: campaign.maxImpressions) + 1,
            )
        }

        @SuppressWarnings(
            "TooGenericExceptionCaught",
            "LongMethod",
        )
        private fun loadCachedData() {
            if (!InAppMessaging.instance().isLocalCachingEnabled()) {
                return
            }

            InAppLogger(TAG).debug("start")
            messages.clear()
            val cachedData = retrieveData()
            if (cachedData.isNotEmpty()) {
                try {
                    val jsonObject = JSONObject(cachedData)
                    for (key in jsonObject.keys()) {
                        messages[key] = Gson().fromJson(
                            jsonObject.getJSONObject(key).toString(), Message::class.java,
                        )
                    }
                } catch (ex: Exception) {
                    InAppErrorLogger.logError(
                        TAG,
                        InAppError(
                            "invalid JSON format for $IAM_USER_CACHE data",
                            ex, ev = Event.UserDataCacheDecodingFailed,
                        ),
                    )
                }
            }
            InAppLogger(TAG).debug("end")
        }

        private fun retrieveData(): String {
            return HostAppInfoRepository.instance().getContext()?.let { ctx ->
                val preferenceFile = InAppMessaging.getPreferencesFile()
                val preferenceData = PreferencesUtil.getString(
                    context = ctx,
                    name = preferenceFile,
                    key = IAM_USER_CACHE,
                    defValue = "",
                )
                InAppLogger(TAG).info("retrieveData - file: $preferenceFile")
                InAppLogger(TAG).debug("retrieveData - data: $preferenceData")
                preferenceData
            }.orEmpty()
        }

        private fun saveDataToCache() {
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                HostAppInfoRepository.instance().getContext()?.let { ctx ->
                    val preferenceFile = InAppMessaging.getPreferencesFile()
                    val preferenceData = Gson().toJson(messages)
                    InAppLogger(TAG).info("saveData - file: $preferenceFile")
                    InAppLogger(TAG).debug("saveData - data: $preferenceData")
                    PreferencesUtil.putString(
                        context = ctx,
                        name = preferenceFile,
                        key = IAM_USER_CACHE,
                        value = preferenceData,
                    )
                } ?: InAppLogger(TAG).debug("failed saving response data")
            }
        }

        private fun updateImpressions(campaign: Message, newValue: Int): Message {
            val updatedCampaign = campaign.apply { impressionsLeft = newValue }
            messages[campaign.campaignId] = updatedCampaign

            saveDataToCache()
            return updatedCampaign
        }
    }
}
