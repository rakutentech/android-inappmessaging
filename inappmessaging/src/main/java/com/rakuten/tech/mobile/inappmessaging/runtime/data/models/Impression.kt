package com.rakuten.tech.mobile.inappmessaging.runtime.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType

/**
 * This class represents impression object.
 */
internal data class Impression(
    val impType: ImpressionType,
    @Json(name = "timestamp")
    val timestamp: Long
) {

    @Json(name = "type")
    val type = impType.typeId
}
