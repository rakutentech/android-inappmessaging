package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.sdkutils.logger.Logger

/**
 * Error, warn, and info logs are logged regardless of build configuration as per standard:
 * https://source.android.com/docs/core/tests/debug/understanding-logging#log-level-guidelines
 *
 * Debug logs are logged only if host app enables it through manifest metadata.
 */
internal class InAppLogger(tag: String) : Logger(tag) {
    init {
        this.setDebug(isDebug)
    }

    companion object {
        internal var isDebug = false
    }
}
