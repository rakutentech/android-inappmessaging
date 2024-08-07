package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.sdkutils.logger.Logger

/**
 * Logging enabled if host app enables it through manifest metadata.
 */
internal class InAppLogger(tag: String) : Logger(tag) {
    init {
        this.setDebug(isDebug)
    }

    companion object {
        internal var isDebug = false
    }
}

/**
 * Logging enabled regardless of build configuration. For debug builds, all log levels are logged. For release builds,
 * only info, warn, and error levels are logged.
 * **Caution**: Log minimal information without any sensitive data.
 */
internal class InAppProdLogger(tag: String): Logger(tag)