package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config

import com.google.gson.annotations.SerializedName

/**
 * Config Response class for GSON.
 */
internal data class ConfigResponse(
    @SerializedName("data") val data: ConfigResponseData? = null,
)
