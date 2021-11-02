package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

/**
 * Class for parsing Trigger, which is a response from MessageMixer.
 */
internal data class Trigger(
    @SerializedName("type")
    val type: Int,

    @SerializedName("eventType")
    val eventType: Int,

    @SerializedName("eventName")
    val eventName: String,

    @SerializedName("attributes")
    val triggerAttributes: MutableList<TriggerAttribute>
)
