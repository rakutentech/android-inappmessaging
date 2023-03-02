package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

/**
 * Class for parsing MessagePayload, which is a response from MessageMixer.
 */
internal data class MessagePayload(
    @SerializedName("headerColor")
    val headerColor: String,

    @SerializedName("backgroundColor")
    val backgroundColor: String,

    @SerializedName("messageSettings")
    val messageSettings: MessageSettings,

    @SerializedName("messageBody")
    val messageBody: String? = null,

    @SerializedName("resource")
    val resource: Resource,

    @SerializedName("titleColor")
    val titleColor: String,

    @SerializedName("header")
    val header: String? = null,

    @SerializedName("frameColor")
    val frameColor: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("messageBodyColor")
    val messageBodyColor: String,
)
