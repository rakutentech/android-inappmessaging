package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.view.MotionEvent
import android.view.View
import android.widget.ScrollView
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager

/**
 * Custom on touch listener to handle displaying of tooltip campaign on scrollable views.
 */
interface CustomOnTouchListener : View.OnTouchListener {
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (v is ScrollView) {
                    // remove tooltip if target was hidden
                    DisplayManager.instance().removeHiddenTargets(v)
                    // run another display image
                    DisplayManager.instance().displayMessage()
                }
            }
        }
        return false
    }
}
