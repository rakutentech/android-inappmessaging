package com.rakuten.tech.mobile.inappmessaging.runtime.data.models

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType

/**
 * This class represents impression object.
 */
internal data class Impression(
    private val impType: ImpressionType,
    @SerializedName("timestamp")
    val timestamp: Long,
) {

    @SerializedName("type")
    val type = impType.typeId
}
