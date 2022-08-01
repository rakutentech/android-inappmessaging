package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*

internal class ValidTestMessage(
    private val campaignId: String = DEFAULT_CAMPAIGN_ID,
    private val isTest: Boolean = IS_TEST,
    private val isCampaignDismissable: Boolean = true,
    private val maxImpressions: Int = 1,
    private val infiniteImpressions: Boolean = false,
    private val triggers: List<Trigger>? = null
) : Message {
    private var max = maxImpressions

    override var impressionsLeft: Int? = null
        get() = if (field == null) max else field

    override var isOptedOut: Boolean? = null
        get() = if (field == null) false else field

    override fun getType(): Int = InAppMessageType.MODAL.typeId

    override fun getCampaignId() = campaignId

    override fun getTriggers(): List<Trigger> {
        if (triggers == null) {
            val triggerList = ArrayList<Trigger>()
            triggerList.add(Trigger(1, 1, "test", mutableListOf()))
            return triggerList
        }
        return triggers
    }

    override fun getMessagePayload(): MessagePayload = MessagePayload(
        DEFAULT_COLOR, "#ffffff",
        MessageSettings(
            DisplaySettings(1, 1, Long.MAX_VALUE, 1, false, 1, false),
            ControlSettings(listOf())
        ),
        null, Resource(cropType = 2), DEFAULT_COLOR, null, "#ffffff", "title",
        DEFAULT_COLOR
    )

    override fun isTest(): Boolean = isTest

    override fun getMaxImpressions() = max

    override fun setMaxImpression(maxImpression: Int) {
        this.max = maxImpression
    }

    override fun getContexts(): List<String> = listOf()

    override fun infiniteImpressions() = infiniteImpressions

    override fun hasNoEndDate() = false

    override fun isCampaignDismissable() = isCampaignDismissable

    @SuppressWarnings("ComplexCondition")
    override fun equals(other: Any?): Boolean {
        val otherObject = other as Message
        if (getType() != otherObject.getType() ||
            getCampaignId() != otherObject.getCampaignId() ||
            isTest() != otherObject.isTest() ||
            getMaxImpressions() != otherObject.getMaxImpressions()
        ) return false
        return true
    }

    override fun hashCode(): Int {
        var result = campaignId.hashCode()
        result = 31 * result + isTest.hashCode()
        return result
    }

    companion object {
        private const val DEFAULT_CAMPAIGN_ID = "5bf41c52-e4c0-4cb2-9183-df429e84d681"
        private const val IS_TEST = true
        private const val DEFAULT_COLOR = "#000000"
    }
}
