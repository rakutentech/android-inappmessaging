package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

/**
 * Class for parsing TriggerAttribute, which is a response from MessageMixer.
 */
internal data class TriggerAttribute(
    @SerializedName("name")
    val name: String,

    @SerializedName("value")
    val value: String,

    @SerializedName("type")
    val type: Int,

    @SerializedName("operator")
    val operator: Int
)
