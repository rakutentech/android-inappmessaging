package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.api.ConfigRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ConfigQueryParamsBuilder
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkerUtils
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.ConfigScheduler
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import retrofit2.Call
import retrofit2.Response
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicInteger

/**
 * This class contains the actual work to communicate with Config Service. It extends Worker class,
 * and uses synchronized network call to make request to Config Service.
 */
@SuppressWarnings("LongParameterList")
internal class ConfigWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val hostRepo: HostAppInfoRepository,
    private val configRepo: ConfigResponseRepository,
    private val messagePingScheduler: MessageMixerPingScheduler,
    private val configScheduler: ConfigScheduler = ConfigScheduler.instance(),
    private val retryUtil: RetryDelayUtil = RetryDelayUtil
) :
    Worker(context, workerParams) {

    /**
     * Overload constructor to handle OneTimeWorkRequest.Builder().
     */
    constructor(context: Context, workerParams: WorkerParameters) :
        this(
            context, workerParams, HostAppInfoRepository.instance(), ConfigResponseRepository.instance(),
            MessageMixerPingScheduler.instance()
        )

    /**
     * Main method to do the work. Make Config Service network call is the main work.
     * Retries sending the request with default backoff when network error is encountered.
     */
    @SuppressWarnings("TooGenericExceptionCaught")
    override fun doWork(): Result {
        InAppLogger(TAG).debug(hostRepo.getConfigUrl())
        val hostAppId = hostRepo.getPackageName()
        val hostAppVersion = hostRepo.getVersion()
        val subscriptionId = hostRepo.getSubscriptionKey()
        // Terminate request if any of the following values are empty: appId, appVersion or subscription key.
        if (hostAppId.isEmpty() || hostAppVersion.isEmpty() || subscriptionId.isEmpty()) {
            return Result.failure()
        }

        return try {
            // Executing the API network call.
            onResponse(setupCall(hostAppId, hostAppVersion, subscriptionId).execute())
        } catch (e: Exception) {
            InAppLogger(TAG).error(e.message)
            // RETRY by default has exponential backoff baked in.
            Result.retry()
        }
    }

    private fun setupCall(hostAppId: String, hostAppVersion: String, subscriptionId: String): Call<ConfigResponse> {
        val locale = hostRepo.getDeviceLocale()
        val configUrl = hostRepo.getConfigUrl()
        val sdkVersion = BuildConfig.VERSION_NAME
        val params = ConfigQueryParamsBuilder(
            appId = hostAppId,
            locale = locale,
            appVersion = hostAppVersion,
            sdkVersion = sdkVersion
        ).queryParams
        return RuntimeUtil.getRetrofit()
            .create(ConfigRetrofitService::class.java)
            .getConfigService(configUrl, subscriptionId, params)
    }

    /**
     * This method handles the response from Config Service.
     * if response code is 429 -> retries sending of request with random exponential back off
     * if response code is 5xx -> retries at most 3 times with random exponential back off
     * else -> returns failure
     */
    @VisibleForTesting
    @Throws(IllegalArgumentException::class)
    fun onResponse(response: Response<ConfigResponse?>): Result {
        if (response.isSuccessful && response.body() != null) {
            handleResponse(response)
        } else return when {
            response.code() == RetryDelayUtil.RETRY_ERROR_CODE -> handleRetry(response)
            response.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> handleInternalError(response)
            else -> {
                serverErrorCounter.set(0) // reset server error counter
                // clear temp data (ignore all temp data stored during config request)
                InAppMessaging.setNotConfiguredInstance()
                WorkerUtils.logRequestError(TAG, response.code(), response.errorBody()?.string())
                Result.failure()
            }
        }
        return Result.success()
    }

    private fun handleInternalError(response: Response<ConfigResponse?>): Result {
        WorkerUtils.logRequestError(TAG, response.code(), response.errorBody()?.string())
        return WorkerUtils.checkRetry(serverErrorCounter.getAndIncrement()) { retryConfigRequest() }
    }

    private fun handleRetry(response: Response<ConfigResponse?>): Result {
        serverErrorCounter.set(0) // reset server error counter
        WorkerUtils.logSilentRequestError(TAG, response.code(), response.errorBody()?.string())
        return retryConfigRequest()
    }

    private fun handleResponse(response: Response<ConfigResponse?>) {
        serverErrorCounter.set(0) // reset server error counter
        // Adding config data to its repo.
        configRepo.addConfigResponse(response.body()?.data)
        // Schedule a ping request to message mixer. Initial delay is 0
        // reset current delay to initial
        ConfigScheduler.currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
        InAppLogger(TAG).debug(
            "Config Response: %d (%b)",
            response.body()?.data?.rollOutPercentage, configRepo.isConfigEnabled()
        )
        if (configRepo.isConfigEnabled()) {
            MessageMixerPingScheduler.currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
            messagePingScheduler.pingMessageMixerService(0)
        } else {
            // reset IAM instance which will clear temp data
            InAppMessaging.setNotConfiguredInstance()
        }
    }

    private fun retryConfigRequest(): Result {
        configScheduler.startConfig(ConfigScheduler.currDelay)
        ConfigScheduler.currDelay = retryUtil.getNextDelay(ConfigScheduler.currDelay)
        // set previous worker as success to avoid logging
        return Result.success()
    }

    companion object {
        private const val TAG = "IAM_ConfigWorker"
        internal val serverErrorCounter = AtomicInteger(0)
    }
}
