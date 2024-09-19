package com.rakuten.tech.mobile.inappmessaging.runtime.extensions

import android.net.Uri
import android.util.Patterns

internal fun String?.isValidUrl(): Boolean {
    return !this.isNullOrEmpty() && (this.isValidWebUrl() || this.isValidDeeplinkUrl())
}

private fun String.isValidWebUrl(): Boolean {
    return Patterns.WEB_URL.matcher(this).matches()
}

private fun String.isValidDeeplinkUrl(): Boolean {
    val uri = Uri.parse(this)
    return uri.scheme != null && uri.host != null
}