package com.rakuten.tech.mobile.inappmessaging.runtime.data.models

import com.google.gson.annotations.SerializedName

@SuppressWarnings("DataClassContainsFunctions")
internal data class Tooltip(
    @SerializedName("UIElement")
    var id: String = "",
    @SerializedName("position")
    val position: String = "",
    @SerializedName("redirectURL")
    val url: String? = null,
    @SerializedName("auto-disappear")
    val autoDisappear: Int? = null
) {
    internal fun isValid() = id.isNotEmpty() && position.isNotEmpty()
}