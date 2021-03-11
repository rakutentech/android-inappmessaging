package com.rakuten.tech.mobile.inappmessaging.runtime.runnable

import android.app.Activity
import android.view.View
import androidx.annotation.UiThread
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageFullScreenView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageModalView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageSlideUpView
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import kotlinx.coroutines.Runnable

/**
 * Displaying message runnable which presents the message on the UI thread. Message close, and other
 * button actions will also be handled here.
 */
@UiThread
internal class DisplayMessageRunnable(
    private val message: Message,
    private val hostActivity: Activity,
    private val imageAspectRatio: Float
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
                    modalView.populateViewData(message, imageAspectRatio)
                    hostActivity.addContentView(modalView, hostActivity.window.attributes)
                }
                InAppMessageType.FULL -> {
                    val fullScreenView = hostActivity
                            .layoutInflater
                            .inflate(
                                    R.layout.in_app_message_full_screen,
                                    null) as InAppMessageFullScreenView
                    fullScreenView.populateViewData(message, imageAspectRatio)
                    hostActivity.addContentView(fullScreenView, hostActivity.window.attributes)
                }
                InAppMessageType.SLIDE -> {
                    val slideUpView = hostActivity
                            .layoutInflater
                            .inflate(
                                    R.layout.in_app_message_slide_up,
                                    null) as InAppMessageSlideUpView
                    slideUpView.populateViewData(message, imageAspectRatio)
                    hostActivity.addContentView(slideUpView, hostActivity.window.attributes)
                }
                else -> {
                }
            }
        }
    }
}
