package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import androidx.work.ListenableWorker
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException

internal object WorkerUtils {
    private const val MAX_RETRY = 3

    fun logRequestError(tag: String, code: Int, message: String?) {
        val errMsg = "Response Code $code: ${message ?: "no error message"}"
        InAppLogger(tag).error(errMsg)
        InAppMessaging.errorCallback?.let {
            it(InAppMessagingException("$tag: $errMsg"))
        }
    }

    fun logSilentRequestError(tag: String, code: Int, message: String?) {
        InAppLogger(tag).debug("response Code $code: ${message ?: "no error message"}")
    }

    fun checkRetry(counter: Int, retryFunc: () -> ListenableWorker.Result): ListenableWorker.Result {
        return if (counter < MAX_RETRY) {
            retryFunc.invoke()
        } else {
            ListenableWorker.Result.failure()
        }
    }
}
