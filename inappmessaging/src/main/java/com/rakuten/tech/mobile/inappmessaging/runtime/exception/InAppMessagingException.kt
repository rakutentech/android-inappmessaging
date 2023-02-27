package com.rakuten.tech.mobile.inappmessaging.runtime.exception

/**
 * Custom exception of In-App Messaging.
 */
class InAppMessagingException(
    name: String,
    cause: Throwable? = null,
) : RuntimeException(name, cause)
