package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.rat

/**
 * This class contains just name and value attributes.
 * Specially made for broadcasting RAT events.
 */
internal data class RatAttribute(
    private val name: String,
    private val value: Any,
)
