package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import org.amshove.kluent.shouldBeEquivalentTo
import org.junit.Test

@OptIn(ExperimentalStdlibApi::class)
class MessageMixerResponseSpec {

    @Test
    fun `should deserialize MessageMixerResponse from json field names set`() {
        val json = TestDataHelper.messageMixerResponseJson
        val testDataClass = Gson().fromJson(json, MessageMixerResponse::class.java)
        testDataClass.shouldBeEquivalentTo(
            MessageMixerResponse(
                currentPingMillis = 1583890595467,
                nextPingMillis = 3600000,
                data = listOf(
                    DataItem(
                        campaignData = Message(
                            campaignId="1234567890",
                            maxImpressions=100,
                            areImpressionsInfinite=false,
                            hasNoEndDate=true,
                            isCampaignDismissable=false,
                            type=2,
                            isTest=false,
                            triggers=listOf(
                                Trigger(
                                    type=1,
                                    eventType=1,
                                    eventName="Launch the App Event",
                                    triggerAttributes= mutableListOf(
                                        TriggerAttribute(
                                            name="attribute",
                                            value="attrValue",
                                            type=1,
                                            operator=1
                                        ),
                                    ),
                                ),
                                Trigger(type=1,
                                    eventType=2,
                                    eventName="Login Event",
                                    triggerAttributes= mutableListOf()
                                ),
                            ),
                            messagePayload=MessagePayload(
                                headerColor="#ffffff",
                                backgroundColor="#000000",
                                messageSettings=MessageSettings(
                                    displaySettings=DisplaySettings(
                                        orientation=1,
                                        slideFrom=1,
                                        endTimeMillis=1584109800000,
                                        textAlign=2,
                                        isOptedOut=false,
                                        delay=0,
                                        isHtml=false),
                                    controlSettings=ControlSettings(
                                        buttons=listOf(
                                            MessageButton(
                                                buttonBackgroundColor="#000000",
                                                buttonTextColor="#ffffff",
                                                buttonBehavior=OnClickBehavior(
                                                    action=1,
                                                    uri="https://en.wikipedia.org/wiki/Test"
                                                ),
                                                buttonText="Test",
                                                embeddedEvent=Trigger(
                                                    type=1,
                                                    eventType=4,
                                                    eventName="custom",
                                                    triggerAttributes= mutableListOf(
                                                        TriggerAttribute(
                                                            name="attribute1",
                                                            value="attrValue1",
                                                            type=1,
                                                            operator=1
                                                        ),
                                                        TriggerAttribute(
                                                            name="attribute2",
                                                            value="1",
                                                            type=2,
                                                            operator=1
                                                        ),
                                                        TriggerAttribute(
                                                            name="attribute3",
                                                            value="1.0",
                                                            type=3,
                                                            operator=1
                                                        ),
                                                        TriggerAttribute(
                                                            name="attribute4",
                                                            value="true",
                                                            type=4,
                                                            operator=1
                                                        ),
                                                        TriggerAttribute(
                                                            name="attribute5",
                                                            value="1234567",
                                                            type=5,
                                                            operator=1
                                                        )
                                                    )
                                                ),
                                            ),
                                            MessageButton(
                                                buttonBackgroundColor="#000fff",
                                                buttonTextColor="#fff000",
                                                buttonBehavior=OnClickBehavior(
                                                    action=2,
                                                    uri="https://test.url"
                                                ),
                                                buttonText="Redirect",
                                                embeddedEvent=Trigger(
                                                    type=2,
                                                    eventType=3,
                                                    eventName="test",
                                                    triggerAttributes= mutableListOf(
                                                        TriggerAttribute(
                                                            name="attribute",
                                                            value="attribute Value",
                                                            type=1,
                                                            operator=1
                                                        ),
                                                    ),
                                                ),
                                            ),
                                        ),
                                        content=Content(
                                            onClick=OnClickBehavior(
                                                action=1,
                                                uri="https://sample.url"
                                            ),
                                            embeddedEvent=Trigger(
                                                type=1,
                                                eventType=1,
                                                eventName="event",
                                                triggerAttributes= mutableListOf(
                                                    TriggerAttribute(
                                                        name="attribute name",
                                                        value="value",
                                                        type=1,
                                                        operator=1
                                                    ),
                                                ),
                                            ),
                                        ),
                                    ),
                                ),
                                messageBody="Response Test",
                                resource=Resource(
                                    assetsUrl=null,
                                    imageUrl=null,
                                    cropType=2
                                ),
                                titleColor="#000000",
                                header="DEV-Test",
                                frameColor="#ffffff",
                                title="DEV-Test (Android In-App-Test)",
                                messageBodyColor="#ffffff"
                            ),
                        ),
                    ),
                ),
            ),
        )
    }
}