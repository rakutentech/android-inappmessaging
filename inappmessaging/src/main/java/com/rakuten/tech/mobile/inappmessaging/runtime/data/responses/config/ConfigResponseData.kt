package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config

import com.google.gson.annotations.SerializedName

/**
 * Data object from Config Service.
 * Part of ConfigResponse.
 * @param rollOutPercentage Value between [0-100]
 * Gives the roll-out percentage to the SDK so it can compute if it needs to enable itself or not.
 * Example: 0 = Disabled, 65 = 65% chances of being enabled, 100 = 100% chances of being enabled
 */
internal data class ConfigResponseData(
    @SerializedName("endpoints") val endpoints: ConfigResponseEndpoints? = null,
    @SerializedName("rolloutPercentage") val rollOutPercentage: Int,
)
