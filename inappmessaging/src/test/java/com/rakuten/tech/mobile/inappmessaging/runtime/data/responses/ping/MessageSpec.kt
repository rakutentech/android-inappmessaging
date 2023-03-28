package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import org.amshove.kluent.*
import org.junit.Test
import java.util.Date

@SuppressWarnings(
    "LargeClass",
)
@OptIn(ExperimentalStdlibApi::class)
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
                messageBody = """{"random":"invalid"}""",
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
        campaign.getTooltipConfig()?.shouldBeEquivalentTo(
            Tooltip(id = "myId", position = "top-left", autoDisappear = 5, url = "myUrl"),
        )
        campaign.type.shouldBeEqualTo(InAppMessageType.TOOLTIP.typeId)
    }
}
