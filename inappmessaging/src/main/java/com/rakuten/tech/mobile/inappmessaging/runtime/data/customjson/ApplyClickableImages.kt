package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ButtonActionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Content
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.OnClickBehavior
import com.rakuten.tech.mobile.inappmessaging.runtime.data.ui.UiMessage

internal fun UiMessage.applyCustomClickableImages(clickableImages: List<ClickableImage>?): UiMessage {
    if (clickableImages.isNullOrEmpty()) {
        return this
    }

    // Right now, only single image is supported
    val image = clickableImages[0]
    if (image.image != 1 || image.redirectURL.isNullOrEmpty()) {
        return this
    }

    val newOnclick = OnClickBehavior(action = ButtonActionType.REDIRECT.typeId, uri = image.redirectURL)
//    val newOnclick = OnClickBehavior(action = ButtonActionType.REDIRECT.typeId, uri = "https://www.youtube.com")
    return this.copy(
        content = if (this.content == null) {
            Content(onClick = newOnclick)
        } else {
            this.content.copy(onClick = newOnclick)
        }
    )
}