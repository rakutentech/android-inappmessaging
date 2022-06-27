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
