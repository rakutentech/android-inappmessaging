package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.app.Activity
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ResourceUtils
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessagingTooltipView
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.DisplayMessageWorker

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
        private const val MS_MULTIPLIER = 1000L

        @VisibleForTesting
        internal var instance: DisplayManager = DisplayManagerImpl(
            Handler(Looper.getMainLooper()),
        )

        fun instance() = instance
    }

    @SuppressWarnings(
        "TooManyFunctions",
        "LargeClass",
    )
    class DisplayManagerImpl(
        private val handler: Handler,
    ) : DisplayManager {

        override fun displayMessage() {
            DisplayMessageWorker.enqueueWork()
        }

        override fun removeMessage(activity: Activity?, removeAll: Boolean, delay: Int, id: String?): Any? {
            if (activity == null) return null

            // Find any displaying InApp Message view from the activity.
            return if (id != null) {
                // id is not null if tooltip
                removeWithId(activity, id, delay)
                null
            } else {
                if (removeAll) {
                    removeAllTooltip(activity, delay)
                }
                // remove normal campaign
                activity.findViewById<ViewGroup>(R.id.in_app_message_base_view)?.let { viewGroup ->
                    scheduleRemoval(delay, viewGroup, activity = activity)
                    viewGroup.tag
                }
            }
        }

        private fun removeWithId(activity: Activity, id: String?, delay: Int) {
            activity.findViewById<ViewGroup>(R.id.in_app_message_tooltip_view)?.let { view ->
                if (view.tag == id) {
                    scheduleRemoval(delay = delay, view = view, id = id, activity = activity)
                } else {
                    scheduleTargetChild(it = view, id = id, delay = delay, activity = activity)
                }
            }
        }

        private fun scheduleTargetChild(it: ViewGroup, id: String?, delay: Int, activity: Activity) {
            it.parent?.let { parent ->
                for (i in 0 until (parent as ViewGroup).childCount) {
                    val child = parent.getChildAt(i)
                    if (child?.id == R.id.in_app_message_tooltip_view && child.tag == id) {
                        scheduleRemoval(delay = delay, view = child as ViewGroup, id = id, activity = activity)
                        break
                    }
                }
            }
        }

        private fun removeAllTooltip(activity: Activity, delay: Int) {
            activity.findViewById<ViewGroup>(R.id.in_app_message_tooltip_view)?.parent?.let { viewParent ->
                val viewList = mutableListOf<View>()
                for (i in 0 until (viewParent as ViewGroup).childCount) {
                    val child = viewParent.getChildAt(i)
                    if (child?.id == R.id.in_app_message_tooltip_view) {
                        viewList.add(child)
                    }
                }
                for (view in viewList) {
                    scheduleRemoval(
                        delay = delay, view = view as ViewGroup, id = view.tag as String?,
                        activity = activity,
                    )
                }
            }
        }

        private fun scheduleRemoval(delay: Int, view: ViewGroup, id: String? = null, activity: Activity) {
            if (delay > 0) {
                // auto disappear processing for tooltips
                handler.postDelayed(
                    {
                        // confirm if view is still visible, for users may have already closed the campaign
                        // when the delay completes
                        if (isViewPresent(view, id)) {
                            removeCampaign(view, id, activity)
                            (view as? InAppMessagingTooltipView)?.listener?.onAutoDisappear()
                        }
                    }, delay * MS_MULTIPLIER,
                )
            } else {
                // to avoid crashing when redirect from tooltip view
                removeCampaign(view, id, activity)
            }
        }

        private fun isViewPresent(viewGroup: ViewGroup, id: String?): Boolean {
            var result = false
            val parent = viewGroup.parent as? ViewGroup ?: return result
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                if (child?.id == viewGroup.id && child.tag == id) {
                    result = true
                    break
                }
            }
            return result
        }

        private fun removeCampaign(inAppMessageBaseView: ViewGroup, id: String?, activity: Activity) {
            // Removing just the InApp Message from the view hierarchy.
            if (inAppMessageBaseView.parent !is ViewGroup) {
                // avoid crash
                return
            }

            val parent = inAppMessageBaseView.parent as ViewGroup
            if (parent.id == R.id.in_app_message_tooltip_layout) {
                removeTooltip(parent, id, activity)
            } else {
                parent.isFocusableInTouchMode = true
                parent.requestFocus()
                parent.removeView(inAppMessageBaseView)
            }

            InAppLogger(TAG).debug("View removed")
        }

        private fun removeTooltip(parent: ViewGroup, id: String?, activity: Activity) {
            val gp = parent.parent
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                if (child?.id == R.id.in_app_message_tooltip_view && child.tag == id) {
                    // remove target tooltip view
                    parent.removeView(child)
                    break
                }
            }
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
            val activity = HostAppInfoRepository.instance().getRegisteredActivity() ?: return
            activity.findViewById<FrameLayout>(R.id.in_app_message_tooltip_layout)?.let { frameLayout ->
                val removeList = mutableListOf<View>()
                for (i in 0 until frameLayout.childCount) {
                    val child = frameLayout.getChildAt(i)
                    if (child?.id == R.id.in_app_message_tooltip_view) {
                        addToList(child = child, activity = activity, parent = parent, removeList = removeList)
                    }
                }
                for (view in removeList) {
                    val viewId = view.tag as String?
                    if (viewId != null) {
                        scheduleRemoval(delay = 0, view = view as ViewGroup, id = viewId, activity = activity)
                    }
                }
            }
        }

        @SuppressWarnings("ReplaceSafeCallChainWithRun")
        private fun addToList(child: View, activity: Activity, parent: ViewGroup, removeList: MutableList<View>) {
            val message = CampaignRepository.instance().messages[child.tag as String]
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
