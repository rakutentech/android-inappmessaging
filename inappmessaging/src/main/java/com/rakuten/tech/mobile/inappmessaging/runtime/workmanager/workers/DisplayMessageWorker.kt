package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ImageUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import com.rakuten.tech.mobile.inappmessaging.runtime.runnable.DisplayMessageRunnable
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkManagerUtil
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

/**
 * Since one service is essentially one worker thread, so there's no chance multiple worker threads
 * can dispatch Runnables to Android's message queue. Only one at a time.
 */
internal class DisplayMessageWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    var messageReadinessManager = MessageReadinessManager.instance()
    var handler = Handler(Looper.getMainLooper())
    var picasso: Picasso? = null

    /**
     * This method starts displaying message runnable.
     */
    override suspend fun doWork(): Result {
        InAppLogger(TAG).debug("Display worker START, thread: ${Thread.currentThread().name}")
        prepareNextMessage()
        InAppLogger(TAG).debug("Display worker END")
        return Result.success()
    }

    /**
     * This method checks if there is a message to be displayed and proceeds if found.
     */
    private fun prepareNextMessage() {
        // Retrieving the next ready message, and its display permission been checked.
        val messages = messageReadinessManager.getNextDisplayMessage()
        val hostActivity = HostAppInfoRepository.instance().getRegisteredActivity()
        if (hostActivity != null) {
            for (message in messages) {
                val imageUrl = message.messagePayload.resource.imageUrl
                if (!imageUrl.isNullOrEmpty()) {
                    fetchImageThenDisplayMessage(message, hostActivity, imageUrl)
                } else {
                    // If no image, just display the message.
                    displayMessage(message, hostActivity)
                }
            }
        }
    }

    /**
     * This method fetches image from network, then cache it in memory.
     * Once image is fully downloaded, the message will be displayed.
     */
    private fun fetchImageThenDisplayMessage(message: Message, hostActivity: Activity, imageUrl: String) {
        ImageUtil.fetchImage(
            imageUrl = imageUrl,
            callback = object : Callback {
                override fun onSuccess() {
                    // Picasso callbacks run on main thread
                    // Prepare for next message off the main thread if this message is cancelled
                    displayMessage(message, hostActivity, true)
                }

                override fun onError(e: Exception?) {
                    InAppLogger(TAG).debug("Downloading image failed")
                }
            },
            context = hostActivity, picasso = picasso,
        )
    }

    private fun displayMessage(message: Message, hostActivity: Activity, newWorker: Boolean = false) {
        if (!verifyContexts(message)) {
            // Message display aborted by the host app
            InAppLogger(TAG).debug("Message display cancelled by the host app")
            // Remove message in queue
            messageReadinessManager.removeMessageFromQueue(message.campaignId)
            // Prepare next message
            if (newWorker) enqueueWork() else prepareNextMessage()
            return
        }

        // Display message on main thread
        handler.post(DisplayMessageRunnable(message, hostActivity))
    }

    /**
     * This method verifies campaign's contexts before displaying the message.
     */
    private fun verifyContexts(message: Message): Boolean {
        val campaignContexts = message.contexts
        if (message.isTest || campaignContexts.isEmpty()) {
            return true
        }

        return InAppMessaging.instance()
            .onVerifyContext(campaignContexts, message.messagePayload.title)
    }

    companion object {
        private const val TAG = "IAM_JobIntentService"
        private const val DISPLAY_WORKER = "iam_message_display_worker"

        /**
         * This method enqueues work in to this service.
         */
        fun enqueueWork() {
            HostAppInfoRepository.instance().getContext()?.let { ctx ->
                val displayRequest = OneTimeWorkRequest.Builder(DisplayMessageWorker::class.java)
                    .setConstraints(WorkManagerUtil.getNetworkConnectedConstraint())
                    .addTag(DISPLAY_WORKER)
                    .build()
                WorkManager.getInstance(ctx).enqueue(displayRequest)
            }
        }
    }
}
