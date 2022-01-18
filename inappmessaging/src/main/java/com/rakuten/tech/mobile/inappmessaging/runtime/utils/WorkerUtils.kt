package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import androidx.work.ListenableWorker
import com.rakuten.tech.mobile.inappmessaging.runtime.InApp
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import timber.log.Timber

internal object WorkerUtils {
    fun logRequestError(tag: String, code: Int, message: String?) {
        val errMsg = "Response Code $code: ${message ?: "no error message"}"
        Timber.tag(tag).e(errMsg)
        InApp.errorCallback?.let {
            it(InAppMessagingException("$tag: $errMsg"))
        }
    }

    fun logSilentRequestError(tag: String, code: Int, message: String?) {
        Timber.tag(tag).d("Response Code $code: ${message ?: "no error message"}")
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
