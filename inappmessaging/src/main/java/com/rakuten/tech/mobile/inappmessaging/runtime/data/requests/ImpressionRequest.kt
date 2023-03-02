package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Impression
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.UserIdentifier

/**
 * This class represents the request body for impression.
 */
internal data class ImpressionRequest(
    @SerializedName("campaignId")
    private val campaignId: String?,
    @SerializedName("isTest")
    private val isTest: Boolean,
    @SerializedName("appVersion")
    private val appVersion: String?,
    @SerializedName("sdkVersion")
    private val sdkVersion: String?,
    @SerializedName("userIdentifiers")
    private val userIdentifiers: List<UserIdentifier>,
    @SerializedName("impressions")
    val impressions: List<Impression>,
)
