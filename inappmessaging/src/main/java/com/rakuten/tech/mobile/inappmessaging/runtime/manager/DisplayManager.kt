package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.app.Activity
import android.content.Intent
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
    fun removeMessage(activity: Activity?): Any?

    companion object {
        private const val TAG = "IAM_DisplayManager"

        private var instance: DisplayManager = DisplayManagerImpl()

        fun instance() = instance
    }

    private class DisplayManagerImpl : DisplayManager {

        override fun displayMessage() {
            DisplayMessageJobIntentService.enqueueWork(Intent())
        }

        override fun removeMessage(activity: Activity?): Any? {
            if (activity == null) return null

            // Find any displaying InApp Message view from the activity.
            val inAppMessageBaseView = activity.findViewById<ViewGroup>(R.id.in_app_message_base_view)
            if (inAppMessageBaseView != null) {
                // Removing just the InApp Message from the view hierarchy.
                val parent = inAppMessageBaseView.parent as ViewGroup
                parent.isFocusableInTouchMode = true
                parent.requestFocus()
                parent.removeView(inAppMessageBaseView)
                Timber.tag(TAG).d("View removed")
            }
            return inAppMessageBaseView?.tag
        }
    }
}
