package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import android.os.Build
import com.google.gson.Gson
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@SuppressWarnings("LargeClass")
class MessageMixerResponseSpec(private val testname: String, private val actual: Any?, private val expected: Any?) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
                name = "{0} type test"
        )
        @SuppressWarnings("LongMethod")
        fun data(): List<Array<out Any?>> {
            return listOf(
                    arrayOf("currentPingMills", response.currentPingMillis, 1583890595467),
                    arrayOf("nextPingMillis", response.nextPingMillis, 3600000L),
                    arrayOf("type", campaignData.getType(), 2),
                    arrayOf("campaignId", campaignData.getCampaignId(), "1234567890"),
                    arrayOf("isTest", campaignData.isTest(), false),
                    arrayOf("maxImpressions", campaignData.getMaxImpressions(), 0), // default value
                    arrayOf("headerColor", messagePayload.headerColor, "#ffffff"),
                    arrayOf("backgroundColor", messagePayload.backgroundColor, "#000000"),
                    arrayOf("messageBody", messagePayload.messageBody, "Login Test"),
                    arrayOf("titleColor", messagePayload.titleColor, "#000000"),
                    arrayOf("header", messagePayload.header, "DEV-Test (Android In-App-Test) - Login"),
                    arrayOf("frameColor", messagePayload.frameColor, "#ffffff"),
                    arrayOf("title", messagePayload.title, "DEV-Test (Android In-App-Test)"),
                    arrayOf("messageBodyColor", messagePayload.messageBodyColor, "#ffffff"),
                    arrayOf("orientation", displaySettings.orientation, 1),
                    arrayOf("slideFrom", displaySettings.slideFrom, 1),
                    arrayOf("endTimeMillis", displaySettings.endTimeMillis, 1584109800000),
                    arrayOf("textAlign", displaySettings.textAlign, 2),
                    arrayOf("optOut", displaySettings.optOut, false),
                    arrayOf("content action", controlContentBehavior.action, 1),
                    arrayOf("content uri", controlContentBehavior.uri, "https://en.wikipedia.org/wiki/Test"),
                    arrayOf("content trigger type", controlContentTrigger.type, 1),
                    arrayOf("content trigger eventName", controlContentTrigger.eventName, "event"),
                    arrayOf("content trigger eventType", controlContentTrigger.eventType, 1),
                    arrayOf("content trigger attribute name", controlContentTriggerAttr.name, "attribute"),
                    arrayOf("content trigger attribute value", controlContentTriggerAttr.value, "attrValue"),
                    arrayOf("content trigger attribute type", controlContentTriggerAttr.type, 1),
                    arrayOf("content trigger attribute operator", controlContentTriggerAttr.operator, 1),
                    arrayOf("buttonBackgroundColor", messageButton.buttonBackgroundColor, "#000000"),
                    arrayOf("buttonTextColor", messageButton.buttonTextColor, "#ffffff"),
                    arrayOf("buttonText", messageButton.buttonText, "Test"),
                    arrayOf("button action", buttonOnClickBehavior.action, 1),
                    arrayOf("button uri", buttonOnClickBehavior.uri, "https://en.wikipedia.org/wiki/Test"),
                    arrayOf("button type", buttonTrigger.type, 1),
                    arrayOf("button trigger eventName", buttonTrigger.eventName, "event"),
                    arrayOf("button trigger eventType", buttonTrigger.eventType, 1),
                    arrayOf("button trigger attribute name", buttonTriggerAttr.name, "attribute"),
                    arrayOf("button trigger attribute value", buttonTriggerAttr.value, "attrValue"),
                    arrayOf("button trigger attribute type", buttonTriggerAttr.type, 1),
                    arrayOf("button trigger attribute operator", buttonTriggerAttr.operator, 1),
                    arrayOf("cropType", resource.cropType, 2),
                    arrayOf("imageUrl", resource.imageUrl, "https://sample.image.url/test.jpg"),
                    arrayOf("assetsUrl", resource.assetsUrl, null),
                    arrayOf("trigger type", campaignTrigger.type, 1),
                    arrayOf("trigger eventName", campaignTrigger.eventName, "Launch the App Event"),
                    arrayOf("trigger eventType", campaignTrigger.eventType, 1),
                    arrayOf("trigger attribute name", campaignTriggerAttr.name, "attribute"),
                    arrayOf("trigger attribute value", campaignTriggerAttr.value, "attrValue"),
                    arrayOf("trigger attribute type", campaignTriggerAttr.type, 1),
                    arrayOf("trigger attribute operator", campaignTriggerAttr.operator, 1)
            )
        }

        internal const val MIXER_RESPONSE = """
            {
                "currentPingMillis":1583890595467,
                "data":[{
                    "campaignData":{
                        "campaignId":"1234567890",
                        "isTest":false,
                        "messagePayload":{
                            "backgroundColor":"#000001",
                            "frameColor":"#fffff1",
                            "header":"DEV-Test",
                            "headerColor":"#fffff1",
                            "messageBody":"Response Test",
                            "messageBodyColor":"#fffff1",
                            "messageSettings":{
                                "controlSettings":{
                                    "buttons":[{
                                        "buttonBackgroundColor":"#000001",
                                        "buttonTextColor":"#fffff1",
                                        "buttonText":"Close",
                                        "buttonBehavior":{
                                            "action":2,
                                            "uri":"https://en.wikipedia.org/wiki/Test"
                                        },
                                        "campaignTrigger":{
                                            "type":2,
                                            "eventType":2,
                                            "eventName":"event name",
                                            "attributes":[{
                                                "name":"attribute name",
                                                "value":"attribute value",
                                                "type":2,
                                                "operator":2
                                            }]
                                        }
                                    }],
                                    "content":{
                                        "onClickBehavior":{
                                            "action":2,
                                            "uri":"https://en.wikipedia.org/wiki/Test1"
                                        },
                                        "campaignTrigger":{
                                            "type":2,
                                            "eventType":2,
                                            "eventName":"event",
                                            "attributes":[{
                                                "name":"attribute name2",
                                                "value":"attribute value2",
                                                "type":1,
                                                "operator":1
                                            }]
                                        }
                                    }
                                },
                                "displaySettings":{
                                    "endTimeMillis":1584109800015,
                                    "optOut":false,
                                    "orientation":1,
                                    "slideFrom":1,
                                    "textAlign":2,
                                    "delay":3000,
                                    "html":false
                                }
                            },
                            "resource":{
                                "cropType":2,
                                "imageUrl":"https://sample.image.url/test.jpg"
                            },
                            "title":"DEV-Test (Android In-App-Test)",
                            "titleColor":"#000000"
                        },
                        "triggers":[
                            {
                            "eventName":"Launch the App Event",
                            "eventType":1,
                            "attributes":[{
                                "name":"attribute",
                                "value":"attrValue",
                                "type":1,
                                "operator":1
                            }],
                            "type":1
                            },
                            {
                            "eventName":"Login Event",
                            "eventType":2,
                            "attributes":[],
                            "type":1
                            }
                        ],
                        "type":2
                    }
                }],
                "nextPingMillis":3600000
            }"""
        private val response =
                Gson().fromJson(MIXER_RESPONSE.trimIndent(), MessageMixerResponse::class.java)
        private val dataItem = DataItem(response.data[0].campaignData)
        private val campaignData = CampaignData(dataItem.campaignData.getMessagePayload(),
                dataItem.campaignData.getType(), dataItem.campaignData.getTriggers(),
                dataItem.campaignData.getCampaignId(), dataItem.campaignData.isTest(),
                dataItem.campaignData.getMaxImpressions())
        private val messagePayload = MessagePayload(campaignData.getMessagePayload().headerColor,
                campaignData.getMessagePayload().backgroundColor, campaignData.getMessagePayload().messageSettings,
                campaignData.getMessagePayload().messageBody, campaignData.getMessagePayload().resource,
                campaignData.getMessagePayload().titleColor, campaignData.getMessagePayload().header,
                campaignData.getMessagePayload().frameColor, campaignData.getMessagePayload().title,
                campaignData.getMessagePayload().messageBodyColor)
        private val messageSettings = MessageSettings(messagePayload.messageSettings.displaySettings,
                messagePayload.messageSettings.controlSettings)
        private val displaySettings = DisplaySettings(messageSettings.displaySettings.orientation,
                messageSettings.displaySettings.slideFrom, messageSettings.displaySettings.endTimeMillis,
                messageSettings.displaySettings.textAlign, messageSettings.displaySettings.optOut,
                messageSettings.displaySettings.delay, messageSettings.displaySettings.html)
        private val controlSettings = ControlSettings(messageSettings.controlSettings.buttons,
            messageSettings.controlSettings.content
        )
        private val messageButton = MessageButton(controlSettings.buttons[0].buttonBackgroundColor,
                controlSettings.buttons[0].buttonTextColor, controlSettings.buttons[0].buttonBehavior,
                controlSettings.buttons[0].buttonText, controlSettings.buttons[0].embeddedEvent)
        private val buttonOnClickBehavior = OnClickBehavior(messageButton.buttonBehavior.action,
                messageButton.buttonBehavior.uri)
        private val buttonTrigger = Trigger(messageButton.embeddedEvent?.type!!, messageButton.embeddedEvent.eventType,
                messageButton.embeddedEvent.eventName, messageButton.embeddedEvent.triggerAttributes)
        private val buttonTriggerAttr = TriggerAttribute(buttonTrigger.triggerAttributes[0].name,
                buttonTrigger.triggerAttributes[0].value, buttonTrigger.triggerAttributes[0].type,
                buttonTrigger.triggerAttributes[0].operator)
        private val controlContent = Content(controlSettings.content?.onClick!!, controlSettings.content.embeddedEvent)
        private val controlContentBehavior = OnClickBehavior(controlContent.onClick.action, controlContent.onClick.uri)
        private val controlContentTrigger = Trigger(controlContent.embeddedEvent?.type!!,
                controlContent.embeddedEvent.eventType, controlContent.embeddedEvent.eventName,
                controlContent.embeddedEvent.triggerAttributes)
        private val controlContentTriggerAttr = TriggerAttribute(controlContentTrigger.triggerAttributes[0].name,
                controlContentTrigger.triggerAttributes[0].value,
                controlContentTrigger.triggerAttributes[0].type, controlContentTrigger.triggerAttributes[0].operator)
        private val resource = Resource(messagePayload.resource.assetsUrl, messagePayload.resource.imageUrl,
                messagePayload.resource.cropType)
        private val campaignTrigger = Trigger(campaignData.getTriggers()[0].type,
                campaignData.getTriggers()[0].eventType, campaignData.getTriggers()[0].eventName,
                campaignData.getTriggers()[0].triggerAttributes)
        private val campaignTriggerAttr = TriggerAttribute(campaignTrigger.triggerAttributes[0].name,
                campaignTrigger.triggerAttributes[0].value, campaignTrigger.triggerAttributes[0].type,
                campaignTrigger.triggerAttributes[0].operator)
    }

    private fun generateDummyCampaign(id: String, title: String): CampaignData {
        val messagePayload = MessagePayload("#000000", "#ffffff",
            MessageSettings(DisplaySettings(1, 1, 1, 1, false, 1, false),
                ControlSettings(listOf())), null, Resource(cropType = 2), "#000000", null, "#ffffff", title,
            "#000000")
        return CampaignData(messagePayload, 1, listOf(), id, false, 1)
    }

    @Test
    fun `should be correct value after parsing`() {
        actual shouldBeEqualTo expected
    }

    @Test
    fun `should return empty array if there are no contexts when calling getContexts()`() {
        val campaign = generateDummyCampaign("id", "title")
        campaign.getContexts() shouldHaveSize 0
    }

    @Test
    fun `should properly read context if there's nothing beside it when calling getContexts()`() {
        val campaign = generateDummyCampaign("id", "[ctx]")
        campaign.getContexts() shouldBeEqualTo listOf("ctx")
    }

    @Test
    fun `should properly read one context when calling getContexts()`() {
        val campaign = generateDummyCampaign("id", "[ctx] title")
        campaign.getContexts() shouldBeEqualTo listOf("ctx")
    }

    @Test
    fun `should properly read multiple contexts when calling getContexts()`() {
        val campaign = generateDummyCampaign("id", "[ctx1] [ctx2][ctx3] title")
        campaign.getContexts() shouldBeEqualTo listOf("ctx1", "ctx2", "ctx3")
    }

    @Test
    fun `should properly read multiple contexts separated with characters when calling getContexts()`() {
        val campaign = generateDummyCampaign("id", "[ctx A]~~[ctx B]ab ab[ctx C]")
        campaign.getContexts() shouldBeEqualTo listOf("ctx A", "ctx B", "ctx C")
    }

    @Test
    fun `should ignore invalid contexts when calling getContexts()`() {
        val campaign = generateDummyCampaign("id", "[ctx] [ctxbad title")
        campaign.getContexts() shouldBeEqualTo listOf("ctx")
    }

    @Test
    fun `should properly read context even if there are invalid ones when calling getContexts()`() {
        val campaign = generateDummyCampaign("id", "ctxbad] title [ctx]")
        campaign.getContexts() shouldBeEqualTo listOf("ctx")
    }

    @Test
    fun `should not return any context when title is empty`() {
        val campaign = generateDummyCampaign("id", "")
        campaign.getContexts() shouldHaveSize 0
    }

    @Test
    fun `should not always start times closed to zero`() {
        val campaign = generateDummyCampaign("id", "")
        campaign.getNumberOfTimesClosed() shouldBeEqualTo 0
    }

    @Test
    fun `should have correct incremented value`() {
        val campaign = generateDummyCampaign("id", "")
        campaign.incrementTimesClosed()
        campaign.getNumberOfTimesClosed() shouldBeEqualTo 1
    }
}
