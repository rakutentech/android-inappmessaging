package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

internal data class CustomJson(
    val pushPrimer: PushPrimer? = null,
)

internal data class PushPrimer(
    /**
     * Buttons that will trigger the OS push notification prompt.
     * i.e. ["1", "2"]
     */
    val buttons: List<String>? = null,
)
