package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

internal data class CustomJson(
    val pushPrimer: PushPrimer? = null,
)

internal data class PushPrimer(
    /**
     * Buttons that will trigger push notification permission request.
     * i.e. [1, 2]
     */
    val buttons: List<Int>? = null,
)
