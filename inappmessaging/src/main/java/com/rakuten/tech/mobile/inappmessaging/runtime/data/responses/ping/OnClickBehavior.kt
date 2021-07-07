package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

/**
 * Class for parsing OnClickBehavior, which is a response from MessageMixer.
 */
internal data class OnClickBehavior(
    @SerializedName("action")
    val action: Int,

    @SerializedName("uri")
    val uri: String?
)
