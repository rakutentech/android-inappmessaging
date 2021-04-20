package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.api.ConfigRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ConfigRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection

/**
 * This class contains the actual work to communicate with Config Service. It extends Worker class,
 * and uses synchronized network call to make request to Config Service.
 */
internal class ConfigWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val hostRepo: HostAppInfoRepository,
    private val configRepo: ConfigResponseRepository,
    private val messagePingScheduler: MessageMixerPingScheduler
) :
    Worker(context, workerParams) {

    /**
     * Overload constructor to handle OneTimeWorkRequest.Builder().
     */
    constructor(context: Context, workerParams: WorkerParameters) :
            this(context, workerParams, HostAppInfoRepository.instance(), ConfigResponseRepository.instance(),
                MessageMixerPingScheduler.instance())

    /**
     * Main method to do the work. Make Config Service network call is the main work. Regardless of
     * the response(200/400) returned from Config Service, `WorkerResult.Success` should always be
     * returned. Because this work(network call to config service) was scheduled and executed.
     */
    @Suppress("LongMethod")
    override fun doWork(): Result {
        Timber.tag(TAG).d(hostRepo.getConfigUrl())
        val hostAppId = hostRepo.getPackageName()
        val locale = hostRepo.getDeviceLocale()
        val hostAppVersion = hostRepo.getVersion()
        // Terminate request if either appId or appVersion is empty or null.
        if (hostAppId.isNullOrEmpty() || hostAppVersion.isNullOrEmpty()) {
            return Result.failure()
        }

        val configUrl = hostRepo.getConfigUrl() ?: ""
        val sdkVersion = BuildConfig.VERSION_NAME
        val configServiceCall = RuntimeUtil.getRetrofit()
                .create(ConfigRetrofitService::class.java)
                .getConfigService(
                        configUrl,
                        ConfigRequest(hostAppId, locale, hostAppVersion, sdkVersion))

        return try {
            // Executing the API network call.
            onResponse(configServiceCall.execute())
        } catch (e: IOException) {
            Timber.tag(TAG).d(e)
            // RETRY by default has exponential backoff baked in.
            Result.retry()
        }
    }

    /**
     * This method handles the response from Config Service. Returns RETRY if response code is 500
     * because server could be busy for the moment. Returns FAILURE if response code is 400.
     */
    @VisibleForTesting
    @Throws(IllegalArgumentException::class)
    fun onResponse(response: Response<ConfigResponse?>): Result {
        if (response.isSuccessful && response.body() != null) {
            // Adding config data to its repo.
            configRepo.addConfigResponse(response.body()?.data)
            // Schedule a ping request to message mixer. Initial delay is 0
            messagePingScheduler.pingMessageMixerService(0)
            Timber.tag(TAG).d("Config Response: %b", response.body()?.data?.enabled)
        } else return if (response.code() == InAppMessagingConstants.RETRY_ERROR_CODE ||
                response.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            // Retry with exponential backoff if server has error.
            Result.retry()
        } else {
            Result.failure()
        }
        return Result.success()
    }

    companion object {
        private const val TAG = "IAM_ConfigWorker"
    }
}
