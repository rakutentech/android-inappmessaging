package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

internal data class MessagePayload(
    val headerColor: String,
    val backgroundColor: String,
    val messageSettings: MessageSettings,
    val messageBody: String? = null,
    val resource: Resource,
    val titleColor: String,
    val header: String? = null,
    val frameColor: String,
    val title: String,
    val messageBodyColor: String,
)

internal data class MessageSettings(
    val displaySettings: DisplaySettings,
    val controlSettings: ControlSettings,
)

internal data class Resource(
    val assetsUrl: String? = null,
    val imageUrl: String? = null,
    val cropType: Int,
)

internal data class DisplaySettings(
    val orientation: Int,
    val slideFrom: Int,
    val endTimeMillis: Long,
    val textAlign: Int,
    @SerializedName("optOut") val isOptedOut: Boolean,
    val delay: Int,
    @SerializedName("html") val isHtml: Boolean, // currently not used (always false)
)

internal data class ControlSettings(
    val buttons: List<MessageButton>,
    val content: Content? = null,
)

internal data class MessageButton(
    val buttonBackgroundColor: String,
    val buttonTextColor: String,
    val buttonBehavior: OnClickBehavior,
    val buttonText: String,
    @SerializedName("campaignTrigger") val embeddedEvent: Trigger? = null,
)

internal data class OnClickBehavior(
    val action: Int,
    val uri: String? = null,
)

internal data class Content(
    @SerializedName("onClickBehavior") val onClick: OnClickBehavior,
    @SerializedName("campaignTrigger") val embeddedEvent: Trigger? = null,
)
