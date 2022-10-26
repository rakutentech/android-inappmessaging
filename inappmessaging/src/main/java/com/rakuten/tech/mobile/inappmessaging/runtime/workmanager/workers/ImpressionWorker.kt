package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.rakuten.tech.mobile.inappmessaging.runtime.api.MessageMixerRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkerUtils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicInteger

/**
 * A background Worker class which handles reporting impressions.
 */
internal class ImpressionWorker(
    context: Context,
    workerParams: WorkerParameters
) :
    Worker(context, workerParams) {

    /**
     * This method makes a thread blocking network call to post impression.
     * If server responding a non-successful response, work will be retried again with exponential backoff.
     */
    override fun doWork(): Result {
        // Retrieve input data.
        val impressionEndpoint = ConfigResponseRepository.instance().getImpressionEndpoint()
        val impressionRequestJsonRequest = inputData.getString(IMPRESSION_REQUEST_KEY)

        // Validate input data.
        if (impressionEndpoint.isEmpty() || impressionRequestJsonRequest.isNullOrEmpty()) {
            return Result.failure()
        }

        return executeRequest(impressionRequestJsonRequest, impressionEndpoint)
    }

    @SuppressWarnings("TooGenericExceptionCaught")
    private fun executeRequest(impressionRequestJsonRequest: String?, impressionEndpoint: String): Result {
        // Convert impressionRequestJsonString to ImpressionRequest object.
        val impressionRequest = try {
            Gson().fromJson(impressionRequestJsonRequest, ImpressionRequest::class.java)
        } catch (e: JsonParseException) {
            InAppLogger(TAG).error(e.message)
            return Result.failure()
        }

        AccountRepository.instance().logWarningForUserInfo(TAG)
        return try {
            // Execute Retrofit API call and handle response.
            onResponse(createReportImpressionCall(impressionEndpoint, impressionRequest).execute())
        } catch (e: Exception) {
            InAppLogger(TAG).debug(e.message)
            Result.retry()
        }
    }

    fun onResponse(response: Response<ResponseBody>): Result {
        InAppLogger(TAG).debug("Impression Response:%d", response.code())

        return when {
            response.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR ->
                WorkerUtils.checkRetry(serverErrorCounter.getAndIncrement()) { Result.retry() }
            response.code() >= HttpURLConnection.HTTP_MULT_CHOICE -> {
                serverErrorCounter.set(0) // reset server error counter
                Result.failure()
            }
            else -> {
                serverErrorCounter.set(0) // reset server error counter
                Result.success()
            }
        }
    }

    /**
     * This method returns a Call of ResponseBody object.
     */
    fun createReportImpressionCall(
        impressionEndpoint: String,
        impressionRequest: ImpressionRequest,
        accountRepo: AccountRepository = AccountRepository.instance()
    ): Call<ResponseBody> =
        // Create a Retrofit API call.
        RuntimeUtil.getRetrofit()
            .create(MessageMixerRetrofitService::class.java)
            .reportImpression(
                subscriptionId = HostAppInfoRepository.instance().getSubscriptionKey(),
                deviceId = HostAppInfoRepository.instance().getDeviceId(),
                accessToken = accountRepo.getAccessToken(),
                impressionUrl = impressionEndpoint,
                impressionRequest = impressionRequest
            )

    companion object {
        const val IMPRESSION_REQUEST_KEY = "impression_request_key"
        private const val TAG = "IAM_ImpressionWorker"
        internal val serverErrorCounter = AtomicInteger(0)
    }
}
