package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

import com.google.gson.JsonParser
import com.rakuten.tech.mobile.inappmessaging.runtime.RmcHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ButtonActionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageButton
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.OnClickBehavior
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Resource
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mockStatic

@SuppressWarnings("LongMethod")
class MessageMapperSpec {
    private val message = TestDataHelper.createDummyMessage()
    private val mockRmcHelper = mockStatic(RmcHelper::class.java)

    @Before
    fun setup() {
        mockRmcHelper.`when`<Any> { RmcHelper.isRmcIntegrated() }.thenReturn(true)
    }

    @After
    fun tearDown() {
        mockRmcHelper.close()
    }

    @Test
    fun `should do nothing if there is no customJson`() {
        val uiMessage = MessageMapper.mapFrom(message)

        MessageMapper.mapFrom(message) shouldBeEqualTo uiMessage
    }

    @Test
    fun `should apply custom PushPrimer setting`() {
        val settings = TestDataHelper.message0Payload.messageSettings
        val payload = TestDataHelper.message0Payload.copy(
            messageSettings = settings.copy(
                controlSettings = settings.controlSettings.copy(
                    buttons = listOf(
                        MessageButton(
                            "#FF0000", "#FF0000",
                            OnClickBehavior(ButtonActionType.DEEPLINK.typeId, ""), "text", null,
                        ),
                    ),
                ),
            ),
        )

        val uiMessage = MessageMapper.mapFrom(
            TestDataHelper.createDummyMessage(
                messagePayload = payload,
                customJson = JsonParser.parseString("""{"pushPrimer": { "button": 1 }}""").asJsonObject,
            ),
        )

        uiMessage.buttons.size shouldBeEqualTo 1
        uiMessage.buttons[0].buttonBehavior.action shouldBeEqualTo ButtonActionType.PUSH_PRIMER.typeId
    }

    @Test
    fun `should apply custom ClickableImage setting`() {
        val settings = TestDataHelper.message0Payload.messageSettings
        val payload = TestDataHelper.message0Payload.copy(
            resource = Resource(imageUrl = "http://test/image.png", cropType = 0),
            messageSettings = settings.copy(controlSettings = settings.controlSettings.copy(content = null)),
        )

        val uiMessage = MessageMapper.mapFrom(
            TestDataHelper.createDummyMessage(
                messagePayload = payload,
                customJson = JsonParser.parseString("""{"clickableImage": { "url": "http://test.com" }}""")
                    .asJsonObject,
            ),
        )

        uiMessage.content shouldNotBeEqualTo null
        uiMessage.content?.onClick shouldNotBeEqualTo null
        uiMessage.content?.onClick?.action shouldBeEqualTo ButtonActionType.REDIRECT.typeId
        uiMessage.content?.onClick?.uri shouldBeEqualTo "http://test.com"
    }
}
