package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.PositionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessagingTooltipView.Companion.PADDING
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessagingTooltipView.Companion.TRI_SIZE

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
                SlideFromDirectionType.TOP, SlideFromDirectionType.INVALID ->
                    AnimationUtils.loadAnimation(context, R.anim.slide_from_bottom)
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

    @SuppressWarnings("LongParameterList", "MagicNumber")
    fun getPosition(view: View, type: PositionType, width: Int, height: Int, marginH: Int, marginV: Int):
            Pair<Int, Int> {
        val rect = Rect()
        view.getHitRect(rect)
        return when (type) {
            PositionType.TOP_RIGHT -> Pair(rect.left + view.width + PADDING / 4, rect.top - height - marginV)
            PositionType.TOP_CENTER -> Pair(rect.left + view.width / 2 - width / 2, rect.top - height - marginV)
            PositionType.TOP_LEFT -> Pair(rect.left - width - marginH, rect.top - height - marginV)
            PositionType.BOTTOM_RIGHT -> Pair(rect.left + view.width + PADDING / 4, rect.top + view.height - marginV)
            PositionType.BOTTOM_CENTER -> Pair(rect.left + view.width / 2 - width / 2,
                rect.top + view.height - TRI_SIZE / 2)
            PositionType.BOTTOM_LEFT -> Pair(rect.left - width - marginH, rect.top + view.height - marginV)
            PositionType.RIGHT -> Pair(rect.left + view.width - TRI_SIZE / 2,
                rect.top - (height - marginV + PADDING) / 2)
            PositionType.LEFT -> Pair(rect.left - width - marginH - TRI_SIZE / 2,
                rect.top - (height - marginV + PADDING) / 2)
        }
    }

    @SuppressWarnings("SwallowedException")
    fun getScrollView(view: View): FrameLayout? {
        var currView = view.parent
        while (currView != null) {
            if (currView is ScrollView || currView is NestedScrollView) {
                return currView as FrameLayout
            }
            currView = currView.parent
        }
        return null
    }
}
