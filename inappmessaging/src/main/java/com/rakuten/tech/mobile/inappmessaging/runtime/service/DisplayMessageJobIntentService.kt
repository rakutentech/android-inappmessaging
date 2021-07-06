package com.rakuten.tech.mobile.inappmessaging.runtime.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.core.app.JobIntentService
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.datasource.BaseDataSubscriber
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.request.ImageRequest
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
        val imageUrl = message.getMessagePayload()?.resource?.imageUrl
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
     * Once image is fully downloaded, ImagePrefetchSubscriber will trigger to display the message.
     */
    private fun fetchImageThenDisplayMessage(
        message: Message,
        hostActivity: Activity,
        imageUrl: String
    ) {
        // If Fresco has not been initialized, initialize it first.
        Fresco.getImagePipeline()
                .prefetchToBitmapCache(ImageRequest.fromUri(imageUrl), null /* callerContext */)
                .subscribe(
                        ImagePrefetchSubscriber(message, hostActivity),
                        UiThreadImmediateExecutorService.getInstance())
    }

    /**
     * This method displays message on UI thread.
     */
    private fun displayMessage(message: Message, hostActivity: Activity) {
        if (!verifyContexts(message)) {
            // Message display aborted by the host app
            Timber.tag(TAG).d("message display cancelled by the host app")

            // increment time closed to handle required number of events to be triggered
            readyMessagesRepo.removeMessage(message.getCampaignId() ?: "", true)

            prepareNextMessage()
            return
        }

        UiThreadImmediateExecutorService.getInstance()
                .execute(
                        DisplayMessageRunnable(
                                message,
                                hostActivity,
                                calculateImageAspectRatio(
                                        message.getMessagePayload()?.resource?.imageUrl)))
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

    /**
     * This method calculates cached image aspect ratio based on its width and height(width / height).
     * If cached image is not found, default aspect ratio (0.75) will be returned.
     */
    private fun calculateImageAspectRatio(imageUrl: String?): Float {
        if (!imageUrl.isNullOrEmpty()) {
            // Get Fresco's image pipeline.
            val imagePipeline = Fresco.getImagePipeline()

            // Get image cache key from image pipeline.
            val cacheKey =
                    imagePipeline.getCacheKey(ImageRequest.fromUri(imageUrl), null /*callerContext*/)

            // Get reference of the cached image from image pipeline.
            val closeableReference = imagePipeline.getCachedImage(cacheKey)

            // Get image from the reference.
            if (closeableReference != null) {
                try {
                    val closeableImage = closeableReference.get()
                    return closeableImage.width.toFloat() / closeableImage.height.toFloat()
                } catch (ie: IllegalStateException) {
                    Timber.tag(TAG).d(ie.cause, "unable to compute for aspect ratio")
                }
            }
        }
        return DEFAULT_IMAGE_ASPECT_RATIO
    }

    /**
     * Subscriber class of downloading images from network.
     */
    inner class ImagePrefetchSubscriber(
        private val message: Message,
        private val hostActivity: Activity
    ) :
        BaseDataSubscriber<Void>() {

        @VisibleForTesting
        override fun onNewResultImpl(dataSource: DataSource<Void?>) {
            // After image is fully downloaded, then display message.
            if (dataSource.progress >= ONE_HUNDRED_PERCENT) {
                displayMessage(this.message, this.hostActivity)
                Timber.tag(TAG).d("Downloading image progress: %f", dataSource.progress)
            }
        }

        override fun onFailureImpl(dataSource: DataSource<Void?>) {
            // When image can't be downloaded, there's no need to display this message.
            Timber.tag(TAG).d("Downloading image failed")
        }
    }

    companion object {
        private const val ONE_HUNDRED_PERCENT = 1f
        private const val DISPLAY_MESSAGE_JOB_ID = 3210
        private const val DEFAULT_IMAGE_ASPECT_RATIO = 0.75f
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
