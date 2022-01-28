package com.rakuten.tech.mobile.inappmessaging.runtime.runnable

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.annotation.UiThread
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ContextExtension.findViewByName
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ViewUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageFullScreenView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageModalView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageSlideUpView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessagingTooltipView
import kotlinx.coroutines.Runnable

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
    @UiThread
    @SuppressWarnings("LongMethod")
    override fun run() {
        // If there's already a message found, don't display another message.
        if (hostActivity.findViewById<View?>(R.id.in_app_message_base_view) != null) {
            return
        } else if (hostActivity.findViewById<View?>(R.id.in_app_message_tooltip_view)?.tag == message.getCampaignId()) {
            return
        }

        val messageType = InAppMessageType.getById(message.getType())
        if (messageType != null) {
            when (messageType) {
                InAppMessageType.MODAL -> {
                    val modalView = hostActivity
                        .layoutInflater
                        .inflate(R.layout.in_app_message_modal, null) as InAppMessageModalView
                    modalView.populateViewData(message)
                    hostActivity.addContentView(modalView, hostActivity.window.attributes)
                }
                InAppMessageType.FULL -> {
                    val fullScreenView = hostActivity
                            .layoutInflater
                            .inflate(
                                    R.layout.in_app_message_full_screen,
                                    null) as InAppMessageFullScreenView
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
                InAppMessageType.TOOLTIP -> {
//                    return
                    val toolTipView = hostActivity
                        .layoutInflater
                        .inflate(
                            R.layout.in_app_message_tooltip,
                            null) as InAppMessagingTooltipView
                    toolTipView.populateViewData(message)
                    message.getTooltipConfig()?.let {
                        InAppMessaging.instance().getRegisteredActivity()?.findViewByName<View>(it.id)?.let { target ->
                            if (ViewUtil.isInScrollView(target)) {
                                val frame = FrameLayout(hostActivity)
                                frame.id = R.id.in_app_message_tooltip_layout
                                frame.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                                // TODO need to check which view is the scrollview
                                val parent = target.parent
                                val scroll = parent.parent
                                (scroll as ScrollView).removeView(parent as ViewGroup)
                                frame.addView(parent)
                                frame.addView(toolTipView)
                                scroll.addView(frame)
                            } else {
                                val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                hostActivity.addContentView(toolTipView, params)
                            }
                        }
                    }
                }
                else -> Any()
            }
        }
    }
}
