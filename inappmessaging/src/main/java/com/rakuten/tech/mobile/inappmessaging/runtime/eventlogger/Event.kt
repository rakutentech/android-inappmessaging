package com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger

/**
 * https:<company-confluence-link>/display/MAGS/IAM+Event+Logger%3A+SDK+Events
 */
internal sealed class Event(
    val type: EventType,
    open val code: String,
    open var message: String = "",
    open val info: MutableMap<String, String> = mutableMapOf(),
) {
    // May be used for API request errors.
    // 3, 5, 6, 7, 9, 13, 14, 15, 22, 29
    data class ApiRequestFailed(
        val api: BackendApi,
        override val code: String,
        val severity: EventType = EventType.CRITICAL,
    ) : Event(
        severity,
        code,
    )

    // May be used for generic errors in the SDK (such as a caught exception).
    // 10, 17, 18, 19, 31, 39, 40
    data class OperationFailed(
        val tag: String,
        val severity: EventType = EventType.WARNING,
    ) : Event(
        severity,
        "${tag}_FAILED",
    )

    // 4
    object ConfigInvalidVConfiguration : Event(
        EventType.CRITICAL,
        "CONFIG_INVALID_CONFIGURATION",
    )

    // 8
    object ConfigJsonDecodingError : Event(
        EventType.WARNING,
        "CONFIG_JSON_DECODING_ERROR",
    )

    // 20
    object DisplayPermMissingMdata : Event(
        EventType.WARNING,
        "DISPLAY_PERMISSION_MISSING_METADATA",
    )

    // 27
    object ImpressionMissingMdata : Event(
        EventType.WARNING,
        "IMPRESSION_MISSING_METADATA",
    )

    // 28
    object ImpressionJsonEncodingFailed : Event(
        EventType.WARNING,
        "IMPRESSION_JSON_ENCODING_FAILED",
    )

    // 30
    object ImpressionRatTrackerFailed : Event(
        EventType.WARNING,
        "IMPRESSION_RAT_TRACKER_FAILED",
    )

    // 32
    object UserDataCacheEncodingFailed : Event(
        EventType.WARNING,
        "USERDATA_CACHE_ENCODING_FAILED",
    )

    // 33
    object UserDataCacheDecodingFailed : Event(
        EventType.WARNING,
        "USER_CACHE_DECODING_FAILED",
    )

    // 34
    object ImageDownloadFailed : Event(
        EventType.WARNING,
        "IMAGE_DOWNLOAD_FAILED",
    )

    // 35
    object InvalidTooltip : Event(
        EventType.WARNING,
        "CAMPAIGN_INVALID_TOOLTIP",
    )

    // 26
    object CampaignDisplayCancelled : Event(
        EventType.WARNING,
        "CAMPAIGN_DISPLAY_CANCELLED_BY_APP",
    )

    // 37
    object CampaignInvalidColor : Event(
        EventType.WARNING,
        "CAMPAIGN_INVALID_COLOR",
    )

    // 38
    object CampaignRedirectActionFailed : Event(
        EventType.WARNING,
        "CAMPAIGN_REDIRECT_ACTION_FAILED",
    )
}
