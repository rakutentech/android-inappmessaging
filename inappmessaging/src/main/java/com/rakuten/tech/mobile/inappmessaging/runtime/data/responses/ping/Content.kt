package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class for parsing Content, which is a response from MessageMixer.
 */
@JsonClass(generateAdapter = true)
internal data class Content(
    @Json(name = "onClickBehavior")
    val onClick: OnClickBehavior,
    @Json(name = "campaignTrigger")
    val embeddedEvent: Trigger? = null
)
