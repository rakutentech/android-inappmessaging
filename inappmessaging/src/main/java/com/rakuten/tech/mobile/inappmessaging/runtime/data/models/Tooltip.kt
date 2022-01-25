package com.rakuten.tech.mobile.inappmessaging.runtime.data.models

import com.google.gson.annotations.SerializedName

internal data class Tooltip(
    @SerializedName("UIElement")
    var id: String,
    @SerializedName("position")
    val position: String,
    @SerializedName("redirectURL")
    val url: String?,
    @SerializedName("auto-disappear")
    val autoDisappear: Int?
)
