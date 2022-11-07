package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.view.MotionEvent
import android.view.View
import android.widget.ScrollView
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager

/**
 * Custom on touch listener to handle displaying of tooltip campaign on scrollable views.
 */
abstract class CustomOnTouchListener : View.OnTouchListener {

    internal var displayManager = DisplayManager.instance()

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP ->
                if (v is ScrollView) {
                    // remove tooltip if target was hidden
                    displayManager.removeHiddenTargets(v)
                    // run another display image
                    displayManager.displayMessage()
                }
        }
        return v.performClick()
    }
}
