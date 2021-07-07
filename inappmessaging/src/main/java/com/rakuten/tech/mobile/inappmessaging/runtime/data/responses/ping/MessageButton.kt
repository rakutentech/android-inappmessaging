package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

/**
 * Class for parsing MessageButton, which is a response from MessageMixer.
 */
internal data class MessageButton(
    @SerializedName("buttonBackgroundColor")
    val buttonBackgroundColor: String,

    @com.google.gson.annotations.SerializedName("buttonTextColor")
    val buttonTextColor: String,

    @SerializedName("buttonBehavior")
    val buttonBehavior: OnClickBehavior,

    @SerializedName("buttonText")
    val buttonText: String,

    @SerializedName("campaignTrigger")
    val embeddedEvent: Trigger
)
