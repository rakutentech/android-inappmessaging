package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.PositionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
import com.rakuten.tech.mobile.inappmessaging.runtime.extensions.getRectLocationOnContainer
import com.rakuten.tech.mobile.inappmessaging.runtime.extensions.isVisible
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessagingTooltipView.Companion.TRI_SIZE

/**
 * Utility methods for views.
 */
@SuppressWarnings("MagicNumber")
internal object ViewUtil {
    private const val TAG = "IAM_ViewUtil"

    /**
     * This method returns an android.view.animation.Animation object which includes InAppMessage's custom
     * animation.
     */
    @SuppressWarnings("SwallowedException", "ElseCaseInsteadOfExhaustiveWhen")
    fun getSlidingAnimation(context: Context, direction: SlideFromDirectionType): Animation? {
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

    @SuppressWarnings("LongMethod")
    fun getTooltipPosition(
        container: ViewGroup,
        view: View,
        anchorView: View,
        positionType: PositionType,
        margin: Int,
    ): Point {
        val location = Point()

        val anchorRect: Rect = anchorView.getRectLocationOnContainer(container)
        val anchorCenter = Point(anchorRect.centerX(), anchorRect.centerY())

        when (positionType) {
            PositionType.TOP_CENTER -> {
                location.x = anchorCenter.x - (view.width - margin) / 2
                location.y = anchorRect.top - view.height
            }
            PositionType.RIGHT -> {
                location.x = anchorRect.right
                location.y = anchorCenter.y - (view.height + margin) / 2
            }
            PositionType.BOTTOM_CENTER -> {
                location.x = anchorCenter.x - (view.width - margin) / 2
                location.y = anchorRect.bottom
            }
            PositionType.LEFT -> {
                location.x = anchorRect.left - view.width
                location.y = anchorCenter.y - (view.height + margin) / 2
            }

            PositionType.TOP_LEFT -> {
                location.x = anchorRect.left - (view.width + TRI_SIZE / 2)
                location.y = anchorRect.top - (view.height + TRI_SIZE / 2)
            }
            PositionType.TOP_RIGHT -> {
                location.x = anchorRect.right + (TRI_SIZE / 2)
                location.y = anchorRect.top - view.height - (TRI_SIZE / 2)
            }
            PositionType.BOTTOM_RIGHT -> {
                location.x = anchorRect.right + (TRI_SIZE / 2)
                location.y = anchorRect.bottom - margin + (TRI_SIZE / 2)
            }
            PositionType.BOTTOM_LEFT -> {
                location.x = anchorRect.left - view.width - (TRI_SIZE / 2)
                location.y = anchorRect.bottom - margin + (TRI_SIZE / 2)
            }
        }
        return location
    }

    fun getScrollView(view: View): ViewGroup? {
        var currView = view.parent
        while (currView != null) {
            if (currView is ScrollView || currView is NestedScrollView) {
                return currView as? ViewGroup
            }

            currView = currView.parent
        }
        return null
    }

    fun isViewByNameVisible(activity: Activity, name: String, resourceUtil: ResourceUtils? = null): Boolean {
        val view = (resourceUtil ?: ResourceUtils).findViewByName<View>(activity, name)
        return view?.isVisible() ?: false
    }
}
