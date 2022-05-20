package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Impression
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.UserIdentifier

/**
 * This class represents the request body for impression.
 */
@JsonClass(generateAdapter = true)
internal data class ImpressionRequest(
    @Json(name = "campaignId")
    private val campaignId: String?,
    @Json(name = "isTest")
    private val isTest: Boolean,
    @Json(name = "appVersion")
    private val appVersion: String?,
    @Json(name = "sdkVersion")
    private val sdkVersion: String?,
    @Json(name = "userIdentifiers")
    private val userIdentifiers: List<UserIdentifier>,
    @Json(name = "impressions")
    val impressions: List<Impression>
)
