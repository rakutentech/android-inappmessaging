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
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message
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
    val message: Message,
    private val messageCoroutine: MessageActionsCoroutine = MessageActionsCoroutine(),
    private val displayManager: DisplayManager = DisplayManager.instance(),
    private val buildChecker: BuildVersionChecker = BuildVersionChecker.instance(),
    private val eventScheduler: EventMessageReconciliationScheduler = EventMessageReconciliationScheduler.instance(),
    private val inApp: InAppMessaging = InAppMessaging.instance(),
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
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (buildChecker.isAndroidQAndAbove()) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> this.magnifier = Magnifier.Builder(view).build()
                MotionEvent.ACTION_MOVE -> handleMagnifier(view, event)
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> this.magnifier?.dismiss()
                else -> this.magnifier?.dismiss()
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
        } else if (R.id.message_close_button == view.id && !message.isCampaignDismissable) {
            // Disable closing the message if not dismissable.
            return
        } else {
            // Handling button click in coroutine.
            handleClick(view.id)
        }
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        return if (event != null && event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
            // Disable closing the message if not dismissable.
            if (!message.isCampaignDismissable) {
                false
            } else {
                // Handling back button click in coroutine.
                handleClick(MessageActionsCoroutine.BACK_BUTTON)
                true
            }
        } else {
            false
        }
    }

    internal fun handleClick(
        id: Int,
        mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ) {
        CoroutineScope(mainDispatcher).launch {
            displayManager.removeMessage(
                inApp.getRegisteredActivity(),
                id = if (message.type == InAppMessageType.TOOLTIP.typeId) message.campaignId else null,
            )
            withContext(dispatcher) {
                handleMessage(id)
            }
        }
    }

    internal fun handleMessage(id: Int) {
        val result = messageCoroutine.executeTask(message, id, isOptOutChecked)
        if (result) {
            eventScheduler.startReconciliationWorker(
                delay = (message.messagePayload.messageSettings.displaySettings.delay).toLong(),
            )
        }
    }

    @SuppressLint("NewApi")
    private fun handleMagnifier(view: View, event: MotionEvent) {
        this.magnifier?.let { magnifier ->
            val viewPosition = IntArray(2)
            view.getLocationOnScreen(viewPosition)
            magnifier.show(event.rawX - viewPosition[0], event.rawY - viewPosition[1])
        }
    }
}
