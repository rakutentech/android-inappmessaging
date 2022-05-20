package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class for parsing MessagePayload, which is a response from MessageMixer.
 */
@JsonClass(generateAdapter = true)
internal data class MessagePayload(
    @Json(name = "headerColor")
    val headerColor: String,

    @Json(name = "backgroundColor")
    val backgroundColor: String,

    @Json(name = "messageSettings")
    val messageSettings: MessageSettings,

    @Json(name = "messageBody")
    val messageBody: String? = null,

    @Json(name = "resource")
    val resource: Resource,

    @Json(name = "titleColor")
    val titleColor: String,

    @Json(name = "header")
    val header: String? = null,

    @Json(name = "frameColor")
    val frameColor: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "messageBodyColor")
    val messageBodyColor: String
)
