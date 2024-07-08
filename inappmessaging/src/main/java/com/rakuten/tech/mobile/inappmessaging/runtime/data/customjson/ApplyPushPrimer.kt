package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ButtonActionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageButton
import com.rakuten.tech.mobile.inappmessaging.runtime.data.ui.UiMessage

/**
 * Applies PushPrimer CustomJson rules to [UiMessage].
 */
@SuppressWarnings("LongMethod")
internal fun UiMessage.applyCustomPushPrimer(pushPrimer: PushPrimer?): UiMessage {
    if (pushPrimer?.button == null) {
        return this
    }

    val customButtons = mutableListOf<MessageButton>()
    for ((index, rawButton) in buttons.withIndex()) {
        val shouldUpdateActionToPPrimer = pushPrimer.button == index + 1
        val customButton = if (!shouldUpdateActionToPPrimer) {
            rawButton
        } else {
            // Change the action to PushPrimer
            rawButton.copy(
                buttonBehavior = rawButton.buttonBehavior.copy(
                    action = ButtonActionType.PUSH_PRIMER.typeId,
                ),
            )
        }

        customButtons.add(customButton)
    }

    return this.copy(
        buttons = customButtons,
    )
}
