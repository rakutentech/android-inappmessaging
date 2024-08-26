package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ButtonActionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Content
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.OnClickBehavior
import com.rakuten.tech.mobile.inappmessaging.runtime.data.ui.UiMessage

internal fun UiMessage.applyClickableImage(): UiMessage {
    // ToDo: Read from CustomJson
    val newOnclick = OnClickBehavior(action = ButtonActionType.REDIRECT.typeId, uri = "https://www.google.com/")

    return this.copy(
        content = if (this.content == null) {
            Content(onClick = newOnclick)
        } else {
            this.content.copy(onClick = newOnclick)
        }
    )
}