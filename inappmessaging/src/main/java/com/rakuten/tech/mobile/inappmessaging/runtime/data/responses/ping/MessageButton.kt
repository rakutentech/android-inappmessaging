package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class for parsing MessageButton, which is a response from MessageMixer.
 */
@JsonClass(generateAdapter = true)
internal data class MessageButton(
    @Json(name = "buttonBackgroundColor")
    val buttonBackgroundColor: String,

    @Json(name = "buttonTextColor")
    val buttonTextColor: String,

    @Json(name = "buttonBehavior")
    val buttonBehavior: OnClickBehavior,

    @Json(name = "buttonText")
    val buttonText: String,

    @Json(name = "campaignTrigger")
    val embeddedEvent: Trigger? = null
)
