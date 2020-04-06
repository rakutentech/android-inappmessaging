package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config

import com.google.gson.annotations.SerializedName

/**
 * Data object from Config Service.
 * Part of ConfigResponse.
 */
internal data class ConfigResponseData(
    @SerializedName("endpoints") val endpoints: ConfigResponseEndpoints? = null,
    @SerializedName("enabled") val enabled: Boolean = false
)
