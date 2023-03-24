package com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers

import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*
import java.io.File

internal object TestDataHelper {

    val messageMixerResponseJson: String = ""
        get() {
            if (field.isEmpty())
                return File("src/test/resources/test_response.json").readText()
            return field
        }

    val messageMixerResponse: MessageMixerResponse = Gson().fromJson(messageMixerResponseJson, MessageMixerResponse::class.java)
    private val message0 = messageMixerResponse.data[0].campaignData
    val message0Payload = message0.messagePayload.copy()

    fun createDummyMessage(
        campaignId: String? = null,
        maxImpressions: Int? = null,
        areImpressionsInfinite: Boolean? = null,
        hasNoEndDate: Boolean? = null,
        isCampaignDismissable: Boolean? = null,
        type: Int? = null,
        isTest: Boolean? = null,
        triggers: List<Trigger>? = null,
        messagePayload: MessagePayload? = null,
    ): Message {
        return message0.copy(
            campaignId = campaignId ?: message0.campaignId,
            maxImpressions = maxImpressions ?: message0.maxImpressions,
            areImpressionsInfinite = areImpressionsInfinite ?: message0.areImpressionsInfinite,
            hasNoEndDate = hasNoEndDate ?: message0.hasNoEndDate,
            isCampaignDismissable = isCampaignDismissable ?: message0.isCampaignDismissable,
            type = type ?: message0.type,
            isTest = isTest ?: message0.isTest,
            triggers = triggers ?: message0.triggers,
            messagePayload = messagePayload ?: message0.messagePayload,
        )
    }

    fun createDummyPayload(
        headerColor: String? = null,
        backgroundColor: String? = null,
        messageSettings: MessageSettings? = null,
        messageBody: String? = null,
        resource: Resource? = null,
        titleColor: String? = null,
        header: String? = null,
        frameColor: String? = null,
        title: String? = null,
        messageBodyColor: String? = null,
    ): MessagePayload {
        return message0Payload.copy(
            headerColor = headerColor ?: message0Payload.headerColor,
            backgroundColor = backgroundColor ?: message0Payload.backgroundColor,
            messageSettings = messageSettings ?: message0Payload.messageSettings,
            messageBody = messageBody ?: message0Payload.messageBody,
            resource = resource ?: message0Payload.resource,
            titleColor = titleColor ?: message0Payload.titleColor,
            header = header ?: message0Payload.header,
            frameColor = frameColor ?: message0Payload.frameColor,
            title = title ?: message0Payload.title,
            messageBodyColor = messageBodyColor ?: message0Payload.messageBodyColor,
        )
    }
}