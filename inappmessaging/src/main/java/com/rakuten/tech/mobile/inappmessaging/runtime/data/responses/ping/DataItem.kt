package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

/**
 * Class for parsing DataItem, which is a response from MessageMixer.
 */
internal data class DataItem(
    @SerializedName("campaignData")
    val campaignData: CampaignData
)
