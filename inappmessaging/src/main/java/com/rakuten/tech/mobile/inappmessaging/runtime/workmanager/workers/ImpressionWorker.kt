package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.rakuten.tech.mobile.inappmessaging.runtime.api.MessageMixerRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import okhttp3.ResponseBody
import retrofit2.Call
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection

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
    @SuppressWarnings("LongMethod", "ReturnCount")
    override fun doWork(): Result {
        // Retrieve input data.
        val impressionEndpoint = ConfigResponseRepository.instance().getImpressionEndpoint()
        val impressionRequestJsonRequest = inputData.getString(IMPRESSION_REQUEST_KEY)

        // Validate input data.
        if (impressionEndpoint.isEmpty() || impressionRequestJsonRequest.isNullOrEmpty()) {
            return Result.failure()
        }

        // Convert impressionRequestJsonString to ImpressionRequest object.
        val impressionRequest = try {
            Gson().fromJson(impressionRequestJsonRequest, ImpressionRequest::class.java)
        } catch (e: JsonSyntaxException) {
            Timber.tag(TAG).e(e)
            return Result.failure()
        }

        val impressionServiceCall = createReportImpressionCall(impressionEndpoint, impressionRequest)
        try {
            // Execute Retrofit API call and handle response.
            val response = impressionServiceCall.execute()
            Timber.tag(TAG).d("Impression Response:%d", response.code())

            // Only RETRY with exponential backoff if response code above 500, it might be server busy
            // establishing connection to database.
            if (response.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
                return Result.retry()
            } else if (response.code() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                return Result.failure()
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
            return Result.retry()
        }
        return Result.success()
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
                            HostAppInfoRepository.instance().getInAppMessagingSubscriptionKey().toString(),
                            HostAppInfoRepository.instance().getDeviceId().toString(),
                            accountRepo.getRaeToken(),
                            impressionEndpoint,
                            impressionRequest)

    companion object {
        const val IMPRESSION_REQUEST_KEY = "impression_request_key"
        private const val TAG = "IAM_ImpressionWorker"
    }
}
