package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat

internal object ResourceUtils {
    internal var mockFont: Typeface? = null

    fun getResourceIdentifier(context: Context, name: String, type: String) =
        context.resources.getIdentifier(name, type, context.packageName)

    @SuppressLint("NewApi")
    fun getFont(context: Context, id: Int) = when {
        id <= 0 -> null
        BuildVersionChecker.instance().isAndroidOAndAbove() -> context.resources.getFont(id)
        else -> mockFont ?: ResourcesCompat.getFont(context, id)
    }
}
