package com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger

/**
 * https:<company-confluence-link>/display/MAGS/IAM+Event+Logger%3A+SDK+Events.
 */
internal sealed class Event(
    val type: EventType,
    open val code: String,
    open var message: String = "",
    open val info: MutableMap<String, String> = mutableMapOf(),
) {
    // May be used for API request errors.
    data class ApiRequestFailed(
        val api: BackendApi,
        override val code: String,
        val severity: EventType = EventType.CRITICAL,
    ) : Event(
        severity,
        code,
    )

    // May be used for generic errors in the SDK (such as a caught exception).
    data class OperationFailed(
        val tag: String,
        val severity: EventType = EventType.WARNING,
    ) : Event(
        severity,
        "${tag}_FAILED",
    )

    data class JsonDecodingFailed(
        val tag: String,
        val severity: EventType = EventType.CRITICAL,
    ) : Event(
        severity,
        "${tag}_JSON_DECODING_ERROR",
    )

    data class InvalidConfiguration(
        val tag: String,
    ) : Event(
        EventType.CRITICAL,
        "${tag}_INVALID_CONFIGURATION",
    )

    object ImpressionRatTrackerFailed : Event(
        EventType.WARNING,
        "IMPRESSION_RAT_TRACKER_FAILED",
    )

    object UserDataCacheDecodingFailed : Event(
        EventType.WARNING,
        "USER_CACHE_DECODING_FAILED",
    )

    data class ImageLoadFailed(
        val url: String?,
    ) : Event(
        EventType.WARNING,
        "IMAGE_LOAD_FAILED",
        "Image load failed",
        info = mutableMapOf("url" to url.orEmpty()),
    )

    object InvalidTooltip : Event(
        EventType.WARNING,
        "CAMPAIGN_INVALID_TOOLTIP",
    )

    data class CampaignRedirectActionFailed(
        val url: String?,
    ) : Event(
        EventType.WARNING,
        "CAMPAIGN_REDIRECT_ACTION_FAILED",
        "Redirect failed",
        info = mutableMapOf("url" to url.orEmpty()),
    )
}
