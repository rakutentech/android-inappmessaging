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
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.PingResponseMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.PingRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageMixerResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection

/**
 * This class contains the actual work to communicate with Message Mixer Service. It extends Worker
 * class, and uses synchronized network call to make request to Message Mixer Service. Note: If
 * return RETRY, WorkerManager will make exponential backoff retries by itself.
 */
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

    /**
     * This is the main method to do the work. Making Message Mixer network call is the main work.
     * If response is successful(200-300) from the Service, `WorkerResult.Success` should always be returned.
     * If response was not successful(4xx), RETRY will be returned, then WorkManager will start
     * exponential backoff by itself. If there are trouble making the network request, then return
     * FAILURE. Note: This worker should be a periodic worker, meaning it will reschedule itself to
     * make the request in x amount of time.
     */
    override fun doWork(): Result {
        // Create a retrofit API.
        val serviceApi: MessageMixerRetrofitService =
                RuntimeUtil.getRetrofit().create(MessageMixerRetrofitService::class.java)

        // Create an pingRequest for the API.
        val pingRequest = PingRequest(HostAppInfoRepository.instance().getVersion(), RuntimeUtil.getUserIdentifiers())

        // Create an retrofit API network call.
        val responseCall = serviceApi.performPing(
                HostAppInfoRepository.instance().getInAppMessagingSubscriptionKey().toString(),
                AccountRepository.instance().getRaeToken(),
                HostAppInfoRepository.instance().getDeviceId().toString(),
                ConfigResponseRepository.instance().getPingEndpoint(),
                pingRequest)
        return try {
            // Execute a thread blocking API network call, and handle response.
            onResponse(responseCall.execute())
        } catch (e: Exception) {
            Timber.tag(TAG).d(e)
            Result.failure()
        }
    }

    /**
     * This method process returned response from Message Mixer Service if connection was successful.
     * And return RETRY if response code is 500 because server could be busy for the moment.
     *
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500. Invoke
     * MessageMixerResponse.isSuccessful() to determine if the response indicates success. If response
     * wasn't successful, return RETRY to let WorkManger retry with exponential backoff. If response
     * is 400, return FAILURE.
     */
    @VisibleForTesting
    @SuppressWarnings("LongMethod")
    fun onResponse(response: Response<MessageMixerResponse>): Result {
        if (response.isSuccessful) {
            val messageMixerResponse = response.body()
            if (messageMixerResponse != null) {
                // Add time data.
                PingResponseMessageRepository.instance().lastPingMillis = messageMixerResponse.currentPingMillis

                // Parse all data in response.
                val parsedMessages = parsePingResponseWithTestMessage(messageMixerResponse)

                // Add all parsed messages into PingResponseMessageRepository.
                PingResponseMessageRepository.instance().replaceAllMessages(parsedMessages)

                // Start a new MessageEventReconciliationWorker, there was a new Ping Response to parse.
                // This worker will attempt to cancel message scheduled but hasn't been displayed yet
                // because there could be an edge case where the message is obsolete.
                eventMessageScheduler.startEventMessageReconciliationWorker()

                // Schedule next ping.
                scheduleNextPing(messageMixerResponse.nextPingMillis)
                Timber.tag(TAG).d("campaign size: %d", messageMixerResponse.data.size)
            }
        } else return when {
            response.code() == RetryDelayUtil.RETRY_ERROR_CODE -> {
                messageMixerScheduler.pingMessageMixerService(MessageMixerPingScheduler.currDelay)
                MessageMixerPingScheduler.currDelay = retryUtil.getNextDelay(MessageMixerPingScheduler.currDelay)
                // set previous worker as success to avoid logging
                Result.success()
            }
            response.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> Result.retry() // Retry if server has error.
            else -> Result.failure()
        }
        return Result.success()
    }

    /**
     * This method schedules the next ping to Message Mixer. Due to JobScheduler's limitation,
     * WorkRequest backoff time has to be between 10 seconds to 5 hours.
     */
    @Throws(IllegalArgumentException::class)
    private fun scheduleNextPing(nextPingMillis: Long) {
        // reset current delay to initial
        MessageMixerPingScheduler.currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
        messageMixerScheduler.pingMessageMixerService(nextPingMillis)
        Timber.tag(TAG).d("Next ping scheduled in: %d", nextPingMillis)
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
    }
}
