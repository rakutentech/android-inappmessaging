package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.ui.UiMessage

/**
 * Maps [Message] DTO to [UiMessage] model.
 */
internal object MessageMapper : Mapper<Message, UiMessage> {

    @SuppressWarnings("LongMethod")
    override fun mapFrom(from: Message): UiMessage {
        val uiModel = UiMessage(
            id = from.campaignId,
            type = from.type,
            isTest = from.isTest,
            backgroundColor = from.messagePayload.backgroundColor,
            headerText = from.messagePayload.header,
            headerColor = from.messagePayload.headerColor,
            bodyText = from.messagePayload.messageBody,
            bodyColor = from.messagePayload.messageBodyColor,
            imageUrl = from.messagePayload.resource.imageUrl,
            shouldShowUpperCloseButton = from.isCampaignDismissable,
            buttons = from.messagePayload.messageSettings.controlSettings.buttons,
            displaySettings = from.messagePayload.messageSettings.displaySettings,
            content = from.messagePayload.messageSettings.controlSettings.content,
            tooltipData = from.getTooltipConfig(),
        )

        // Apply CustomJson rules if exists
        return if (from.customJson == null) {
            uiModel
        } else {
            uiModel
                .applyCustomPushPrimer(from.customJson.pushPrimer) // PushPrimer
        }
    }
}
