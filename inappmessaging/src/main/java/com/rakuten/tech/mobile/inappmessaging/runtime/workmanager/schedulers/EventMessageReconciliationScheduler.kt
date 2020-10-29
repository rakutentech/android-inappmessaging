package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.MessageEventReconciliationWorker
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.MessageEventReconciliationWorker.Companion.EVENT_RECONCILIATION_DATA

/**
 * Scheduler for MessageEventReconciliationWorker. Providing static helper functions.
 */
internal interface EventMessageReconciliationScheduler {
    /**
     * A chain work requests, first reconcile messages and events. After work has been successfully
     * completed, schedule to display next ready message.
     */
    @Suppress("FunctionMaxLength")
    fun startEventMessageReconciliationWorker(message: Message? = null)

    companion object {
        private const val MESSAGES_EVENTS_WORKER_NAME = "iam_messages_events_worker"
        private var instance: EventMessageReconciliationScheduler = EventMessageReconciliationSchedulerImpl()

        fun instance(): EventMessageReconciliationScheduler = instance
    }

    private class EventMessageReconciliationSchedulerImpl : EventMessageReconciliationScheduler {

        @Suppress("FunctionMaxLength")
        override fun startEventMessageReconciliationWorker(message: Message?) {
            // Starts MessageEventReconciliationWorker as a unique worker.
            // This worker must be a unique worker, but it can be replaced with a new one. Because we don't
            // want the same worker working in parallel which will result bad data, and unwanted behaviour.
            val reconciliationWorkRequest =
                    OneTimeWorkRequest.Builder(MessageEventReconciliationWorker::class.java)
                            .addTag(MESSAGES_EVENTS_WORKER_NAME)
                            .setInputData(Data.Builder().putString(EVENT_RECONCILIATION_DATA,
                                    message?.getCampaignId()).build())
                            .build()
            WorkManager.getInstance(InAppMessaging.instance().getHostAppContext()!!)
                    .beginUniqueWork(MESSAGES_EVENTS_WORKER_NAME, ExistingWorkPolicy.REPLACE, reconciliationWorkRequest)
                    .enqueue()
        }
    }
}
