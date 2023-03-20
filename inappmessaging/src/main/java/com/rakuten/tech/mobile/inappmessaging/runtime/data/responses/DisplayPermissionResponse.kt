package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses

import androidx.annotation.RestrictTo
import com.google.gson.annotations.SerializedName

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal data class DisplayPermissionResponse(
    val display: Boolean,
    @SerializedName("performPing") val shouldPing: Boolean,
)
