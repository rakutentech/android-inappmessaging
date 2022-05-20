package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class for parsing Trigger, which is a response from MessageMixer.
 */
@JsonClass(generateAdapter = true)
internal data class Trigger(
    @Json(name = "type")
    val type: Int,

    @Json(name = "eventType")
    val eventType: Int,

    @Json(name = "eventName")
    val eventName: String,

    @Json(name = "attributes")
    val triggerAttributes: MutableList<TriggerAttribute>
)
