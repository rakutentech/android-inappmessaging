package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages

import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*

internal class InvalidTestMessage : Message {
    override var impressionsLeft: Int? = 0

    override var isOptedOut: Boolean? = false

    override fun getType(): Int = -1

    override fun getCampaignId() = ""

    override fun getTriggers(): List<Trigger> = listOf()

    override fun getMessagePayload() = MessagePayload(
        "invalid", "invalid",
        MessageSettings(
            DisplaySettings(1, 1, 1, 1, false, 1, false),
            ControlSettings(listOf())
        ),
        null, Resource(cropType = 2), "#000000", null, "#ffffff", "title",
        "#000000"
    )

    override fun isTest(): Boolean = false

    override fun getMaxImpressions(): Int = 0

    override fun setMaxImpression(maxImpression: Int) {}

    override fun getContexts(): List<String> = listOf()

    override fun infiniteImpressions() = false

    override fun hasNoEndDate() = false

    override fun isCampaignDismissable() = true
}
