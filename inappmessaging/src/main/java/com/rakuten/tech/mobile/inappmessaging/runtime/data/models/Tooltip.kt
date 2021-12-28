package com.rakuten.tech.mobile.inappmessaging.runtime.data.models

import com.google.gson.annotations.SerializedName

internal data class Tooltip(
    @SerializedName("UIElement")
    val id: String,
    @SerializedName("position")
    val position: String,
    @SerializedName("color")
    val color: String,
    @SerializedName("redirectURL")
    val url: String?,
    @SerializedName("auto-fade")
    val autoFade: Int?
)
