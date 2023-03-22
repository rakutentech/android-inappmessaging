package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

/**
 * Class for parsing response from MessageMixer.
 * @property nextPingMillis Next ping to MessageMixer in millisecond.
 * @property currentPingMillis UTC time when MessageMixer returned response.
 */
internal data class PingResponse(
    val data: List<DataItem>,
    val nextPingMillis: Long,
    val currentPingMillis: Long,
)

internal data class DataItem(
    val campaignData: Message,
)