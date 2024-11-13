package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Content
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.OnClickBehavior
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class ApplyClickableImageSpec {
    private val message = MessageMapper.mapFrom(TestDataHelper.createDummyMessage()).copy(
        content = null,
        imageUrl = "http://test.com",
        type = InAppMessageType.MODAL.typeId,
    )

    @Test
    fun `should do nothing if clickableImage setting does not exist`() {
        val uiMessage = message.applyCustomClickableImage(null, false)
        uiMessage shouldBeEqualTo message
    }

    @Test
    fun `should do nothing if url attribute is invalid`() {
        var uiMessage = message.applyCustomClickableImage(ClickableImage(), false)
        uiMessage shouldBeEqualTo message

        uiMessage = message.applyCustomClickableImage(ClickableImage(""), false)
        uiMessage shouldBeEqualTo message

        uiMessage = message.applyCustomClickableImage(ClickableImage("ogle.124dsefsd"), false)
        uiMessage shouldBeEqualTo message

        uiMessage = message.applyCustomClickableImage(ClickableImage("intent:/invalid/deeplink"), false)
        uiMessage shouldBeEqualTo message

        uiMessage = message.applyCustomClickableImage(ClickableImage("   myapp://open"), false)
        uiMessage shouldBeEqualTo message

        uiMessage = message.applyCustomClickableImage(ClickableImage("https://test.com   "), false)
        uiMessage shouldBeEqualTo message
    }

    @Test
    fun `should do nothing if imageUrl does not exist or empty`() {
        var uiMessage = message.copy(imageUrl = null)
            .applyCustomClickableImage(ClickableImage("https://test.com"), false)
        uiMessage.content?.onClick?.uri shouldBeEqualTo null

        uiMessage = message.copy(imageUrl = "")
            .applyCustomClickableImage(ClickableImage("https://test.com"), false)
        uiMessage.content?.onClick?.uri shouldBeEqualTo null
    }

    @Test
    fun `should do nothing if campaign layout isn't clickable`() {
        val uiMessage = message.copy(type = InAppMessageType.SLIDE.typeId)
            .applyCustomClickableImage(ClickableImage("https://test.com"), false)

        uiMessage.content?.onClick?.uri shouldBeEqualTo null
    }

    @Test
    fun `should do nothing if campaign is a PushPrimer`() {
        val uiMessage = message.copy(type = InAppMessageType.MODAL.typeId)
            .applyCustomClickableImage(ClickableImage("https://test.com"), true)

        uiMessage.content?.onClick?.uri shouldBeEqualTo null
    }

    @Test
    fun `should update content url to clickableImage url for null content data`() {
        val uiMessage = message.copy(type = InAppMessageType.MODAL.typeId)
            .applyCustomClickableImage(ClickableImage("https://test.com"), false)

        uiMessage.content?.onClick?.uri shouldBeEqualTo "https://test.com"
    }

    @Test
    fun `should update content url to clickableImage url for non-null content data`() {
        val uiMessage = message.copy(
            type = InAppMessageType.MODAL.typeId,
            content = Content(onClick = OnClickBehavior(3)),
        )
            .applyCustomClickableImage(ClickableImage("myapp://open?param=value"), false)

        uiMessage.content?.onClick?.uri shouldBeEqualTo "myapp://open?param=value"
    }
}
