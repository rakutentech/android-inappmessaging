package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import androidx.work.ListenableWorker
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppError
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppErrorLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.BackendApi
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import retrofit2.Response

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

    fun checkRetry(
        counter: Int,
        api: BackendApi,
        response: Response<*>,
        retryFunc: () -> ListenableWorker.Result,
    ): ListenableWorker.Result {
        return if (counter < MAX_RETRY) {
            retryFunc.invoke()
        } else {
            "${api.alias} API failed - ${response.errorBody()?.string()}".let {
                InAppErrorLogger.logError(
                    "WorkerUtils",
                    InAppError(
                        it, ex = InAppMessagingException(it),
                        ev = Event.ApiRequestFailed(api, "${response.code()}"),
                    ),
                )
            }
            ListenableWorker.Result.failure()
        }
    }
}
