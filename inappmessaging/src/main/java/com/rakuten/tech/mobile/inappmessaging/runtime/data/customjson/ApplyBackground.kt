package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

import com.rakuten.tech.mobile.inappmessaging.runtime.data.ui.UiMessage

internal fun UiMessage.applyBackground(background: Background?): UiMessage {
    return this.copy(
        backdropOpacity = background?.opacity
    )
}