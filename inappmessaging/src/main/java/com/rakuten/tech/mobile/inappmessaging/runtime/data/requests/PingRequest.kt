package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.UserIdentifier
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

/**
 * This class represents the request body for ping request.
 */
@SuppressFBWarnings("URF_UNREAD_FIELD")
internal data class PingRequest(
    @SerializedName("appVersion")
    private val appVersion: String?, // NOPMD
    @SerializedName("userIdentifiers")
    private val userIdentifiers: MutableList<UserIdentifier>? // NOPMD
)
