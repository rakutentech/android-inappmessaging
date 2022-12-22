package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.PositionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessagingTooltipView.Companion.PADDING
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessagingTooltipView.Companion.TRI_SIZE
import com.rakuten.tech.mobile.sdkutils.logger.Logger

/**
 * Utility methods for views.
 */
@SuppressWarnings("MagicNumber", "TooManyFunctions")
internal object ViewUtil {
    private const val TAG = "IAM_ViewUtil"

    /**
     * This method returns an android.view.animation.Animation object which includes InAppMessage's custom
     * animation.
     */
    @SuppressWarnings("SwallowedException", "ElseCaseInsteadOfExhaustiveWhen")
    fun getSlidingAnimation(
        context: Context,
        direction: SlideFromDirectionType
    ): Animation? {
        return try {
            when (direction) {
                SlideFromDirectionType.RIGHT -> AnimationUtils.loadAnimation(context, R.anim.slide_from_right)
                SlideFromDirectionType.LEFT -> AnimationUtils.loadAnimation(context, R.anim.slide_from_left)
                SlideFromDirectionType.BOTTOM -> AnimationUtils.loadAnimation(context, R.anim.slide_from_bottom)
                else -> AnimationUtils.loadAnimation(context, R.anim.slide_from_bottom)
            }
        } catch (e: Resources.NotFoundException) {
            // should never occur unless there is an issue with the animation resource format
            null
        }
    }

    /**
     * Returns the width of the available display size.
     * @param context
     * @return Width of the available display size in pixel.
     */
    fun getDisplayWidth(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics

        // add 1 pixel to fill the entire space into the message's ImageView
        return displayMetrics.widthPixels + 1
    }

    @SuppressWarnings("LongParameterList")
    fun getPosition(view: View, type: PositionType, width: Int, height: Int, marginH: Int, marginV: Int):
        Pair<Int, Int> {
        val rect = getViewBoundsRelativeToRoot(view)
        Logger(TAG).debug("Target position: ${rect.left}-${rect.top}-${rect.right}-${rect.bottom}")
        return when (type) {
            PositionType.TOP_RIGHT -> Pair(getRightPos(rect, view), getTopPos(rect, height, marginV))
            PositionType.TOP_CENTER -> Pair(getCenterPos(rect, view, width), getTopPos(rect, height, marginV))
            PositionType.TOP_LEFT -> Pair(getLeftPos(rect, width, marginH), getTopPos(rect, height, marginV))
            PositionType.BOTTOM_RIGHT -> Pair(getRightPos(rect, view), getBottomPos(rect, view, marginV))
            PositionType.BOTTOM_CENTER -> Pair(getCenterPos(rect, view, width), rect.top + view.height)
            PositionType.BOTTOM_LEFT -> Pair(getLeftPos(rect, width, marginH), getBottomPos(rect, view, marginV))
            PositionType.RIGHT -> Pair(rect.left + view.width - TRI_SIZE / 2, getSidePos(rect, height, marginV))
            PositionType.LEFT -> Pair(getLeftPos(rect, width, marginH), getSidePos(rect, height, marginV))
        }
    }

    private fun getSidePos(rect: Rect, height: Int, marginV: Int) = rect.top - (height - marginV + TRI_SIZE) / 2

    private fun getCenterPos(rect: Rect, view: View, width: Int) = rect.left + view.width / 2 - width / 2

    private fun getRightPos(rect: Rect, view: View) = rect.left + view.width + PADDING / 4

    private fun getLeftPos(rect: Rect, width: Int, marginH: Int) = rect.left - width - marginH - TRI_SIZE / 2

    private fun getBottomPos(rect: Rect, view: View, marginV: Int) = rect.top + view.height - marginV + TRI_SIZE / 2

    private fun getTopPos(rect: Rect, height: Int, marginV: Int) = rect.top - height - marginV - TRI_SIZE / 2

    /**
     * Gets the view's position relative to the root layout
     */
    private fun getViewBoundsRelativeToRoot(view: View): Rect {
        val bounds = Rect()
        // visible bounds
        view.getDrawingRect(bounds)
        // calculates the relative coordinates to the parent
        // the parentViewGroup can be any parent in the view hierarchy, so it also works with ScrollView
        (view.parent as? ViewGroup)?.offsetDescendantRectToMyCoords(view, bounds)
        return bounds
    }

    fun getEdgePosition(width: Int, height: Int, topPos: Pair<Int, Int>): Pair<Int?, Int?> {
        val windowW = Resources.getSystem().displayMetrics.widthPixels
        val windowH = Resources.getSystem().displayMetrics.heightPixels
        val right = if (windowW < (topPos.first + width)) {
            -(width - (windowW - topPos.first)) - PADDING - TRI_SIZE
        } else {
            null
        }

        val bottom = if (windowH < (topPos.second + height)) {
            -(height - (windowH - topPos.second)) - PADDING - TRI_SIZE
        } else {
            null
        }
        return Pair(right, bottom)
    }

    @SuppressWarnings("SwallowedException")
    fun getScrollView(view: View): ViewGroup? {
        var currView = view.parent
        while (currView != null) {
            if (currView is ScrollView || currView is NestedScrollView || currView is SwipeRefreshLayout) {
                return currView as ViewGroup
            }

            currView = currView.parent
        }
        return null
    }

    fun getFrameLayout(view: View): FrameLayout? {
        var currView = view.parent
        while (currView != null) {
            if (currView is FrameLayout) {
                return currView
            }

            currView = currView.parent
        }
        return null
    }

    fun getFirstLayoutChild(group: ViewGroup): Int {
        var layoutIdx = -1
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i)
            if (child is ViewGroup) {
                layoutIdx = i
                break
            }
        }

        return layoutIdx
    }

    fun getToolBarIndex(parent: FrameLayout): Int {
        var idx = parent.childCount

        for (i in 0 until parent.childCount) {
            if (parent.getChildAt(i) is Toolbar) {
                idx = i
            }
        }

        return idx
    }

    fun isViewVisible(view: View): Boolean {
        val scrollView = getScrollView(view)
        return if (scrollView != null) {
            val scrollBounds = Rect()
            scrollView.getHitRect(scrollBounds)
            view.getLocalVisibleRect(scrollBounds)
        } else {
            true
        }
    }
}
