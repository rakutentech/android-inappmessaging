package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkManagerUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.MessageMixerWorker
import java.util.concurrent.TimeUnit

/**
 * Utility class used for scheduling WorkManager's workers to communicate with IAM /ping endpoint.
 */
@SuppressWarnings("PMD.UseUtilityClass") // Private constructor made package private for testing.
internal interface MessageMixerPingScheduler {

    /**
     * This method syncs with Message Mixer service in the background with all necessary input data. Note: If
     * request to Message Mixer is successful, a new request will be automatically scheduled for the
     * future.
     */
    fun pingMessageMixerService(initialDelay: Long)

    companion object {
        private const val MESSAGE_MIXER_PING_WORKER = "iam_message_mixer_worker"
        private var instance: MessageMixerPingScheduler = MessageMixerPingSchedulerImpl()
        internal var currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY

        fun instance(): MessageMixerPingScheduler = instance
    }

    private class MessageMixerPingSchedulerImpl : MessageMixerPingScheduler {
        override fun pingMessageMixerService(initialDelay: Long) {
            // Do not continue if config is disabled.
            if (!ConfigResponseRepository.instance().isConfigEnabled()) {
                return
            }
            // Schedule a work request to fetch campaigns. Add a unique tag with this request.
            val periodicMessageMixerFetch = OneTimeWorkRequest.Builder(MessageMixerWorker::class.java)
                    // Delay will be between androidx.work.WorkRequest.MAX_BACKOFF_MILLIS,
                    // and androidx.work.WorkRequest.MIN_BACKOFF_MILLIS.
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setConstraints(WorkManagerUtil.getNetworkConnectedConstraint())
                    .addTag(MESSAGE_MIXER_PING_WORKER)
                    .build()

            // Enqueue work request in the background.
            WorkManager.getInstance(InAppMessaging.instance().getHostAppContext()!!)
                    .enqueueUniqueWork(MESSAGE_MIXER_PING_WORKER, ExistingWorkPolicy.REPLACE, periodicMessageMixerFetch)
        }
    }
}
