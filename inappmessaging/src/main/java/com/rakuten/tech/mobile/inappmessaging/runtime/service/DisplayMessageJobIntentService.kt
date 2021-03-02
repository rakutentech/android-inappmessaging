package com.rakuten.tech.mobile.inappmessaging.runtime.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.app.JobIntentService
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalDisplayedMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ReadyForDisplayMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import com.rakuten.tech.mobile.inappmessaging.runtime.runnable.DisplayMessageRunnable
import timber.log.Timber

/**
 * Since one service is essentially one worker thread, so there's no chance multiple worker threads
 * can dispatch Runnables to Android's message queue. Only one at a time.
 */
internal class DisplayMessageJobIntentService : JobIntentService() {
    var localDisplayRepo = LocalDisplayedMessageRepository.instance()
    var readyMessagesRepo = ReadyForDisplayMessageRepository.instance()
    var messageReadinessManager = MessageReadinessManager.instance()
    var handler = Handler(Looper.getMainLooper())

    /**
     * This method starts displaying message runnable.
     */
    public override fun onHandleWork(intent: Intent) {
        Timber.tag(TAG).d("onHandleWork() started on thread: %s", Thread.currentThread().name)
        prepareNextMessage()
        Timber.tag(TAG).d("onHandleWork() ended")
    }

    /**
     * This method checks if there is a message to be displayed and proceeds if found.
     */
    private fun prepareNextMessage() {
        // Retrieving the next ready message, and its display permission been checked.
        val message: Message = messageReadinessManager.getNextDisplayMessage() ?: return
        val hostActivity = InAppMessaging.instance().getRegisteredActivity()
        if (hostActivity != null) {
            displayMessage(message, hostActivity)
        }
    }

    /**
     * This method displays message on UI thread.
     */
    private fun displayMessage(message: Message, hostActivity: Activity) {
        if (!verifyContexts(message)) {
            // Message display aborted by the host app
            Timber.tag(TAG).d("message display cancelled by the host app")

            // Mark the message as displayed
            localDisplayRepo.addMessage(message)
            readyMessagesRepo.removeMessage(message.getCampaignId()!!)

            prepareNextMessage()
            return
        }

        handler.post(DisplayMessageRunnable(message, hostActivity))
    }

    /**
     * This method verifies campaign's contexts before displaying the message.
     */
    private fun verifyContexts(message: Message): Boolean {
        val campaignContexts = message.getContexts()
        if (message.isTest() || campaignContexts.isEmpty()) {
            return true
        }

        return InAppMessaging.instance().onVerifyContext(
                campaignContexts,
                message.getMessagePayload()?.title ?: "")
    }

    companion object {
        private const val DISPLAY_MESSAGE_JOB_ID = 3210
        private const val TAG = "IAM_JobIntentService"

        /**
         * This method enqueues work in to this service.
         */
        fun enqueueWork(work: Intent) {
            val context: Context = InAppMessaging.instance().getHostAppContext() ?: return
            enqueueWork(context, DisplayMessageJobIntentService::class.java, DISPLAY_MESSAGE_JOB_ID, work)
        }
    }
}
