package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.PositionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessagingTooltipView

/**
 * Utility methods for views.
 */
internal object ViewUtil {

    /**
     * This method returns an android.view.animation.Animation object which includes InAppMessage's custom
     * animation.
     */
    @SuppressWarnings("SwallowedException")
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

    fun getLayoutPosition(view: View, posType: PositionType, width: Int, height: Int, marginHorizontal: Int, marginVertical: Int): Pair<Int, Int>? {
//        val location = IntArray(2)
//        view.getLocationOnScreen(location)
        val rect = Rect()
        view.getHitRect(rect)
        return when (posType) {
            PositionType.TOP_RIGHT -> {
                Pair(rect.left + view.width + InAppMessagingTooltipView.PADDING/4, rect.top - height)
            }
            PositionType.TOP_CENTER -> {
                Pair(rect.left + view.width/2 - width/2, rect.top - height)
            }
            PositionType.TOP_LEFT -> {
                Pair(rect.left - width, rect.top - height)
            }
            PositionType.BOTTOM_RIGHT -> {
                Pair(rect.left + view.width + InAppMessagingTooltipView.PADDING/4, rect.top + view.height)
            }
            PositionType.BOTTOM_CENTER -> {
                Pair(rect.left + view.width/2 - width/2, rect.top + view.height - InAppMessagingTooltipView.TRI_SIZE/2)
            }
            PositionType.BOTTOM_LEFT -> {
                Pair(rect.left - width, rect.top + view.height)
            }
            PositionType.RIGHT -> {
                Pair(rect.left + view.width - InAppMessagingTooltipView.TRI_SIZE/2, rect.top - height/2 + marginVertical/2 + InAppMessagingTooltipView.PADDING/2)
            }
            PositionType.LEFT -> {
                Pair(rect.left - width - InAppMessagingTooltipView.TRI_SIZE/2, rect.top - height/2 + marginVertical/2 + InAppMessagingTooltipView.PADDING/2)
            }
        }
    }

    fun isInScrollView(view: View): Boolean {
        var currView = view.parent
        while (currView != null) {
            val group = try {
                currView as ViewGroup
            } catch (ex: ClassCastException) {
                null
            }
            group?.clipToPadding = false
            group?.clipChildren = false
            if (currView is ScrollView || currView is NestedScrollView) {
                return true
            }
            currView = currView.parent
        }
        return false
    }

//    /**
//     * Returns the corresponding height to the available display size while keeping the aspect ratio.
//     * @param context
//     * @param width The given view's width.
//     * @param height The given view's height.
//     * @return The height of the available display size in pixels.
//     */
//    fun getDisplayHeight(context: Context, width: Int, height: Int): Int {
//        val displayWidth = getDisplayWidth(context)
//        val aspectRationFactor = displayWidth / width.toFloat()
//        return (height * aspectRationFactor).toInt()
//    }
}
