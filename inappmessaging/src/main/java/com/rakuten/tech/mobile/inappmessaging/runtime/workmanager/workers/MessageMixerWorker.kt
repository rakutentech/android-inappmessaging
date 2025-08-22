package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.stream.MalformedJsonException
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppError
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppErrorLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.api.MessageMixerRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.CampaignType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.PingRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageMixerResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.BackendApi
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkerUtils
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.EventMatchingUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import retrofit2.Call
import retrofit2.Response
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList

/**
 * This class contains the actual work to communicate with Message Mixer Service. It extends Worker
 * class, and uses synchronized network call to make request to Message Mixer Service.
 */
internal class MessageMixerWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val eventMessageScheduler: EventMessageReconciliationScheduler,
    private val messageMixerScheduler: MessageMixerPingScheduler,
    private val retryUtil: RetryDelayUtil = RetryDelayUtil,
) :
    Worker(context, workerParams) {

    @VisibleForTesting
    internal var testResponse: Call<MessageMixerResponse>? = null

    /**
     * Overload constructor to handle OneTimeWorkRequest.Builder().
     */
    constructor(context: Context, workerParams: WorkerParameters) :
        this(
            context, workerParams, EventMessageReconciliationScheduler.instance(),
            MessageMixerPingScheduler.instance(),
        )

    /**
     * Main method to do the work. Make Message Mixer network call is the main work.
     * Retries sending the request with default backoff when network error is encountered.
     */
    @SuppressWarnings("TooGenericExceptionCaught", "LongMethod", "ReturnCount")
    override fun doWork(): Result {
        val configRepo = ConfigResponseRepository.instance()
        if (!configRepo.isConfigEnabled()) {
            return Result.failure()
        }

        if (configRepo.getPingEndpoint().isBlank()) {
            InAppErrorLogger.logError(
                TAG,
                InAppError("Invalid ping URL", ev = Event.InvalidConfiguration(BackendApi.PING.name)),
            )
            return Result.failure()
        }

        val call = setupCall()

        // for testing
        testResponse = call
        AccountRepository.instance().logWarningForUserInfo(TAG)
        return try {
            // Execute a thread blocking API network call, and handle response.
            onResponse(call.execute())
        } catch (mje: MalformedJsonException) {
            InAppErrorLogger.logError(TAG, InAppError(ex = mje, ev = Event.JsonDecodingFailed(BackendApi.PING.name)))
            Result.failure()
        } catch (e: Exception) {
            InAppLogger(TAG).error("ping - error: ${e.message}")
            Result.retry()
        }
    }

    private fun setupCall(): Call<MessageMixerResponse> {
        // Create a retrofit API.
        val serviceApi = RuntimeUtil.getRetrofit().create(MessageMixerRetrofitService::class.java)

        // Create an pingRequest for the API.
        val pingRequest = PingRequest(
            appVersion = HostAppInfoRepository.instance().getVersion(),
            userIdentifiers = RuntimeUtil.getUserIdentifiers(),
            supportedTypes = getSupportedCampaign(),
            rmcSdkVersion = HostAppInfoRepository.instance().getRmcSdkVersion(),
        )

        // Create an retrofit API network call.
        return serviceApi.performPing(
            subscriptionId = HostAppInfoRepository.instance().getSubscriptionKey(),
            accessToken = AccountRepository.instance().getAccessToken(),
            deviceId = HostAppInfoRepository.instance().getDeviceId(),
            url = ConfigResponseRepository.instance().getPingEndpoint(),
            requestBody = pingRequest,
        )
    }

    /**
     * This method handles the response from Config Service.
     * if response code is 429 -> retries sending of request with random exponential back off
     * if response code is 5xx -> retries at most 3 times with random exponential back off
     * else -> returns failure
     */
    @VisibleForTesting
    @SuppressWarnings("LongMethod")
    fun onResponse(response: Response<MessageMixerResponse>): Result {
        if (response.isSuccessful) {
            serverErrorCounter.set(0) // reset server error counter
            response.body()?.let { handleResponse(it) }
        } else {
            return when {
                response.code() == RetryDelayUtil.RETRY_ERROR_CODE -> handleRetry(response)
                response.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> handleInternalError(response)
                else -> {
                    serverErrorCounter.set(0) // reset server error counter
                    InAppErrorLogger.logError(
                        TAG,
                        InAppError(
                            "${BackendApi.PING.alias} API failed - ${response.errorBody()?.string()}",
                            ev = Event.ApiRequestFailed(BackendApi.PING, response.code().toString()),
                        ),
                    )
                    Result.failure()
                }
            }
        }
        return Result.success()
    }

    private fun handleInternalError(response: Response<MessageMixerResponse>): Result {
        WorkerUtils.logRequestError(TAG, response.code(), response.errorBody()?.string())
        return WorkerUtils.checkRetry(serverErrorCounter.getAndIncrement(), BackendApi.PING, response) {
            retryPingRequest()
        }
    }

    private fun handleRetry(response: Response<MessageMixerResponse>): Result {
        serverErrorCounter.set(0) // reset server error counter
        WorkerUtils.logSilentRequestError(TAG, response.code(), response.errorBody()?.string())
        return retryPingRequest()
    }

    private fun handleResponse(messageMixerResponse: MessageMixerResponse) {
        InAppLogger(TAG).info("ping API success - campaigns: ${messageMixerResponse.data.size}")

        // Parse all data in response.
        val parsedMessages = parsePingRespTestMessage(messageMixerResponse)

        // Add all parsed messages into CampaignRepository.
        CampaignRepository.instance().syncWith(
            parsedMessages, messageMixerResponse.currentPingMillis,
            ignoreTooltips = !HostAppInfoRepository.instance().isTooltipFeatureEnabled(),
        )

        // Match&Store any temp events using lately synced campaigns.
        EventMatchingUtil.instance().flushEventBuffer()

        // Start a new MessageEventReconciliationWorker, there was a new Ping Response to parse.
        // This worker will attempt to cancel message scheduled but hasn't been displayed yet
        // because there could be an edge case where the message is obsolete.
        eventMessageScheduler.startReconciliationWorker()

        // Schedule next ping.
        scheduleNextPing(messageMixerResponse.nextPingMillis)
    }

    private fun retryPingRequest(): Result {
        messageMixerScheduler.pingMessageMixerService(MessageMixerPingScheduler.currDelay)
        MessageMixerPingScheduler.currDelay = retryUtil.getNextDelay(MessageMixerPingScheduler.currDelay)
        // set previous worker as success to avoid logging
        return Result.success()
    }

    /**
     * This method schedules the next ping to Message Mixer. Due to JobScheduler's limitation,
     * WorkRequest backoff time has to be between 10 seconds to 5 hours.
     */
    private fun scheduleNextPing(nextPingMillis: Long) {
        // reset current delay to initial
        MessageMixerPingScheduler.currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
        messageMixerScheduler.pingMessageMixerService(nextPingMillis)
        InAppLogger(TAG).debug("next ping scheduled in: $nextPingMillis")
    }

    /**
     * This method parses response data, and returns a list of parsed messages.
     */
    private fun parsePingRespTestMessage(response: MessageMixerResponse): List<Message> {
        val parsedMessages = ArrayList<Message>()
        for (data in response.data) {
            val message: Message = data.campaignData
            message.getTooltipConfig()
            parsedMessages.add(message)
        }
        return parsedMessages
    }

    @VisibleForTesting
    internal fun getSupportedCampaign(checker: BuildVersionChecker = BuildVersionChecker): ArrayList<Int> {
        val list = arrayListOf(CampaignType.REGULAR.typeId)
        if (checker.isAndroidTAndAbove()) {
            list.add(CampaignType.PUSH_PRIMER.typeId)
        }

        return list
    }

    companion object {
        private const val TAG = "IAM_MessageMixerWorker"
        internal val serverErrorCounter = AtomicInteger(0)
    }
}
