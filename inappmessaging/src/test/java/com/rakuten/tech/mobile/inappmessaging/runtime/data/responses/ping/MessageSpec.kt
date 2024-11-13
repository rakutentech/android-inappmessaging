package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.JsonParser
import com.rakuten.tech.mobile.inappmessaging.runtime.RmcHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson.Background
import com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson.CustomJson
import com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson.PushPrimer
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mockStatic
import java.util.Date

@SuppressWarnings(
    "LargeClass",
)
class MessageSpec {

    @Test
    fun `should return empty array if there are no contexts when calling contexts`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(title = "title"),
        )
        campaign.contexts shouldHaveSize 0
    }

    @Test
    fun `should properly read context if there's nothing beside it when calling contexts`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(title = "[ctx]"),
        )
        campaign.contexts shouldBeEqualTo listOf("ctx")
    }

    @Test
    fun `should properly read one context when calling contexts`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(title = "[ctx] title"),
        )
        campaign.contexts shouldBeEqualTo listOf("ctx")
    }

    @Test
    fun `should properly read multiple contexts when calling contexts`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(title = "[ctx1] [ctx2][ctx3] title"),
        )
        campaign.contexts shouldBeEqualTo listOf("ctx1", "ctx2", "ctx3")
    }

    @Test
    fun `should properly read multiple contexts separated with characters when calling contexts`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(title = "[ctx A]~~[ctx B]ab ab[ctx C]"),
        )
        campaign.contexts shouldBeEqualTo listOf("ctx A", "ctx B", "ctx C")
    }

    @Test
    fun `should ignore invalid contexts when calling contexts`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(title = "[ctx] [ctxbad title"),
        )
        campaign.contexts shouldBeEqualTo listOf("ctx")
    }

    @Test
    fun `should properly read context even if there are invalid ones when calling contexts`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(title = "ctxbad] title [ctx]"),
        )
        campaign.contexts shouldBeEqualTo listOf("ctx")
    }

    @Test
    fun `should not return any context when title is empty`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(title = ""),
        )
        campaign.contexts shouldHaveSize 0
    }

    @Test
    fun `should return false if campaign has no end date when calling isOutdated`() {
        val campaign = TestDataHelper.createDummyMessage(hasNoEndDate = true)
        campaign.isOutdated.shouldBeFalse()
    }

    @Test
    fun `should return false if campaign duration has elapsed when calling isOutdated`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(
                messageSettings = TestDataHelper.message0Payload.messageSettings.copy(
                    displaySettings = TestDataHelper.message0Payload.messageSettings.displaySettings.copy(
                        endTimeMillis = Date().time,
                    ),
                ),
            ),
        )
        campaign.isOutdated.shouldBeFalse()
    }

    @Test
    fun `should return true if within campaign duration when calling isOutdated`() {
        val campaign = TestDataHelper.createDummyMessage(
            hasNoEndDate = false,
            messagePayload = TestDataHelper.createDummyPayload(
                messageSettings = TestDataHelper.message0Payload.messageSettings.copy(
                    displaySettings = TestDataHelper.message0Payload.messageSettings.displaySettings.copy(
                        endTimeMillis = 0,
                    ),
                ),
            ),
        )
        campaign.isOutdated.shouldBeTrue()
    }

    @Test
    fun `should return maxImpressions by default when calling impressionsLeft`() {
        val campaign = TestDataHelper.createDummyMessage().apply { impressionsLeft = null }
        campaign.impressionsLeft.shouldBeEqualTo(campaign.maxImpressions)
    }

    @Test
    fun `should return impressionsLeft`() {
        val campaign = TestDataHelper.createDummyMessage().apply { impressionsLeft = 100 }
        campaign.impressionsLeft.shouldBeEqualTo(100)
    }

    @Test
    fun `should return false by default when calling isOptedOut`() {
        val campaign = TestDataHelper.createDummyMessage().apply { isOptedOut = null }
        campaign.isOptedOut?.shouldBeFalse()
    }

    @Test
    fun `should return isOptedOut`() {
        val campaign = TestDataHelper.createDummyMessage().apply { isOptedOut = true }
        campaign.isOptedOut?.shouldBeTrue()
    }

    @Test
    fun `should return null if normal campaign when calling getTooltipConfig()`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(title = "normal"),
        )
        campaign.getTooltipConfig().shouldBeNull()
        campaign.type.shouldNotBeEqualTo(InAppMessageType.TOOLTIP.typeId)
    }

    @Test
    fun `should return null if missing id when calling getTooltipConfig()`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(
                title = "${Message.TOOLTIP_TAG} Test",
                messageBody = """{"position":"top-left","auto-disappear":5,"redirectURL":"myUrl"}""",
            ),
        )
        campaign.getTooltipConfig().shouldBeNull()
        campaign.type.shouldNotBeEqualTo(InAppMessageType.TOOLTIP.typeId)
    }

    @Test
    fun `should return null if missing position when calling getTooltipConfig()`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(
                title = "${Message.TOOLTIP_TAG} Test",
                messageBody = """{"UIElement":"myId","auto-disappear":5,"redirectURL":"myUrl"}""",
            ),
        )
        campaign.getTooltipConfig().shouldBeNull()
        campaign.type.shouldNotBeEqualTo(InAppMessageType.TOOLTIP.typeId)
    }

    @Test
    fun `should return null if body is invalid format when calling getTooltipConfig()`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(
                title = "${Message.TOOLTIP_TAG} Test",
                messageBody = """random":"invalid"}""",
            ),
        )
        campaign.getTooltipConfig().shouldBeNull()
        campaign.type.shouldNotBeEqualTo(InAppMessageType.TOOLTIP.typeId)
    }

    @Test
    fun `should return valid tooltip when calling getTooltipConfig()`() {
        val campaign = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(
                title = "${Message.TOOLTIP_TAG} Test",
                messageBody = """{"UIElement":"myId","position":"top-left","auto-disappear":5,"redirectURL":"myUrl"}""",
            ),
        )
        campaign.getTooltipConfig() shouldBeEqualTo
            Tooltip(id = "myId", position = "top-left", autoDisappear = 5, url = "myUrl")
        campaign.type.shouldBeEqualTo(InAppMessageType.TOOLTIP.typeId)
    }

    @Test
    fun `should correctly map message type`() {
        val message = Message(
            "",
            0,
            false,
            false,
            true,
            0,
            true,
            null,
            TestDataHelper.message0Payload,
        )
        message.type shouldBe 0
        message.type = 1
        message.type shouldBe 1
    }

    @Test
    fun `should map messagePayload defaults`() {
        val messagePayload = MessagePayload(
            "",
            "",
            TestDataHelper.message0Payload.messageSettings,
            resource = TestDataHelper.message0Payload.resource,
            titleColor = "",
            frameColor = "",
            title = "",
            messageBodyColor = "",
        )
        messagePayload.messageBody shouldBe null
        messagePayload.header shouldBe null
    }

    @Test
    fun `should map messageButton defaults`() {
        val messageButton = MessageButton(
            "",
            "",
            OnClickBehavior(action = 0),
            "",
        )
        messageButton.embeddedEvent shouldBe null
    }

    @Test
    fun `should map content defaults`() {
        val content = Content(
            OnClickBehavior(action = 0),
        )
        content.embeddedEvent shouldBe null
    }

    @Test
    fun `should map resource defauls`() {
        val res = Resource(cropType = 0)
        res.imageUrl shouldBe null
        res.assetsUrl shouldBe null
        res.cropType shouldBe 0
    }

    @Test
    fun `should map displaySettings defaults`() {
        val settings = DisplaySettings(
            0,
            0,
            0,
            0,
            false,
            0,
            false,
        )
        settings.orientation shouldBe 0
        settings.textAlign shouldBe 0
        settings.delay shouldBe 0
        settings.isHtml shouldBe false
    }
}

class MessageCustomJsonSpec {

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
    fun `should return null CustomJson data if customJson from response is null`() {
        val campaign = TestDataHelper.createDummyMessage(customJson = null)
        campaign.getCustomJsonData() shouldBeEqualTo null
    }

    @Test
    fun `should return null CustomJson data if customJson from response is empty object`() {
        val campaign = TestDataHelper.createDummyMessage(customJson = JsonParser.parseString("{}").asJsonObject)
        campaign.getCustomJsonData() shouldBeEqualTo null
    }

    @Test
    fun `should return null CustomJson data if customJson from response has an unrecognized format`() {
        val campaign = TestDataHelper.createDummyMessage(
            customJson = JsonParser.parseString("""{"pushPrimer": true}""").asJsonObject,
        )
        campaign.getCustomJsonData()?.pushPrimer shouldBeEqualTo null
    }

    @Test
    fun `should return null CustomJson data if RMC is not integrated`() {
        mockRmcHelper.`when`<Any> { RmcHelper.isRmcIntegrated() }.thenReturn(false)

        val campaign = TestDataHelper.createDummyMessage(
            customJson = JsonParser.parseString("""{"pushPrimer": true}""").asJsonObject,
        )
        campaign.getCustomJsonData() shouldBeEqualTo null
    }

    @Test
    fun `should map CustomJson data for valid customJson response`() {
        val campaign = TestDataHelper.createDummyMessage(
            customJson = JsonParser.parseString("""{"pushPrimer": { "button": 1 }}""").asJsonObject,
        )
        campaign.getCustomJsonData() shouldBeEqualTo CustomJson(pushPrimer = PushPrimer(button = 1))
    }

    @Test
    fun `should map CustomJson data even if there is an unknown feature key`() {
        val campaign = TestDataHelper.createDummyMessage(
            customJson = JsonParser.parseString(
                """{ "pushPrimer": { "button": 1 }, "unknownKey": "unknownValue" }""",
            ).asJsonObject,
        )
        campaign.getCustomJsonData() shouldBeEqualTo CustomJson(pushPrimer = PushPrimer(button = 1))
    }

    @Test
    fun `should map CustomJson data even if there is a feature key with invalid attribute`() {
        val campaign = TestDataHelper.createDummyMessage(
            customJson = JsonParser.parseString(
                """{ "pushPrimer": { "button": "abcdef" }, "background": { "opacity": 0.6 } }""",
            ).asJsonObject,
        )
        campaign.getCustomJsonData() shouldBeEqualTo CustomJson(background = Background(opacity = 0.6f))
    }
}
