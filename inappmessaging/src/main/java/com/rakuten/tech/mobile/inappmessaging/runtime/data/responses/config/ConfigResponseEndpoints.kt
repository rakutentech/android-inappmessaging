package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config

import com.google.gson.annotations.SerializedName

/**
 * Endpoints object.
 * Part of ConfigResponse from Config Service.
 */
internal data class ConfigResponseEndpoints(
    @SerializedName("ping") val ping: String? = null,
    @SerializedName("impression") val impression: String? = null,
    @SerializedName("displayPermission") val displayPermission: String? = null,
)
