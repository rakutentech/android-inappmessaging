package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger

internal class ValidTestMessage(
    private val campaignId: String = DEFAULT_CAMPAIGN_ID,
    private val isTest: Boolean = IS_TEST
) : Message {
    internal var timesClosed = 0
    private var max = 1

    override fun getType(): Int = InAppMessageType.MODAL.typeId

    override fun getCampaignId() = campaignId

    override fun getTriggers(): List<Trigger>? {
        val triggerList = ArrayList<Trigger>()
        triggerList.add(Trigger())
        return triggerList
    }

    override fun getMessagePayload(): MessagePayload? = MessagePayload()

    override fun isTest(): Boolean = isTest

    override fun getMaxImpressions() = max

    override fun setMaxImpression(maxImpression: Int) {
        this.max = maxImpression
    }

    override fun getContexts(): List<String> = listOf()

    override fun getNumberOfTimesClosed() = timesClosed

    override fun incrementTimesClosed() {
        timesClosed++
    }

    @Suppress("ComplexCondition")
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
        private const val DEFAULT_CAMPAIGN_ID = "5bf41c52-e4c0-4cb2-9183-df429e84d681"
        private const val IS_TEST = true
    }
}
