package com.rakuten.tech.mobile.inappmessaging.runtime.runnable

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.UiThread
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageBaseView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageFullScreenView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageModalView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageSlideUpView
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import kotlinx.coroutines.Runnable
import timber.log.Timber

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

    private fun downloadImage(view: InAppMessageBaseView, message: Message) {
        val url = message.getMessagePayload()?.resource?.imageUrl
        // hide campaign view (cannot use visibility since Glide callback will not work.)
        // https://github.com/bumptech/glide/issues/618
        if (url != null) {
            view.alpha = 0f
            Glide.with(view).load(url).addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Timber.tag(TAG).d("Downloading image failed")
                    // When image can't be downloaded, remove campaign.
                    DisplayManager.instance().removeMessage(hostActivity)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // show campaign
                    view.alpha = 1f
                    return false
                }
            }).timeout(IMG_DOWNLOAD_TIMEOUT).into(view.findViewById(R.id.message_image_view))
        }
    }

    companion object {
        private const val TAG = "IAM_MessageRunnable"
        private const val IMG_DOWNLOAD_TIMEOUT = 2000 // in ms
    }
}
