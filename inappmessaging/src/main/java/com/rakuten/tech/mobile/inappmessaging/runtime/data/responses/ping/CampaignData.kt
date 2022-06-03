package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message

/**
 * Class for parsing CampaignData, which is a response from MessageMixer.
 */
@SuppressWarnings("TooManyFunctions")
internal data class CampaignData(
    @SerializedName("messagePayload")
    private val messagePayload: MessagePayload,
    @SerializedName("type")
    private val type: Int,
    @SerializedName("triggers")
    private val triggers: List<Trigger>?,
    @SerializedName("campaignId")
    private val campaignId: String,
    @SerializedName("isTest")
    private val isTest: Boolean,
    @SerializedName("maxImpressions")
    private var maxImpressions: Int = 0,
    @SerializedName("hasNoEndDate")
    private val hasNoEndDate: Boolean = false,
    @SerializedName("isCampaignDismissable")
    private val isCampaignDismissable: Boolean = true,
    @SerializedName("infiniteImpressions")
    private val infiniteImpressions: Boolean = false
) : Message {

    @SerializedName("impressionsLeft")
    override var impressionsLeft: Int? = null
        get() = if (field == null) maxImpressions else field

    @SerializedName("isOptedOut")
    override var isOptedOut: Boolean? = null
        get() = if (field == null) false else field

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

    override fun infiniteImpressions() = infiniteImpressions

    override fun hasNoEndDate() = hasNoEndDate

    override fun isCampaignDismissable() = isCampaignDismissable
}
