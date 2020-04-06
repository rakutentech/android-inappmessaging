package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType

/**
 * Utility methods for views.
 */
internal object ViewUtil {

    /**
     * This method returns an android.view.animation.Animation object which includes InAppMessage's custom
     * animation.
     */
    fun getSlidingAnimation(
        context: Context,
        direction: SlideFromDirectionType
    ): Animation {
        return when (direction) {
            SlideFromDirectionType.RIGHT -> AnimationUtils.loadAnimation(context, R.anim.slide_from_right)
            SlideFromDirectionType.LEFT -> AnimationUtils.loadAnimation(context, R.anim.slide_from_left)
            SlideFromDirectionType.BOTTOM -> AnimationUtils.loadAnimation(context, R.anim.slide_from_bottom)
            else -> AnimationUtils.loadAnimation(context, R.anim.slide_from_bottom)
        }
    }
}
