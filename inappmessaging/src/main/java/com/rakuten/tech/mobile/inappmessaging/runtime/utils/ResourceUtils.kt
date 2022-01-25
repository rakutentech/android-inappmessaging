package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.res.ResourcesCompat

internal object ResourceUtils {
    fun getResourceIdentifier(context: Context, name: String, type: String) =
        context.resources.getIdentifier(name, type, context.packageName)

    @SuppressLint("NewApi")
    fun getFont(context: Context, id: Int) = when {
        id <= 0 -> null
        BuildVersionChecker.instance().isAndroidOAndAbove() -> context.resources.getFont(id)
        else -> ResourcesCompat.getFont(context, id)
    }
}
