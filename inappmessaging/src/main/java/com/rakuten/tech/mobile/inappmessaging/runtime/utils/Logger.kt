package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.sdkutils.logger.Logger

/**
 * Will log to console if host app enables debug logging.
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
 * Will log to console regardless of build configuration.
 * **Caution**: Log minimal information without any sensitive data.
 */
internal class InAppProdLogger(tag: String): Logger(tag) {
    init {
        this.setDebug(false)
    }
}