package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

/**
 * Class for parsing DataItem, which is a response from MessageMixer.
 */
internal data class Content(
    @SerializedName("onClickBehavior")
    val onClick: OnClickBehavior,
    @SerializedName("campaignTrigger")
    val embeddedEvent: Trigger
)
