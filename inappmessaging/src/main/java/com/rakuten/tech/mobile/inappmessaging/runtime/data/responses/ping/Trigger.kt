package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

/**
 * Class for parsing DataItem, which is a response from MessageMixer.
 */
internal data class Trigger(
    @SerializedName("type")
    val type: Int? = null,

    @SerializedName("eventType")
    val eventType: Int? = null,

    @SerializedName("eventName")
    val eventName: String? = null,

    @SerializedName("attributes")
    val triggerAttributes: MutableList<TriggerAttribute>? = null
)
