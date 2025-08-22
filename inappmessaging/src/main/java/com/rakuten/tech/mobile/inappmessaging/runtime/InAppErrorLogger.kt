package com.rakuten.tech.mobile.inappmessaging.runtime

import android.util.Log
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.InAppEventLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger

/**
 * @property message Descriptive error message.
 * @property ex Non-fatal exception (caught exception).
 * @property ev Event for the Event Logger platform.
 * @property meta Any metadata for this error.
 */
internal data class InAppError(
    var message: String? = null,
    val ex: Exception? = null,
    val ev: Event? = null,
    val meta: Map<String, String>? = null,
)

/**
 * Utility class to log or report errors to different destinations.
 * - Logcat
 * - Callback for host app
 * - Remote logger (Event Logger platform)
 */
internal object InAppErrorLogger {

    fun logError(tag: String, error: InAppError) {
        val errorMessage = error.message ?: "Unexpected error"
        val exceptionCause = (error.ex?.cause ?: error.ex?.message)?.let { ", Cause: $it" }.orEmpty()
        val fullMessage = "$errorMessage$exceptionCause"

        if (InAppLogger.isDebug) {
            // To avoid initializing InAppLogger everytime based on tag, we will use the default logging utility
            Log.e(tag, "$fullMessage ${error.meta.orEmpty()}", error.ex)
        }

        error.ex?.let { ex ->
            InAppMessaging.errorCallback?.invoke(ex)
        }

        error.ev?.let { inAppEvent ->
            inAppEvent.message = fullMessage
            inAppEvent.info["tag"] = tag
            if (error.meta != null) {
                inAppEvent.info.putAll(error.meta)
            }

            InAppEventLogger.logEvent(inAppEvent)
        }
    }
}
