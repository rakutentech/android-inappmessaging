package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.sdkutils.logger.Logger

internal class InAppLogger(tag: String) : Logger(tag) {
    init {
        this.setDebug(isDebug)
    }

    companion object {
        internal var isDebug = false
    }
}

/**
 * Logger that will log to console regardless of build configuration.
 * **Caution**: Log minimal information without any sensitive data.
 */
internal class InAppProdLogger(tag: String): Logger(tag) {
    init {
        this.setDebug(false)
    }
}