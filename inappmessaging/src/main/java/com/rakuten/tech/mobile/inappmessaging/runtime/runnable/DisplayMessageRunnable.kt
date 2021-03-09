package com.rakuten.tech.mobile.inappmessaging.runtime.runnable

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.UiThread
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ReadyForDisplayMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageBaseView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageFullScreenView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageModalView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageSlideUpView
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask

/**
 * Displaying message runnable which presents the message on the UI thread. Message close, and other
 * button actions will also be handled here.
 */
@UiThread
internal class DisplayMessageRunnable(
    private val message: Message,
    private val hostActivity: Activity
) : Runnable {

    /**
     * Interface method which will be invoked by the Virtual Machine. This is also the actual method
     * which will display message with correct data.
     */
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE") // No need to check casting.
    @UiThread
    @Suppress("LongMethod")
    override fun run() {
        // If there's already a message found, don't display another message.
        if (hostActivity.findViewById<View?>(R.id.in_app_message_base_view) != null) {
            return
        }

        val messageType = InAppMessageType.getById(message.getType())
        if (messageType != null) {
            when (messageType) {
                InAppMessageType.MODAL -> {
                    val modalView = hostActivity
                            .layoutInflater
                            .inflate(R.layout.in_app_message_modal, null) as InAppMessageModalView
                    downloadImage(modalView, message)
                    modalView.populateViewData(message)
                    hostActivity.addContentView(modalView, hostActivity.window.attributes)
                }
                InAppMessageType.FULL -> {
                    val fullScreenView = hostActivity
                            .layoutInflater
                            .inflate(
                                    R.layout.in_app_message_full_screen,
                                    null) as InAppMessageFullScreenView
                    downloadImage(fullScreenView, message)
                    fullScreenView.populateViewData(message)
                    hostActivity.addContentView(fullScreenView, hostActivity.window.attributes)
                }
                InAppMessageType.SLIDE -> {
                    val slideUpView = hostActivity
                            .layoutInflater
                            .inflate(
                                    R.layout.in_app_message_slide_up,
                                    null) as InAppMessageSlideUpView
                    slideUpView.populateViewData(message)
                    hostActivity.addContentView(slideUpView, hostActivity.window.attributes)
                }
                else -> {
                }
            }
        }
    }

    private var timer: Timer? = null
    private fun downloadImage(view: InAppMessageBaseView, message: Message) {
        val url = message.getMessagePayload()?.resource?.imageUrl
        if (url != null) {
            Glide.with(view).load(url).timeout(IMG_DOWNLOAD_TIMEOUT).into(
                    object : ImageViewTarget<Drawable>(view.findViewById(R.id.message_image_view)) {
                        override fun onLoadStarted(placeholder: Drawable?) {
                            super.onLoadStarted(placeholder)
                            // hide campaign view (cannot use visibility since Glide callback will not work.)
                            // https://github.com/bumptech/glide/issues/618
                            view.alpha = 0f
                            timer = Timer()
                            timer?.schedule(timerTask { handleDownload(view, false) }, TIMER_SCHEDULE)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            handleDownload(view, false)
                            super.onLoadFailed(errorDrawable)
                        }

                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            handleDownload(view, true)
                            super.onResourceReady(resource, transition)
                        }

                        override fun setResource(resource: Drawable?) {
                            getView().setImageDrawable(resource)
                        }
                    })
        }
    }

    private fun handleDownload(view: InAppMessageBaseView, isSuccess: Boolean) {
        Glide.with(view).pauseAllRequests()
        if (timer != null) {
            timer?.cancel()
            timer?.purge()
            timer = null
            hostActivity.runOnUiThread {
                if (isSuccess) {
                    view.alpha = 1f
                } else {
                    Timber.tag(TAG).d("Downloading image failed")
                    val id = DisplayManager.instance().removeMessage(hostActivity)
                    if (id != null) {
                        // drop event when download fail
                        ReadyForDisplayMessageRepository.instance().removeMessage(id as String, true)
                    }
                }
            }
        }
    }

    private fun timerTask(wrapped: Runnable) = object : TimerTask() {
        override fun run() {
            wrapped.run()
        }
    }

    companion object {
        private const val TAG = "IAM_MessageRunnable"
        private const val IMG_DOWNLOAD_TIMEOUT = 1000 // in ms (1s for connection timeout and 1s for read timeout)
        private const val TIMER_SCHEDULE = 2100L // in ms (100ms more than total time out to give chance to Glide)
    }
}
