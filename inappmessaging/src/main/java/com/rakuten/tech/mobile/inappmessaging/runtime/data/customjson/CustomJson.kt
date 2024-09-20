package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

internal data class CustomJson(
    val pushPrimer: PushPrimer? = null,
    val clickableImage: ClickableImage? = null,
    val background: Background? = null,
)

internal data class PushPrimer(
    /**
     * Button index that will trigger push notification permission request.
     */
    val button: Int? = null,
)

internal data class ClickableImage(
    /**
     * Redirect URL or deeplink.
     */
    val url: String? = null,
)

/**
 * Backdrop color.
 */
internal data class Background(
    /**
     * Opacity from 0 (completely transparent) to 1 (completely opaque).
     */
    val opacity: Float? = null,
)
