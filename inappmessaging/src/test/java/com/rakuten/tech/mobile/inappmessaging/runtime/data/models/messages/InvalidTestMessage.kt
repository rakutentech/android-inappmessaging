package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages

import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger

internal class InvalidTestMessage : Message {
    override fun getType(): Int = -1

    override fun getCampaignId(): String? = null

    override fun getTriggers(): List<Trigger>? = null

    override fun getMessagePayload(): MessagePayload? = null

    override fun isTest(): Boolean = false

    override fun getMaxImpressions(): Int = 0
}
