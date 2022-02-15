package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.sdkutils.logger.Logger

/**
 * Class for parsing CampaignData, which is a response from MessageMixer.
 */
@SuppressWarnings("TooManyFunctions")
internal data class CampaignData(
    @SerializedName("messagePayload")
    private val messagePayload: MessagePayload,
    @SerializedName("type")
    private var type: Int,
    @SerializedName("triggers")
    private val triggers: List<Trigger>?,
    @SerializedName("campaignId")
    private val campaignId: String,
    @SerializedName("isTest")
    private val isTest: Boolean,
    @SerializedName("maxImpressions")
    private var maxImpressions: Int = 0
) : Message {

    @SerializedName("timesClosed")
    internal var timesClosed = 0
    private var tooltip: Tooltip? = null

    override fun getType(): Int = type

    override fun getCampaignId(): String = campaignId

    override fun getTriggers(): List<Trigger>? = triggers

    override fun getMessagePayload(): MessagePayload = messagePayload

    override fun isTest(): Boolean = isTest

    override fun getMaxImpressions(): Int = maxImpressions

    override fun setMaxImpression(maxImpression: Int) {
        this.maxImpressions = maxImpression
    }

    override fun getContexts(): List<String> {
        val regex = Regex("\\[(.*?)\\]")
        val matches = regex.findAll(messagePayload.title)
        return matches.map { it.groupValues[1] }.toList()
    }

    override fun getTooltipConfig(): Tooltip? {
        val result = messagePayload.title.contains(TOOLTIP_TAG, true)
        if (result && tooltip == null) {
            // change type to tool tip (this will be fixed once the backend supports tooltip)
            try {
                tooltip = Gson().fromJson(messagePayload.messageBody, Tooltip::class.java)
                type = InAppMessageType.TOOLTIP.typeId
            } catch (je: JsonParseException) {
                Logger(TAG).debug("Invalid format for tooltip config.", je)
            }
        }
        return tooltip
    }

    override fun getNumberOfTimesClosed() = synchronized(timesClosed) {
        timesClosed
    }

    override fun incrementTimesClosed() {
        synchronized(timesClosed) {
            timesClosed++
        }
    }

    companion object {
        private const val TOOLTIP_TAG = "[ToolTip]"
        private const val TAG = "IAM_Campaign"
    }
}
