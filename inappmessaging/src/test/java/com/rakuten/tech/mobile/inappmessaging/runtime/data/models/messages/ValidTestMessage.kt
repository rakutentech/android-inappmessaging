package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*

internal class ValidTestMessage(
    private val campaignId: String = DEFAULT_CAMPAIGN_ID,
    private val isTest: Boolean = IS_TEST,
    private val type: Int = InAppMessageType.MODAL.typeId,
    private val tooltip: Tooltip? = null
) : Message {
    internal var timesClosed = 0
    private var max = 1

    override fun getType(): Int = type

    override fun getCampaignId() = campaignId

    override fun getTriggers(): List<Trigger> {
        val triggerList = ArrayList<Trigger>()
        triggerList.add(Trigger(1, 1, "test", mutableListOf()))
        return triggerList
    }

    override fun getMessagePayload(): MessagePayload = MessagePayload(DEFAULT_COLOR, "#ffffff",
        MessageSettings(
            DisplaySettings(1, 1, 1, 1, false, 1, false),
            ControlSettings(listOf())
        ), null, Resource(cropType = 2), DEFAULT_COLOR, null, "#ffffff", "title",
        DEFAULT_COLOR)

    override fun isTest(): Boolean = isTest

    override fun getMaxImpressions() = max

    override fun setMaxImpression(maxImpression: Int) {
        this.max = maxImpression
    }

    override fun getContexts(): List<String> = listOf()

    override fun getTooltipConfig(): Tooltip? = tooltip

    override fun getNumberOfTimesClosed() = timesClosed

    override fun incrementTimesClosed() {
        timesClosed++
    }

    @SuppressWarnings("ComplexCondition")
    override fun equals(other: Any?): Boolean {
        val otherObject = other as Message
        if (getType() != otherObject.getType() ||
                getCampaignId() != otherObject.getCampaignId() ||
                isTest() != otherObject.isTest() ||
                getMaxImpressions() != otherObject.getMaxImpressions()) return false
        return true
    }

    override fun hashCode(): Int {
        var result = campaignId.hashCode()
        result = 31 * result + isTest.hashCode()
        return result
    }

    companion object {
        internal const val DEFAULT_CAMPAIGN_ID = "5bf41c52-e4c0-4cb2-9183-df429e84d681"
        private const val IS_TEST = true
        private const val DEFAULT_COLOR = "#000000"
    }
}
