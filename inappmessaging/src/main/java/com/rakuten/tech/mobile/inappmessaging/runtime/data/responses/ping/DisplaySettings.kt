package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

/**
 * Class for parsing DisplaySettings, which is a response from MessageMixer.
 */
internal data class DisplaySettings(
    @SerializedName("orientation")
    val orientation: Int,

    @SerializedName("slideFrom")
    val slideFrom: Int,

    @SerializedName("endTimeMillis")
    val endTimeMillis: Long,

    @SerializedName("textAlign")
    val textAlign: Int,

    @SerializedName("optOut")
    val optOut: Boolean,

    @SerializedName("delay")
    val delay: Int,

    @SerializedName("html")
    val html: Boolean // currently not used (always false)
)
