package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class for parsing OnClickBehavior, which is a response from MessageMixer.
 */
@JsonClass(generateAdapter = true)
internal data class OnClickBehavior(
    @Json(name = "action")
    val action: Int,

    @Json(name = "uri")
    val uri: String? = null
)
