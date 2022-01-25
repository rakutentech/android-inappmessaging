package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.service.DisplayMessageJobIntentService
import timber.log.Timber

/**
 * Display manager, which controls displaying message, or removing message from the screen.
 */
internal interface DisplayManager {

    /**
     * Thread safe method to display message on UI thread.
     */
    fun displayMessage()

    /**
     * Removing the InApp message view from the screen, and makes the parent view interactive again.
     */
    fun removeMessage(activity: Activity?, delay: Int = 0): Any?

    companion object {
        private const val TAG = "IAM_DisplayManager"
        private const val MS_MULTIPLIER = 1000L

        private var instance: DisplayManager = DisplayManagerImpl()

        fun instance() = instance
    }

    private class DisplayManagerImpl : DisplayManager {

        override fun displayMessage() {
            DisplayMessageJobIntentService.enqueueWork(Intent())
        }

        override fun removeMessage(activity: Activity?, delay: Int): Any? {
            if (activity == null) return null

            // Find any displaying InApp Message view from the activity.
            val inAppMessageBaseView = activity.findViewById<ViewGroup>(R.id.in_app_message_base_view)
                ?: activity.findViewById(R.id.in_app_message_tooltip_view)
            if (inAppMessageBaseView != null) {
                Handler(Looper.getMainLooper()).postDelayed(
                    { removeCampaign(inAppMessageBaseView) }, delay * MS_MULTIPLIER
                )
            }
            return inAppMessageBaseView?.tag
        }

        private fun removeCampaign(inAppMessageBaseView: ViewGroup) {
            // Removing just the InApp Message from the view hierarchy.
            val parent = inAppMessageBaseView.parent as ViewGroup
            if (parent.id == R.id.in_app_message_tooltip_layout) {
                val gp = parent.parent
                // assumption it is only two children: host app layout + tooltip
                for (i in 0..parent.childCount) {
                    val child = parent.getChildAt(i)
                    if (child.id != R.id.in_app_message_tooltip_view) {
                        (gp as ViewGroup).removeView(parent)
                        parent.removeView(child)
                        gp.addView(child)
                        break
                    }
                }
            } else {
                parent.isFocusableInTouchMode = true
                parent.requestFocus()
                parent.removeView(inAppMessageBaseView)
            }
            Timber.tag(TAG).d("View removed")
        }
    }
}
