package com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger

import android.content.Context
import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.InApp
import com.rakuten.tech.mobile.sdkutils.eventlogger.EventLogger

internal interface EventLogger {

    fun configure(context: Context, enable: Boolean? = null, apiUrl: String? = null, apiKey: String? = null)

    fun logEvent(event: Event)
}

internal object InAppEventLogger : com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.EventLogger {

    private var isEnabled: Boolean = true

    override fun configure(context: Context, enable: Boolean?, apiUrl: String?, apiKey: String?) {
        val appManifestConfig = InApp.AppManifestConfig(context)

        isEnabled = enable ?: appManifestConfig.enableEventLogger()

        if (!isEnabled) {
            return
        }

        val realApiUrl = apiUrl ?: appManifestConfig.eventLoggerApiUrl().orEmpty()
        val realApiKey = apiKey ?: appManifestConfig.eventLoggerApiKey().orEmpty()

        if (realApiUrl.isEmpty() || realApiKey.isEmpty()) {
            isEnabled = false
            return
        }

        EventLogger.configure(context, realApiUrl, realApiKey)
    }

    override fun logEvent(event: Event) {
        if (!isEnabled) {
            return
        }

        when (event.type) {
            EventType.CRITICAL ->
                EventLogger.sendCriticalEvent(
                    sourceName = "iam", sourceVersion = BuildConfig.VERSION_NAME,
                    errorCode = event.code, errorMessage = event.message, info = event.info,
                )
            EventType.WARNING ->
                EventLogger.sendWarningEvent(
                    sourceName = "iam", sourceVersion = BuildConfig.VERSION_NAME,
                    errorCode = event.code, errorMessage = event.message, info = event.info,
                )
        }
    }
}
