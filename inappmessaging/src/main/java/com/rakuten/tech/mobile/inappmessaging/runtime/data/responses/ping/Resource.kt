package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import androidx.annotation.RestrictTo
import com.google.gson.annotations.SerializedName

/**
 * Class for parsing DataItem, which is a response from MessageMixer.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal data class Resource(
    @SerializedName("assetsUrl")
    val assetsUrl: String?,

    @SerializedName("imageUrl")
    val imageUrl: String?,

    @SerializedName("cropType")
    val cropType: Int?
)
