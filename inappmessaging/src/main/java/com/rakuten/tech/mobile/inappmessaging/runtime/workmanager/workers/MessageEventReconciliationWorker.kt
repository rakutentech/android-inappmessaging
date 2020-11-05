package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.PingResponseMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ReadyForDisplayMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.MessageEventReconciliationUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil.getCurrentTimeMillis
import timber.log.Timber

/**
 * This worker's main task is to reconcile messages with local events. This worker must be a unique
 * worker. Because multiple of the same worker working in parallel will result bad data, unwanted
 * behaviour, and bad performance.
 */
internal class MessageEventReconciliationWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val pingRepo: PingResponseMessageRepository,
    private val messageUtil: MessageEventReconciliationUtil
) :
        Worker(context, workerParams) {

    /**
     * Overload constructor to handle OneTimeWorkRequest.Builder().
     */
    constructor(context: Context, workerParams: WorkerParameters) :
            this(context, workerParams, PingResponseMessageRepository.instance(),
            MessageEventReconciliationUtil.instance())

    /**
     * This method is the entry point of this worker.
     * Reconcile ping response messages with existing local trigger events.
     * Then add ready to display messages to repository.
     */
    @Suppress("LongMethod")
    override fun doWork(): Result {
        Timber.tag(TAG).d("doWork()")
        var startTime: Long = 0
        if (BuildConfig.DEBUG) {
            startTime = getCurrentTimeMillis()
        }

        // Messages list shouldn't be empty, if it is, then there's no more work to be done.
        val messageListCopy = pingRepo.getAllMessagesCopy()
        if (messageListCopy.isEmpty()) {
            // Job is done!
            return Result.success()
        }

        val messageUtil = messageUtil
        // Move test messages(ready to display) from messageList to a new list readyMessageList.
        val readyMessageList = ArrayList(messageUtil.extractTestMessages(messageListCopy))

        // Add a list of ready messages reconciled messages' triggers with existing local events.
        readyMessageList.addAll(messageUtil.reconcileMessagesAndEvents(messageListCopy))

        // Finally, add all ready messages into ReadyForDisplayMessageRepository.
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(readyMessageList)

        // Schedule to display next message.
        DisplayManager.instance().displayMessage()
        for (message in readyMessageList) {
            Timber.tag(TAG).d("Ready Messages: %s", message.getMessagePayload()?.header)
        }
        var endTime: Long = 0
        if (BuildConfig.DEBUG) {
            endTime = getCurrentTimeMillis()
        }
        Timber.tag(TAG).d("Time took to reconcile: %d milliseconds", endTime - startTime)
        return Result.success()
    }

    companion object {
        private const val TAG = "IAM_MsgEventReconWorker"
    }
}
