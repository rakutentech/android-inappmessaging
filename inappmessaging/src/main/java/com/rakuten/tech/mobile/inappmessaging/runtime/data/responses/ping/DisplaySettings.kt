package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class for parsing DisplaySettings, which is a response from MessageMixer.
 */
@JsonClass(generateAdapter = true)
internal data class DisplaySettings(
    @Json(name = "orientation")
    val orientation: Int,

    @Json(name = "slideFrom")
    val slideFrom: Int,

    @Json(name = "endTimeMillis")
    val endTimeMillis: Long,

    @Json(name = "textAlign")
    val textAlign: Int,

    @Json(name = "optOut")
    val optOut: Boolean,

    @Json(name = "delay")
    val delay: Int,

    @Json(name = "html")
    val html: Boolean // currently not used (always false)
)
