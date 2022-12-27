package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.Window
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
    fun getPosition(
        activity: Activity,
        anchorView: View,
        type: PositionType,
        tImageView: View,
        tTipView: View,
        tCloseBtnView: View,
    ): Pair<Int, Int> {

        val anchorLocation = IntArray(2)
        anchorView.getLocationInWindow(anchorLocation) // location (x, y) based on the activity window

        val actionBarHeight = activity.window.findViewById<View?>(Window.ID_ANDROID_CONTENT).top
        var left = anchorLocation[0]
        var top = anchorLocation[1] - actionBarHeight

        Logger(TAG).debug("Anchor position: ($left, $top)")
        when (type) {
            PositionType.TOP_LEFT -> {
                left -= (tImageView.layoutParams.width + tCloseBtnView.layoutParams.width + tTipView.layoutParams.width/2)   // target view left and tooltip width
                top -= (anchorView.height + tImageView.layoutParams.height + tTipView.layoutParams.height/2)
            }
            PositionType.TOP_CENTER -> {
                left += (anchorView.width - tImageView.layoutParams.width)/2
                top -= (anchorView.height + tImageView.layoutParams.height + tTipView.layoutParams.height)
            }
            PositionType.TOP_RIGHT -> {
                left += (anchorView.width + tTipView.layoutParams.width/2)
                top -= (anchorView.height + tImageView.layoutParams.height + tTipView.layoutParams.height/2)
            }
            PositionType.RIGHT -> {
                left += anchorView.width
                top -= (anchorView.height + tImageView.layoutParams.height)/2
            }
            PositionType.BOTTOM_RIGHT -> {
                left += (anchorView.width + tTipView.layoutParams.width/2)
                top += (anchorView.height - tCloseBtnView.layoutParams.width - tTipView.layoutParams.height)
            }
            PositionType.BOTTOM_CENTER -> {
                left += (anchorView.width - tImageView.layoutParams.width)/2
                top += (anchorView.height - tCloseBtnView.layoutParams.width - tTipView.layoutParams.height)
            }
            PositionType.BOTTOM_LEFT -> {
                left -= (tImageView.layoutParams.width + tCloseBtnView.layoutParams.width + tTipView.layoutParams.width/2)
                top += (anchorView.height - tCloseBtnView.layoutParams.width - tTipView.layoutParams.height)
            }
            PositionType.LEFT -> {
                // tooltipImageView.layoutParams includes padding (border)
                // in vertical view, only consider the center of imageView
                left -= (tImageView.layoutParams.width + tCloseBtnView.layoutParams.width + tTipView.layoutParams.width)
                top -= (anchorView.height + tImageView.layoutParams.height)/2
            }
        }
        Logger(TAG).debug("Tooltip position: ($left, $top)")
        return Pair(left, top)
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
