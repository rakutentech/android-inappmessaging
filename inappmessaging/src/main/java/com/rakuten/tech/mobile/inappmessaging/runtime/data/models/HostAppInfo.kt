package com.rakuten.tech.mobile.inappmessaging.runtime.data.models

import java.util.Locale

/**
 * This class represents host app information.
 */
internal data class HostAppInfo(
    internal val packageName: String? = null,
    internal val deviceId: String? = null,
    internal val version: String? = null,
    internal val subscriptionKey: String? = null,
    internal val locale: Locale? = null,
    internal val configUrl: String? = null
)
