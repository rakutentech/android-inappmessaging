package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import androidx.annotation.RestrictTo
import com.google.gson.annotations.SerializedName

/**
 * Class for parsing Resource, which is a response from MessageMixer.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal data class Resource(
    @SerializedName("assetsUrl")
    val assetsUrl: String? = null,

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("cropType")
    val cropType: Int
)
