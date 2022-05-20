package com.rakuten.tech.mobile.inappmessaging.runtime.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType

/**
 * This class represents impression object.
 */
@JsonClass(generateAdapter = true)
internal data class Impression(
    private val impType: ImpressionType,
    @Json(name = "timestamp")
    val timestamp: Long
) {

    @Json(name = "type")
    val type = impType.typeId
}
