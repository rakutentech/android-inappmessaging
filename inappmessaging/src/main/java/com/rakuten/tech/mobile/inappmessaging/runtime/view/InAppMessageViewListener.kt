package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.Magnifier
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.coroutine.MessageActionsCoroutine
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Touch and clicker listener class for InAppMessageView.
 */
internal class InAppMessageViewListener(
    val message: Message?,
    private val messagCoroutine: MessageActionsCoroutine = MessageActionsCoroutine(),
    private val displayManager: DisplayManager = DisplayManager.instance(),
    private val buildChecker: BuildVersionChecker = BuildVersionChecker.instance()
) :
    View.OnTouchListener, View.OnClickListener {

    // set to public to be mocked in testing.
    @VisibleForTesting
    var magnifier: Magnifier? = null
    private var isOptOutChecked: Boolean = false

    /**
     * Callback When touch event occurred. Which will trigger to magnify message view content.
     */
    @SuppressLint("NewApi")
    @Suppress("LongMethod")
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
            view.performClick()
            return true
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
            CoroutineScope(Dispatchers.Main).launch {
                displayManager.removeMessage(InAppMessaging.instance().getRegisteredActivity())
                withContext(Dispatchers.Default) {
                    val result = messagCoroutine.executeTask(message, view.id, isOptOutChecked)
                    if (result) {
                        displayManager.displayMessage()
                    }
                }
            }
        }
    }
}
