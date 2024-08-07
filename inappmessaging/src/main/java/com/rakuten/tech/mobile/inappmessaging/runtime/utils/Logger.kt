package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.sdkutils.logger.Logger

/**
 * Debug logging that is enabled if host app enables it through manifest metadata.
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
 * Logging that is enabled regardless of build configuration, excluding debug log level.
 * Use info, warn, or error levels to log it in release builds.
 * **Caution**: Log minimal information without any sensitive data.
 */
internal class InAppProdLogger(tag: String): Logger(tag)