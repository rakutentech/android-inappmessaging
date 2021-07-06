package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.rakuten.tech.mobile.inappmessaging.runtime.InApp
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.MessageEventReconciliationWorker

/**
 * Scheduler for MessageEventReconciliationWorker. Providing static helper functions.
 */
internal interface EventMessageReconciliationScheduler {
    /**
     * A chain work requests, first reconcile messages and events. After work has been successfully
     * completed, schedule to display next ready message.
     */
    @SuppressWarnings("FunctionMaxLength")
    fun startEventMessageReconciliationWorker(workManager: WorkManager? = null)

    companion object {
        private const val MESSAGES_EVENTS_WORKER_NAME = "iam_messages_events_worker"
        private var instance: EventMessageReconciliationScheduler = EventMessageReconciliationSchedulerImpl()

        fun instance(): EventMessageReconciliationScheduler = instance
    }

    private class EventMessageReconciliationSchedulerImpl : EventMessageReconciliationScheduler {

        @SuppressWarnings("FunctionMaxLength")
        override fun startEventMessageReconciliationWorker(workManager: WorkManager?) {
            // Starts MessageEventReconciliationWorker as a unique worker.
            // This worker must be a unique worker, but it can be replaced with a new one. Because we don't
            // want the same worker working in parallel which will result bad data, and unwanted behaviour.
            val reconciliationWorkRequest = OneTimeWorkRequest.Builder(MessageEventReconciliationWorker::class.java)
                    .addTag(MESSAGES_EVENTS_WORKER_NAME)
                    .build()

            try {
                val context = InAppMessaging.instance().getHostAppContext()
                context?.let {
                    val manager = workManager ?: WorkManager.getInstance(it)
                    manager.beginUniqueWork(MESSAGES_EVENTS_WORKER_NAME,
                            ExistingWorkPolicy.REPLACE, reconciliationWorkRequest)
                            .enqueue()
                }
            } catch (ie: IllegalStateException) {
                // this should not occur since work manager is initialized during SDK initialization
                InApp.errorCallback?.let {
                    it(InAppMessagingException("In-App Messaging message reconciliation failed", ie))
                }
            }
        }
    }
}
