package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import androidx.work.ListenableWorker
import timber.log.Timber

internal object WorkerUtils {
    fun logRequestError(tag: String, code: Int, message: String?) {
        Timber.tag(tag).e("Response Code $code: ${message ?: "no error message"}")
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
