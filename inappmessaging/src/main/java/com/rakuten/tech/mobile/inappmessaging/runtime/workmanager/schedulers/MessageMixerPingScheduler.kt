package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppError
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppErrorLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.BackendApi
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkManagerUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.MessageMixerWorker
import java.util.concurrent.TimeUnit

/**
 * Utility class used for scheduling WorkManager's workers to communicate with IAM /ping endpoint.
 */
@SuppressWarnings(
    "kotlin:S6517",
)
internal interface MessageMixerPingScheduler {

    /**
     * This method syncs with Message Mixer service in the background with all necessary input data. Note: If
     * request to Message Mixer is successful, a new request will be automatically scheduled for the
     * future.
     */
    fun pingMessageMixerService(initialDelay: Long, workManager: WorkManager? = null)

    @SuppressWarnings("kotlin:S6515")
    companion object {
        private const val MESSAGE_MIXER_PING_WORKER = "iam_message_mixer_worker"
        private var instance: MessageMixerPingScheduler = MessageMixerPingSchedulerImpl()
        internal var currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY

        fun instance(): MessageMixerPingScheduler = instance
    }

    private class MessageMixerPingSchedulerImpl : MessageMixerPingScheduler {
        override fun pingMessageMixerService(initialDelay: Long, workManager: WorkManager?) {
            // Do not continue if config is disabled.
            if (!ConfigResponseRepository.instance().isConfigEnabled()) {
                return
            }

            // this is just to handle possible overflow but should never occur
            val delay = if (
                Long.MAX_VALUE - System.currentTimeMillis() <= TimeUnit.MILLISECONDS.toMillis(initialDelay)
            ) {
                // reset current delay
                currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
                RetryDelayUtil.INITIAL_BACKOFF_DELAY
            } else {
                initialDelay
            }

            // Schedule a work request to fetch campaigns. Add a unique tag with this request.
            val periodicMessageMixerFetch = OneTimeWorkRequest.Builder(MessageMixerWorker::class.java)
                // Delay will be between androidx.work.WorkRequest.MAX_BACKOFF_MILLIS,
                // and androidx.work.WorkRequest.MIN_BACKOFF_MILLIS.
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(WorkManagerUtil.getNetworkConnectedConstraint())
                .addTag(MESSAGE_MIXER_PING_WORKER)
                .build()

            // Enqueue work request in the background.
            enqueueRequest(workManager, periodicMessageMixerFetch)
        }

        private fun enqueueRequest(workManager: WorkManager?, periodicMessageMixerFetch: OneTimeWorkRequest) {
            try {
                HostAppInfoRepository.instance().getContext()?.let { ctx ->
                    (workManager ?: WorkManager.getInstance(ctx)).enqueueUniqueWork(
                        MESSAGE_MIXER_PING_WORKER, ExistingWorkPolicy.REPLACE, periodicMessageMixerFetch,
                    )
                }
            } catch (ie: IllegalStateException) {
                "In-App Messaging ping request failed".let {
                    InAppErrorLogger.logError(
                        "MessageMixerPingScheduler",
                        InAppError(
                            it,
                            ex = InAppMessagingException(it, ie), ev = Event.OperationFailed(BackendApi.PING.name),
                        ),
                    )
                }
            }
        }
    }
}
