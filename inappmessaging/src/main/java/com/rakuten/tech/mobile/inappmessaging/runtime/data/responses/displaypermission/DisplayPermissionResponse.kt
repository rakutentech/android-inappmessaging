package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.displaypermission

import androidx.annotation.RestrictTo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * This class represents display permission response.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
@JsonClass(generateAdapter = true)
internal data class DisplayPermissionResponse(
    @Json(name = "display")
    val display: Boolean,

    @Json(name = "performPing")
    val performPing: Boolean
)
