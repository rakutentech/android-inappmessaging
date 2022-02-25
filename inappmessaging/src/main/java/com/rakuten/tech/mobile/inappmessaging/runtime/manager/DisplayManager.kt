package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.app.Activity
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.coroutine.MessageActionsCoroutine
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.TooltipMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ResourceUtils
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.DisplayMessageWorker
import com.rakuten.tech.mobile.sdkutils.logger.Logger

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

    fun removeHiddenTargets(parent: ViewGroup)

    companion object {
        private const val TAG = "IAM_DisplayManager"
        private const val MS_MULTI = 1000L

        private var instance: DisplayManager = DisplayManagerImpl()

        fun instance() = instance
    }

    private class DisplayManagerImpl : DisplayManager {

        override fun displayMessage() {
            // start with tooltip queue to make sure the all tooltip campaigns are displayed first
            DisplayMessageWorker.enqueueWork(true)
        }

        override fun removeMessage(activity: Activity?, removeAll: Boolean, delay: Int, id: String?): Any? {
            if (activity == null) return null

            return if (id != null) {
                // id is not null if tooltip
                removeWithId(activity, id, delay)
                null
            } else {
                if (removeAll) {
                    removeAllTooltip(activity, delay)
                }
                // remove normal campaign
                // find any displaying InApp Message view from the activity.
                activity.findViewById<ViewGroup>(R.id.in_app_message_base_view)?.let {
                    scheduleRemoval(delay, it)
                    it.tag
                }
            }
        }

        private fun removeWithId(activity: Activity, id: String?, delay: Int) {
            activity.findViewById<ViewGroup>(R.id.in_app_message_tooltip_view)?.let {
                if (it.tag == id) {
                    scheduleRemoval(delay, it, id)
                } else {
                    scheduleTargetChild(it, id, delay)
                }
            }
        }

        private fun scheduleTargetChild(it: ViewGroup, id: String?, delay: Int) {
            it.parent?.let { parent ->
                for (i in 0 until (parent as ViewGroup).childCount) {
                    val child = parent.getChildAt(i)
                    if (child?.id == R.id.in_app_message_tooltip_view && child.tag == id) {
                        scheduleRemoval(delay, child as ViewGroup, id)
                        break
                    }
                }
            }
        }

        private fun removeAllTooltip(activity: Activity, delay: Int) {
            activity.findViewById<ViewGroup>(R.id.in_app_message_tooltip_view)?.parent?.let {
                val viewList = mutableListOf<View>()
                for (i in 0 until (it as ViewGroup).childCount) {
                    val child = it.getChildAt(i)
                    if (child?.id == R.id.in_app_message_tooltip_view) {
                        viewList.add(child)
                    }
                }
                for (view in viewList) {
                    scheduleRemoval(delay, view as ViewGroup, view.tag as String?)
                }
            }
        }

        private fun scheduleRemoval(delay: Int, view: ViewGroup, id: String? = null) {
            if (delay > 0) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        removeCampaign(view, id)
                        // to handle repo update and impression request for auto disappear
                        MessageActionsCoroutine().executeTask(
                            TooltipMessageRepository.instance().getCampaign(id ?: ""),
                            ImpressionType.EXIT,
                            false
                        )
                    }, delay * MS_MULTI
                )
            } else {
                // to avoid crashing when redirect from tooltip view
                removeCampaign(view, id)
            }
        }

        private fun removeCampaign(inAppMessageBaseView: ViewGroup, id: String?) {
            // Removing just the InApp Message from the view hierarchy.
            if (inAppMessageBaseView.parent !is ViewGroup) {
                // avoid crash
                return
            }

            val parent = inAppMessageBaseView.parent as ViewGroup
            if (parent.id == R.id.in_app_message_tooltip_layout) {
                removeTooltip(parent, id)
            } else {
                parent.isFocusableInTouchMode = true
                parent.requestFocus()
                parent.removeView(inAppMessageBaseView)
            }
            Logger(TAG).debug("View removed")
        }

        private fun removeTooltip(parent: ViewGroup, id: String?) {
            val gp = parent.parent
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                if (child?.id == R.id.in_app_message_tooltip_view && child.tag == id) {
                    // remove target tooltip view
                    parent.removeView(child)
                    break
                }
            }
            val activity = InAppMessaging.instance().getRegisteredActivity() ?: return

            val tooltip = activity.findViewById<ViewGroup>(R.id.in_app_message_tooltip_view)
            if (tooltip == null && parent.childCount > 0) {
                val child = parent.getChildAt(0) // this is the scrollview
                // if there are no longer any tooltip existing, remove extra layout
                parent.removeView(child)
                (gp as ViewGroup).removeView(parent)
                gp.addView(child)
            }
        }

        override fun removeHiddenTargets(parent: ViewGroup) {
            val activity = InAppMessaging.instance().getRegisteredActivity() ?: return
            activity.findViewById<FrameLayout>(R.id.in_app_message_tooltip_layout)?.let { it ->
                val removeList = mutableListOf<View>()
                for (i in 0 until it.childCount) {
                    val child = it.getChildAt(i)
                    if (child?.id == R.id.in_app_message_tooltip_view) {
                        addToList(child, activity, parent, removeList)
                    }
                }
                for (view in removeList) {
                    (view.tag as String?)?.let {
                        TooltipMessageRepository.instance().removeMessage(it)
                        scheduleRemoval(0, view as ViewGroup, it)
                    }
                }
            }
        }

        private fun addToList(child: View, activity: Activity, parent: ViewGroup, removeList: MutableList<View>) {
            val message = TooltipMessageRepository.instance().getCampaign(child.tag as String)
            val target = message?.getTooltipConfig()?.id?.let {
                ResourceUtils.findViewByName<View>(activity, it)
            }
            val scrollBounds = Rect()
            parent.getHitRect(scrollBounds)
            if (target != null && !target.getLocalVisibleRect(scrollBounds)) {
                // no longer visible
                removeList.add(child)
            }
        }
    }
}
