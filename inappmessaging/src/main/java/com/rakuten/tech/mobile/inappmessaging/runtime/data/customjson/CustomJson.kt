package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

internal data class CustomJson(
    val pushPrimer: PushPrimer? = null,
    val clickableImage: List<ClickableImage>? = null
)

internal data class PushPrimer(
    /**
     * Button index that will trigger push notification permission request.
     */
    val button: Int? = null,
)

internal data class ClickableImage(
    /**
     * Image index
     */
    val image: Int? = null,

    /**
     * External URL or deeplink
     */
    val redirectURL: String? = null
)