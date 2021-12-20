package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

internal object ImageUtil {

    fun fetchImage(imageUrl: String, callback: Callback, context: Context, picasso: Picasso? = null) {
        (picasso ?: Picasso.get()).load(imageUrl).priority(Picasso.Priority.HIGH)
            .resize(ViewUtil.getDisplayWidth(context), 0).fetch(callback)
    }
}
