package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.RmcHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson.CustomJson
import com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson.CustomJsonDeserializer
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import java.util.Date

/**
 * Class for parsing Message, which is a response from MessageMixer.
 */
@SuppressWarnings(
    "DataClassContainsFunctions",
    "kotlin:S1125",
)
internal data class Message(
    val campaignId: String,
    val maxImpressions: Int,
    @SerializedName("infiniteImpressions") val areImpressionsInfinite: Boolean,
    val hasNoEndDate: Boolean,
    val isCampaignDismissable: Boolean,
    var type: Int,
    val isTest: Boolean,
    val triggers: List<Trigger>?,
    val messagePayload: MessagePayload,
    val customJson: JsonObject? = null, // dynamic
) {
    private var tooltip: Tooltip? = null

    @Transient private var customJsonData: CustomJson? = null

    var impressionsLeft: Int? = null
        get() = if (field == null) maxImpressions else field

    var isOptedOut: Boolean? = null
        get() = if (field == null) false else field

    val contexts: List<String>
        get() {
            val regex = Regex("\\[(.*?)\\]")
            val matches = regex.findAll(messagePayload.title)
            return matches.map { it.groupValues[1] }.toList()
        }

    val isOutdated: Boolean
        get() {
            return if (hasNoEndDate) {
                false
            } else {
                messagePayload.messageSettings.displaySettings.endTimeMillis < Date().time
            }
        }

    val isPushPrimer: Boolean
        get() = getCustomJsonData()?.pushPrimer?.button != null

    fun getTooltipConfig(): Tooltip? {
        val result = messagePayload.title.startsWith(TOOLTIP_TAG, true)
        if (result && tooltip == null) {
            // change type to tool tip (this will be fixed once the backend supports tooltip)
            try {
                tooltip = Gson().fromJson(messagePayload.messageBody, Tooltip::class.java)
                if (tooltip?.isValid() == true) {
                    type = InAppMessageType.TOOLTIP.typeId
                } else {
                    // missing required fields
                    tooltip = null
                }
            } catch (je: JsonParseException) {
                InAppLogger(TAG).warn("getTooltipConfig - invalid tooltip format")
                InAppLogger(TAG).debug("parse exception: $je")
            }
        }
        return tooltip
    }

    @SuppressWarnings("TooGenericExceptionCaught")
    fun getCustomJsonData(): CustomJson? {
        if (!RmcHelper.isRmcIntegrated() || customJson == null || customJson.entrySet().isEmpty()) {
            return null
        }
        if (customJsonData == null) {
            try {
                val gson = GsonBuilder()
                    .registerTypeAdapter(CustomJson::class.java, CustomJsonDeserializer())
                    .create()
                customJsonData = gson.fromJson(customJson, CustomJson::class.java)
            } catch (_: Exception) {
                InAppLogger(TAG).debug("getCustomJsonData - invalid customJson format")
            }
        }
        return customJsonData
    }

    companion object {
        internal const val TOOLTIP_TAG = "[ToolTip]"
        private const val TAG = "IAM_Message"
    }
}
