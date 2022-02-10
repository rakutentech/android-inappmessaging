package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import androidx.work.ListenableWorker
import com.rakuten.tech.mobile.inappmessaging.runtime.InApp
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.sdkutils.logger.Logger

internal object WorkerUtils {
    fun logRequestError(tag: String, code: Int, message: String?) {
        val errMsg = "Response Code $code: ${message ?: "no error message"}"
        Logger(tag).error(errMsg)
        InApp.errorCallback?.let {
            it(InAppMessagingException("$tag: $errMsg"))
        }
    }

    fun logSilentRequestError(tag: String, code: Int, message: String?) {
        Logger(tag).debug("Response Code $code: ${message ?: "no error message"}")
    }

    fun checkRetry(counter: Int, retryFunc: () -> ListenableWorker.Result): ListenableWorker.Result {
        return if (counter < MAX_RETRY) {
            retryFunc.invoke()
        } else {
            ListenableWorker.Result.failure()
        }
    }

    private const val MAX_RETRY = 3
}
