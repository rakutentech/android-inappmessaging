package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.rat

import com.google.gson.annotations.SerializedName

/**
 * This class contains just name and value attributes.
 * Specially made for broadcasting RAT events.
 */
@SuppressWarnings("PMD")
internal data class RatAttribute(
    @SerializedName("name") private val name: String,
    @SerializedName("value") private val value: Any
)
