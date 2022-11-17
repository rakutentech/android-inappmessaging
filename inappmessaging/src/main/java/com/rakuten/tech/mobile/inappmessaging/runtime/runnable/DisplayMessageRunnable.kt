package com.rakuten.tech.mobile.inappmessaging.runtime.runnable

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.UiThread
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Impression
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.ImpressionManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ResourceUtils
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ViewUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageFullScreenView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageModalView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessageSlideUpView
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessagingTooltipView
import kotlinx.coroutines.Runnable
import java.util.Date

/**
 * Displaying message runnable which presents the message on the UI thread. Message close, and other
 * button actions will also be handled here.
 */
@UiThread
internal class DisplayMessageRunnable(
    private val message: Message,
    private val hostActivity: Activity,
    private val displayManager: DisplayManager = DisplayManager.instance()
) : Runnable {
    internal var testLayout: FrameLayout? = null

    /**
     * Interface method which will be invoked by the Virtual Machine. This is also the actual method
     * which will display message with correct data.
     */
    @UiThread
    override fun run() {
        val messageType = InAppMessageType.getById(message.getType())
        if (shouldNotDisplay(messageType)) return

        if (messageType != null) {
            when (messageType) {
                InAppMessageType.MODAL -> handleModal()
                InAppMessageType.FULL -> handleFull()
                InAppMessageType.SLIDE -> handleSlide()
                InAppMessageType.HTML, InAppMessageType.INVALID -> Any()
                InAppMessageType.TOOLTIP -> handleTooltip()
            }
        }
    }

    private fun handleSlide() {
        val slideUpView = hostActivity.layoutInflater.inflate(R.layout.in_app_message_slide_up, null)
            as InAppMessageSlideUpView
        slideUpView.populateViewData(message)
        hostActivity.addContentView(slideUpView, hostActivity.window.attributes)
        ImpressionManager.sendImpressionEvent(
            message.getCampaignId(),
            listOf(Impression(ImpressionType.IMPRESSION, Date().time)),
            impressionTypeOnly = true
        )
    }

    private fun handleFull() {
        val fullScreenView = hostActivity.layoutInflater.inflate(R.layout.in_app_message_full_screen, null)
            as InAppMessageFullScreenView
        fullScreenView.populateViewData(message)
        hostActivity.addContentView(fullScreenView, hostActivity.window.attributes)
        ImpressionManager.sendImpressionEvent(
            message.getCampaignId(),
            listOf(Impression(ImpressionType.IMPRESSION, Date().time)),
            impressionTypeOnly = true
        )
    }

    private fun handleModal() {
        val modalView = hostActivity.layoutInflater.inflate(R.layout.in_app_message_modal, null)
            as InAppMessageModalView
        modalView.populateViewData(message)
        hostActivity.addContentView(modalView, hostActivity.window.attributes)
        ImpressionManager.sendImpressionEvent(
            message.getCampaignId(),
            listOf(Impression(ImpressionType.IMPRESSION, Date().time)),
            impressionTypeOnly = true
        )
    }

    private fun handleTooltip() {
        val toolTipView = hostActivity.layoutInflater.inflate(R.layout.in_app_message_tooltip, null)
            as InAppMessagingTooltipView
        toolTipView.populateViewData(message)
        message.getTooltipConfig()?.let { displayTooltip(it, toolTipView) }
    }

    private fun displayTooltip(
        tooltip: Tooltip,
        toolTipView: InAppMessagingTooltipView
    ) {
        ResourceUtils.findViewByName<View>(hostActivity, tooltip.id)?.let { target ->
            val scroll = ViewUtil.getScrollView(target)
            if (scroll != null) {
                displayInScrollView(scroll, toolTipView, target)
            } else {
                val params = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                hostActivity.addContentView(toolTipView, params)
            }
            if (tooltip.autoDisappear != null && tooltip.autoDisappear > 0) {
                displayManager.removeMessage(hostActivity, delay = tooltip.autoDisappear, id = message.getCampaignId())
            }
        }
    }

    @SuppressWarnings("LongMethod")
    private fun displayInScrollView(scroll: ViewGroup, toolTipView: InAppMessagingTooltipView, target: View) {
        var frame = hostActivity.findViewById<FrameLayout>(R.id.in_app_message_tooltip_layout)
        // use existing tooltip layout if already available
        if (frame == null) {
            frame = testLayout ?: FrameLayout(hostActivity)
            frame.id = R.id.in_app_message_tooltip_layout
            frame.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            val layoutIdx = ViewUtil.getFirstLayoutChild(scroll)
            if (layoutIdx < 0) {
                return
            }
            val parent = scroll.getChildAt(layoutIdx)

            if (parent is CoordinatorLayout) {
                ViewUtil.getFrameLayout(target)?.let {
                    it.addView(toolTipView, ViewUtil.getToolBarIndex(it))
                }
            } else {
                scroll.removeView(parent as ViewGroup)
                frame.addView(parent)
                frame.addView(toolTipView)
                scroll.addView(frame)
            }
        } else {
            frame.addView(toolTipView)
        }
    }

    private fun shouldNotDisplay(messageType: InAppMessageType?): Boolean {
        val normalCampaign = hostActivity.findViewById<View?>(R.id.in_app_message_base_view)
        return if (messageType == InAppMessageType.TOOLTIP) {
            // if normal non-slide-up campaign is displayed, don't display tooltip on top of normal campaign
            if (normalCampaign != null && normalCampaign !is InAppMessageSlideUpView) {
                true
            } else {
                checkTooltipDisplay()
            }
        } else {
            normalCampaign != null
        }
    }

    private fun checkTooltipDisplay(): Boolean {
        hostActivity.findViewById<View?>(R.id.in_app_message_tooltip_view)?.parent?.let {
            for (i in 0 until (it as ViewGroup).childCount) {
                val child = it.getChildAt(i)
                if (child?.id == R.id.in_app_message_tooltip_view && child.tag == message.getCampaignId()) {
                    // tool campaign is already displayed, no need to display again
                    return true
                }
            }
        }
        return false
    }
}
