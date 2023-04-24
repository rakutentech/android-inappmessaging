package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.view.View
import androidx.core.content.res.ResourcesCompat

internal object ResourceUtils {
    internal var mockFont: Typeface? = null

    fun getResourceIdentifier(context: Context, name: String, type: String) =
        context.resources.getIdentifier(name, type, context.packageName)

    @SuppressLint("NewApi")
    fun getFont(context: Context, id: Int) = when {
        id <= 0 -> null
        BuildVersionChecker.isAndroidOAndAbove() -> context.resources.getFont(id)
        else -> mockFont ?: ResourcesCompat.getFont(context, id)
    }

    fun <T : View> findViewByName(activity: Activity, name: String): T? {
        val id = getResourceIdentifier(activity, name, "id")
        if (id > 0) {
            return activity.findViewById(id)
        }
        return null
    }
}
