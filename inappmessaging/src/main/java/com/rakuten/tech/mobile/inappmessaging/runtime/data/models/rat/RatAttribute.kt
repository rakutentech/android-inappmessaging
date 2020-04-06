package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.rat

import com.google.gson.annotations.SerializedName
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

/**
 * This class contains just name and value attributes.
 * Specially made for broadcasting RAT events.
 */
@SuppressFBWarnings("URF_UNREAD_FIELD")
@SuppressWarnings("PMD")
internal data class RatAttribute(
    @SerializedName("name") private val name: String,
    @SerializedName("value") private val value: Any
)
