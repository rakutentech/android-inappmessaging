package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ButtonActionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageButton
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.OnClickBehavior
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class ApplyPushPrimerSpec {
    private val message = MessageMapper.mapFrom(TestDataHelper.createDummyMessage())

    @Test
    fun `should do nothing if pushPrimer setting does not exist`() {
        val uiMessage = message.applyCustomPushPrimer(null)
        uiMessage shouldBeEqualTo message
    }

    @Test
    fun `should do nothing if pushPrimer required attributes do not exist`() {
        var uiMessage = message.applyCustomPushPrimer(PushPrimer())
        uiMessage shouldBeEqualTo message

        uiMessage = message.applyCustomPushPrimer(PushPrimer(buttons = null))
        uiMessage shouldBeEqualTo message
    }

    @Test
    fun `should do nothing if pushPrimer required buttons attribute is empty`() {
        val uiMessage = message.applyCustomPushPrimer(PushPrimer(buttons = listOf()))
        uiMessage shouldBeEqualTo message
    }

    @Test
    fun `should update button action type to Push Primer`() {
        val message = message.copy(
            buttons = listOf(
                MessageButton(
                    "#FF0000", "#FF0000",
                    OnClickBehavior(ButtonActionType.DEEPLINK.typeId, ""), "text", null,
                ),
            ),
        )

        val uiMessage = message.applyCustomPushPrimer(PushPrimer(buttons = listOf(1)))

        uiMessage.buttons.size shouldBeEqualTo 1
        uiMessage.buttons[0].buttonBehavior.action shouldBeEqualTo ButtonActionType.PUSH_PRIMER.typeId
    }

    @Test
    fun `should not update button action type to PushPrimer`() {
        val message = message.copy(
            buttons = listOf(
                MessageButton(
                    "#FF0000", "#FF0000",
                    OnClickBehavior(ButtonActionType.DEEPLINK.typeId, ""), "text", null,
                ),
            ),
        )

        val uiMessage = message.applyCustomPushPrimer(PushPrimer(buttons = listOf(99)))

        uiMessage.buttons.size shouldBeEqualTo 1
        uiMessage.buttons[0].buttonBehavior.action shouldBeEqualTo ButtonActionType.DEEPLINK.typeId
    }
}
