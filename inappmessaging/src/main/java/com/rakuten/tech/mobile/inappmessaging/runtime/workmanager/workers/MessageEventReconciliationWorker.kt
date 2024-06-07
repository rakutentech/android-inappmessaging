package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.MessageEventReconciliationUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.EventMatchingUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil.getCurrentTimeMillis

/**
 * This worker's main task is to reconcile messages with local events. This worker must be a unique
 * worker. Because multiple of the same worker working in parallel will result bad data, unwanted
 * behaviour, and bad performance.
 */
internal class MessageEventReconciliationWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val eventMatchingUtil: EventMatchingUtil,
    private val messageEventReconciliationUtil: MessageEventReconciliationUtil,
    private val messageReadinessManager: MessageReadinessManager,
) :
    Worker(context, workerParams) {

    /**
     * Overload constructor to handle OneTimeWorkRequest.Builder().
     */
    constructor(context: Context, workerParams: WorkerParameters) :
        this(
            context, workerParams, EventMatchingUtil.instance(),
            MessageEventReconciliationUtil(
                campaignRepo = CampaignRepository.instance(),
                eventMatchingUtil = EventMatchingUtil.instance(),
            ),
            MessageReadinessManager.instance(),
        )

    /**
     * This method is the entry point of this worker.
     * Reconcile ping response messages with existing local trigger events.
     * Then add ready to display messages to repository.
     */
    override fun doWork(): Result {
        InAppLogger(TAG).debug("Reconcile messages and local events")
        var startTime: Long = 0
        if (BuildConfig.DEBUG) {
            startTime = getCurrentTimeMillis()
        }

        // Validate campaigns against current events.
        reconcileMessagesAndEvents()

        // Schedule to display next message.
        DisplayManager.instance().displayMessage()

        var endTime: Long = 0
        if (BuildConfig.DEBUG) {
            endTime = getCurrentTimeMillis()
        }
        InAppLogger(TAG).debug("Time took to reconcile: %d milliseconds", endTime - startTime)
        return Result.success()
    }

    private fun reconcileMessagesAndEvents() {
        messageEventReconciliationUtil.validate { campaign, events ->
            if (eventMatchingUtil.removeSetOfMatchedEvents(events, campaign)) {
                messageReadinessManager.addMessageToQueue(campaign.campaignId)
                InAppLogger(TAG).debug(
                    "Ready message - campaignId: ${campaign.campaignId}, " +
                        "header: ${campaign.messagePayload.header}",
                )
            }
        }
    }

    companion object {
        private const val TAG = "IAM_MsgEventReconWorker"
    }
}
