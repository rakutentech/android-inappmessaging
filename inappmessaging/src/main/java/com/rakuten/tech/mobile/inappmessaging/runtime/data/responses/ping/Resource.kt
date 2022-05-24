package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import androidx.annotation.RestrictTo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class for parsing Resource, which is a response from MessageMixer.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
@JsonClass(generateAdapter = true)
internal data class Resource(
    @Json(name = "assetsUrl")
    val assetsUrl: String? = null,

    @Json(name = "imageUrl")
    val imageUrl: String? = null,

    @Json(name = "cropType")
    val cropType: Int
)
