package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.Magnifier
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.coroutine.MessageActionsCoroutine
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Touch and clicker listener class for InAppMessageView.
 */
internal class InAppMessageViewListener(
    val message: Message?,
    private val messageCoroutine: MessageActionsCoroutine = MessageActionsCoroutine(),
    private val displayManager: DisplayManager = DisplayManager.instance(),
    private val buildChecker: BuildVersionChecker = BuildVersionChecker.instance(),
    private val eventScheduler: EventMessageReconciliationScheduler = EventMessageReconciliationScheduler.instance(),
    private val inApp: InAppMessaging = InAppMessaging.instance()
) :
    View.OnTouchListener, View.OnClickListener, View.OnKeyListener {

    // set to public to be mocked in testing.
    @VisibleForTesting
    var magnifier: Magnifier? = null
    private var isOptOutChecked: Boolean = false

    /**
     * Callback When touch event occurred. Which will trigger to magnify message view content.
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("LongMethod")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (buildChecker.isAndroidQAndAbove()) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> this.magnifier = Magnifier.Builder(view).build()
                MotionEvent.ACTION_MOVE -> if (this.magnifier != null) {
                    val viewPosition = IntArray(2)
                    view.getLocationOnScreen(viewPosition)
                    this.magnifier?.show(event.rawX - viewPosition[0],
                            event.rawY - viewPosition[1])
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> if (this.magnifier != null) {
                    this.magnifier?.dismiss()
                }
                else -> if (this.magnifier != null) this.magnifier?.dismiss()
            }
            return view.performClick()
        }
        return false
    }

    /**
     * OnClick listener callback when In-App message close button clicked.
     */
    @UiThread
    override fun onClick(view: View) {
        if (R.id.opt_out_checkbox == view.id) {
            // If user only checked the opt-out box, just assign the isOptOutChecked variable.
            this.isOptOutChecked = (view as CheckBox).isChecked
        } else {
            // Handling button click in coroutine.
            handleClick(view.id)
        }
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
            // Handling back button click in coroutine.
            handleClick(MessageActionsCoroutine.BACK_BUTTON)
            return true
        }
        return false
    }

    private fun handleClick(id: Int, dispatcher: CoroutineDispatcher = Dispatchers.Default) {
        CoroutineScope(Dispatchers.Main).launch {
            val type = MessageActionsCoroutine.getOnClickBehaviorType(id)
            val delay = if (message?.getType() == InAppMessageType.TOOLTIP.typeId &&
                (type != ImpressionType.CLICK_CONTENT || message.getTooltipConfig()?.url == null)) {
                // should only add delay if set and not redirect (i.e. close button or content + no url)
                    message.getTooltipConfig()?.autoDisappear ?: 0
            } else {
                0
            }
            displayManager.removeMessage(inApp.getRegisteredActivity(), delay = delay,
                id = if (message?.getType() == InAppMessageType.TOOLTIP.typeId) message.getCampaignId() else null)
            withContext(dispatcher) {
                handleMessage(type)
            }
        }
    }

    internal fun handleMessage(type: ImpressionType) {
        val result = messageCoroutine.executeTask(message, type, isOptOutChecked)
        if (result) {
            eventScheduler.startEventMessageReconciliationWorker(
                delay = (message?.getMessagePayload()?.messageSettings?.displaySettings?.delay ?: 0).toLong()
            )
        }
    }
}
