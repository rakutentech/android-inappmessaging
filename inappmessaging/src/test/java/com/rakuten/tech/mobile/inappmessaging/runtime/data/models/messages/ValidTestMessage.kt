package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger

internal class ValidTestMessage(var id: String = DEFAULT_CAMPAIGN_ID, private val isTest: Boolean = IS_TEST) : Message {

    override fun getType(): Int = InAppMessageType.MODAL.typeId

    override fun getCampaignId(): String? = id

    override fun getTriggers(): List<Trigger>? {
        val triggerList = ArrayList<Trigger>()
        triggerList.add(Trigger())
        return triggerList
    }

    override fun getMessagePayload(): MessagePayload? = MessagePayload()

    override fun isTest(): Boolean = isTest

    override fun getMaxImpressions(): Int = 1

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
        var result = id.hashCode()
        result = 31 * result + isTest.hashCode()
        return result
    }

    companion object {
        private const val DEFAULT_CAMPAIGN_ID = "5bf41c52-e4c0-4cb2-9183-df429e84d681"
        private const val IS_TEST = true
    }
}
