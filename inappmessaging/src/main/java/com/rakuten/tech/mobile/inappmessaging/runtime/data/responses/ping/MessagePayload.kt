package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

/**
 * Class for parsing DataItem, which is a response from MessageMixer.
 */
internal data class MessagePayload(
    @SerializedName("headerColor")
    val headerColor: String? = null,

    @SerializedName("backgroundColor")
    val backgroundColor: String? = null,

    @SerializedName("messageSettings")
    val messageSettings: MessageSettings? = null,

    @SerializedName("messageBody")
    val messageBody: String? = null,

    @SerializedName("resource")
    val resource: Resource? = null,

    @SerializedName("titleColor")
    val titleColor: String? = null,

    @SerializedName("header")
    val header: String? = null,

    @SerializedName("frameColor")
    val frameColor: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("messageBodyColor")
    val messageBodyColor: String? = null
)
