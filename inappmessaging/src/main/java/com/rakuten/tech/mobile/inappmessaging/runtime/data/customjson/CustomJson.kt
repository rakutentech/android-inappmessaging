package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

internal data class CustomJson(
    val pushPrimer: PushPrimer? = null,
)

internal data class PushPrimer(
    /**
     * Button index that will trigger push notification permission request.
     */
    val button: Int? = null,
)
