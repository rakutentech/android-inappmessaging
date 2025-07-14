package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppError
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppErrorLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.SdkApi
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.MessageEventReconciliationWorker
import java.util.concurrent.TimeUnit

/**
 * Scheduler for MessageEventReconciliationWorker. Providing static helper functions.
 */
@SuppressWarnings(
    "kotlin:S6517",
)
internal interface EventMessageReconciliationScheduler {
    /**
     * A chain work requests, first reconcile messages and events. After work has been successfully
     * completed, schedule to display next ready message.
     */
    fun startReconciliationWorker(workManager: WorkManager? = null, delay: Long = 0)

    @SuppressWarnings("kotlin:S6515")
    companion object {
        private const val MESSAGES_EVENTS_WORKER_NAME = "iam_messages_events_worker"
        private var instance: EventMessageReconciliationScheduler = EventMessageReconciliationSchedulerImpl()

        fun instance(): EventMessageReconciliationScheduler = instance
    }

    private class EventMessageReconciliationSchedulerImpl : EventMessageReconciliationScheduler {

        @SuppressWarnings("LongMethod")
        override fun startReconciliationWorker(workManager: WorkManager?, delay: Long) {
            // Starts MessageEventReconciliationWorker as a unique worker.
            // This worker must be a unique worker, but it can be replaced with a new one. Because we don't
            // want the same worker working in parallel which will result bad data, and unwanted behaviour.
            val reconciliationWorkRequest = OneTimeWorkRequest.Builder(MessageEventReconciliationWorker::class.java)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(MESSAGES_EVENTS_WORKER_NAME)
                .build()

            try {
                HostAppInfoRepository.instance().getContext()?.let { ctx ->
                    val manager = workManager ?: WorkManager.getInstance(ctx)
                    manager.beginUniqueWork(
                        MESSAGES_EVENTS_WORKER_NAME, ExistingWorkPolicy.REPLACE, reconciliationWorkRequest,
                    ).enqueue()
                }
            } catch (ie: IllegalStateException) {
                // this should not occur since work manager is initialized during SDK initialization
                "In-App Messaging message reconciliation failed".let {
                    InAppErrorLogger.logError(
                        "EventMessageReconciliation",
                        InAppError(it, InAppMessagingException(it, ie), Event.OperationFailed(SdkApi.LOG_EVENT.name)),
                    )
                }
            }
        }
    }
}
