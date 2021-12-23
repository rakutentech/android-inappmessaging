package com.rakuten.tech.mobile.inappmessaging.runtime.service

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.app.JobIntentService
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ImageUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalDisplayedMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ReadyForDisplayMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import com.rakuten.tech.mobile.inappmessaging.runtime.runnable.DisplayMessageRunnable
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import timber.log.Timber
import java.lang.Exception

/**
 * Since one service is essentially one worker thread, so there's no chance multiple worker threads
 * can dispatch Runnables to Android's message queue. Only one at a time.
 */
internal class DisplayMessageJobIntentService : JobIntentService() {
    var localDisplayRepo = LocalDisplayedMessageRepository.instance()
    var readyMessagesRepo = ReadyForDisplayMessageRepository.instance()
    var messageReadinessManager = MessageReadinessManager.instance()
    var handler = Handler(Looper.getMainLooper())
    var picasso: Picasso? = null

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
        val message = messageReadinessManager.getNextDisplayMessage() ?: return
        val hostActivity = InAppMessaging.instance().getRegisteredActivity()
        val imageUrl = message.getMessagePayload().resource.imageUrl
        if (hostActivity != null) {
            if (!imageUrl.isNullOrEmpty()) {
                    fetchImageThenDisplayMessage(message, hostActivity, imageUrl)
            } else {
                    // If no image, just display the message.
                    displayMessage(message, hostActivity)
            }
        }
    }

    /**
     * This method fetches image from network, then cache it in memory.
     * Once image is fully downloaded, the message will be displayed.
     */
    private fun fetchImageThenDisplayMessage(
        message: Message,
        hostActivity: Activity,
        imageUrl: String
    ) {
        ImageUtil.fetchImage(imageUrl, object : Callback {
            override fun onSuccess() {
                displayMessage(message, hostActivity)
            }

            override fun onError(e: Exception?) {
                Timber.tag(TAG).d("Downloading image failed")
            }
        }, hostActivity, picasso)
    }

    /**
     * This method displays message on UI thread.
     */
    private fun displayMessage(message: Message, hostActivity: Activity) {
        if (!verifyContexts(message)) {
            // Message display aborted by the host app
            Timber.tag(TAG).d("message display cancelled by the host app")

            // increment time closed to handle required number of events to be triggered
            readyMessagesRepo.removeMessage(message.getCampaignId(), true)

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

        return InAppMessaging.instance()
            .onVerifyContext(campaignContexts, message.getMessagePayload().title)
    }

    companion object {
        private const val DISPLAY_MESSAGE_JOB_ID = 3210
        private const val TAG = "IAM_JobIntentService"

        /**
         * This method enqueues work in to this service.
         */
        fun enqueueWork(work: Intent) {
            InAppMessaging.instance().getHostAppContext()?.let {
                enqueueWork(it, DisplayMessageJobIntentService::class.java, DISPLAY_MESSAGE_JOB_ID, work)
            }
        }
    }
}
