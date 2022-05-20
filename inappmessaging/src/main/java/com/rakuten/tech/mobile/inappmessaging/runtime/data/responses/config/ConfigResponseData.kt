package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data object from Config Service.
 * Part of ConfigResponse.
 * @param endpoints Config response endpoint.
 * @param rollOutPercentage Value between [0-100].
 * Gives the roll-out percentage to the SDK so it can compute if it needs to enable itself or not.
 * Example: 0 = Disabled, 65 = 65% chances of being enabled, 100 = 100% chances of being enabled
 */
@SuppressWarnings("OutdatedDocumentation")
@JsonClass(generateAdapter = true)
internal data class ConfigResponseData(
    @Json(name = "endpoints") val endpoints: ConfigResponseEndpoints? = null,
    @Json(name = "rolloutPercentage") val rollOutPercentage: Int,
)
