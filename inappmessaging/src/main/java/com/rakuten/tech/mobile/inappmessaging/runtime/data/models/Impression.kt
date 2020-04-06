package com.rakuten.tech.mobile.inappmessaging.runtime.data.models

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

/**
 * This class represents impression object.
 */
internal data class Impression(
    private val impType: ImpressionType,
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    @SerializedName("timestamp")
    val timestamp: Long
) {

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    @SerializedName("type")
    val type = impType.typeId
}
