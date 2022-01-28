package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rakuten.tech.mobile.inappmessaging.runtime.api.MessageMixerRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalEventRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.PingResponseMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.PingRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageMixerResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkerUtils
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import retrofit2.Call
import retrofit2.Response
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList

/**
 * This class contains the actual work to communicate with Message Mixer Service. It extends Worker
 * class, and uses synchronized network call to make request to Message Mixer Service.
 */
@SuppressWarnings("TooGenericExceptionCaught")
internal class MessageMixerWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val eventMessageScheduler: EventMessageReconciliationScheduler,
    private val messageMixerScheduler: MessageMixerPingScheduler,
    private val retryUtil: RetryDelayUtil = RetryDelayUtil
) :
    Worker(context, workerParams) {

    /**
     * Overload constructor to handle OneTimeWorkRequest.Builder().
     */
    constructor(context: Context, workerParams: WorkerParameters) :
            this(context, workerParams, EventMessageReconciliationScheduler.instance(),
                    MessageMixerPingScheduler.instance())

    @VisibleForTesting
    internal var responseCall: Call<MessageMixerResponse>? = null

    /**
     * Main method to do the work. Make Message Mixer network call is the main work.
     * Retries sending the request with default backoff when network error is encountered.
     */
    override fun doWork(): Result {
        // Create a retrofit API.
        val serviceApi: MessageMixerRetrofitService =
                RuntimeUtil.getRetrofit().create(MessageMixerRetrofitService::class.java)

        // Create an pingRequest for the API.
        val pingRequest = PingRequest(HostAppInfoRepository.instance().getVersion(), RuntimeUtil.getUserIdentifiers())

        // Create an retrofit API network call.
        responseCall = serviceApi.performPing(
                HostAppInfoRepository.instance().getInAppMessagingSubscriptionKey(),
                AccountRepository.instance().getAccessToken(),
                HostAppInfoRepository.instance().getDeviceId(),
                ConfigResponseRepository.instance().getPingEndpoint(),
                pingRequest)
        AccountRepository.instance().logWarningForUserInfo(TAG)
        return try {
            // Execute a thread blocking API network call, and handle response.
            onResponse(responseCall!!.execute())
        } catch (e: Exception) {
            Logger(TAG).error(e.message)
            Result.retry()
        }
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
            val messageMixerResponse = response.body()
            if (messageMixerResponse != null) {
                // Add time data.
                PingResponseMessageRepository.instance().lastPingMillis = messageMixerResponse.currentPingMillis

                // Parse all data in response.
                val parsedMessages = parsePingResponseWithTestMessage(messageMixerResponse)

                // Add all parsed messages into PingResponseMessageRepository.
                PingResponseMessageRepository.instance().replaceAllMessages(parsedMessages)

                // Clear non-persistent local events triggered before current ping
                LocalEventRepository.instance().clearNonPersistentEvents(messageMixerResponse.currentPingMillis)

                // Start a new MessageEventReconciliationWorker, there was a new Ping Response to parse.
                // This worker will attempt to cancel message scheduled but hasn't been displayed yet
                // because there could be an edge case where the message is obsolete.
                eventMessageScheduler.startEventMessageReconciliationWorker()

                // Schedule next ping.
                scheduleNextPing(messageMixerResponse.nextPingMillis)
                Logger(TAG).debug("campaign size: %d", messageMixerResponse.data.size)
            }
        } else return when {
            response.code() == RetryDelayUtil.RETRY_ERROR_CODE -> {
                serverErrorCounter.set(0) // reset server error counter
                WorkerUtils.logSilentRequestError(TAG, response.code(), response.errorBody()?.string())
                retryPingRequest()
            }
            response.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                WorkerUtils.logRequestError(TAG, response.code(), response.errorBody()?.string())
                WorkerUtils.checkRetry(serverErrorCounter.getAndIncrement()) { retryPingRequest() }
            }
            else -> {
                serverErrorCounter.set(0) // reset server error counter
                WorkerUtils.logRequestError(TAG, response.code(), response.errorBody()?.string())
                Result.failure()
            }
        }
        return Result.success()
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
        Logger(TAG).debug("Next ping scheduled in: %d", nextPingMillis)
    }

    /**
     * This method parses response data, and returns a list of parsed messages.
     */
    @SuppressWarnings("FunctionMaxLength")
    private fun parsePingResponseWithTestMessage(response: MessageMixerResponse): List<Message> {
        val parsedMessages = ArrayList<Message>()
        for (data in response.data) {
            val message: Message = data.campaignData
            parsedMessages.add(message)
        }
        return parsedMessages
    }

    companion object {
        private const val TAG = "IAM_MessageMixerWorker"
        internal val serverErrorCounter = AtomicInteger(0)
    }
}
