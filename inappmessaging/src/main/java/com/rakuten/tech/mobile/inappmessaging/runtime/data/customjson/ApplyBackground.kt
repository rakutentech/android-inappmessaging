package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

import com.rakuten.tech.mobile.inappmessaging.runtime.data.ui.UiMessage

internal fun UiMessage.applyBackground(background: Background?): UiMessage {
    if (background?.opacity == null ||
        background.opacity !in 0f..1f
    ) {
        return this
    }

    return this.copy(
        backdropOpacity = background.opacity,
    )
}
