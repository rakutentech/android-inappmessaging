package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

internal data class CustomJson(
    val pushPrimer: PushPrimer? = null,
    val clickableImage: ClickableImage? = null,
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
