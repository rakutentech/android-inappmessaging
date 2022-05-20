package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class for parsing DataItem, which is a response from MessageMixer.
 */
@JsonClass(generateAdapter = true)
internal data class DataItem(
    @Json(name = "campaignData")
    val campaignData: CampaignData
)
