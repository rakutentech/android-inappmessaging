package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses

import com.google.gson.annotations.SerializedName

internal data class ConfigResponse(
    val data: ConfigResponseData? = null,
)

/**
 * @property endpoints Config response endpoint.
 * @property rollOutPercentage Value between [0-100].
 * Gives the roll-out percentage to the SDK so it can compute if it needs to enable itself or not.
 * Example: 0 = Disabled, 65 = 65% chances of being enabled, 100 = 100% chances of being enabled
 */
internal data class ConfigResponseData(
    val endpoints: ConfigResponseEndpoints? = null,
    @SerializedName("rolloutPercentage") val rollOutPercentage: Int,
)

internal data class ConfigResponseEndpoints(
    val ping: String? = null,
    val impression: String? = null,
    val displayPermission: String? = null,
)
