package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.DisplayMessageWorker
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
    fun removeMessage(activity: Activity?, removeAll: Boolean = false, delay: Int = 0, id: String? = null): Any?

    companion object {
        private const val TAG = "IAM_DisplayManager"
        private const val MS_MULTIPLIER = 1000L

        private var instance: DisplayManager = DisplayManagerImpl()

        fun instance() = instance
    }

    private class DisplayManagerImpl : DisplayManager {

        override fun displayMessage() {
            DisplayMessageWorker.enqueueWork(true)
            DisplayMessageWorker.enqueueWork(false)
        }

        override fun removeMessage(activity: Activity?, removeAll: Boolean, delay: Int, id: String?): Any? {
            if (activity == null) return null

            // Find any displaying InApp Message view from the activity.
            return if (id != null) {
                // id is not null if tooltip
                activity.findViewById<ViewGroup>(R.id.in_app_message_tooltip_view)?.parent?.let {
                    for (i in 0..(it as ViewGroup).childCount) {
                        val child = it.getChildAt(i)
                        if (child?.id == R.id.in_app_message_tooltip_view && child.tag == id) {
                            scheduleRemoval(delay, child as ViewGroup)
                            break
                        }
                    }
                }
                null
            } else {
                if (removeAll) {
                    removeAllTooltip(activity, delay)
                }
                // remove normal campaign
                activity.findViewById<ViewGroup>(R.id.in_app_message_base_view)?.let {
                    scheduleRemoval(delay, it)
                    it.tag
                }
            }
        }

        private fun removeAllTooltip(activity: Activity, delay: Int) {
            activity.findViewById<ViewGroup>(R.id.in_app_message_tooltip_view)?.rootView?.let {
                val viewList = mutableListOf<View>()
                for (i in 0..(it as ViewGroup).childCount) {
                    val child = it.getChildAt(i)
                    if (child.id == R.id.in_app_message_tooltip_view) {
                        viewList.add(child)
                    }
                }
                for (view in viewList) {
                    scheduleRemoval(delay, view as ViewGroup)
                }
            }
        }

        private fun scheduleRemoval(delay: Int, view: ViewGroup) {
            if (delay > 0) {
                Handler(Looper.getMainLooper()).postDelayed(
                    { removeCampaign(view) }, delay * MS_MULTIPLIER
                )
            } else {
                // to avoid crashing when redirect from tooltip view
                removeCampaign(view)
            }
        }

        private fun removeCampaign(inAppMessageBaseView: ViewGroup) {
            // Removing just the InApp Message from the view hierarchy.
            if (inAppMessageBaseView.parent !is ViewGroup) {
                // avoid crash
                return
            }

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
