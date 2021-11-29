package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.graphics.Bitmap
import com.squareup.picasso.Picasso
import timber.log.Timber
import java.lang.Exception

internal object ImageUtil {
    private const val TAG = "IAM_ImageUtil"

    @SuppressWarnings("TooGenericExceptionCaught")
    fun fetchBitmap(imageUrl: String): Bitmap? =
        try {
            Picasso.get().load(imageUrl).priority(Picasso.Priority.HIGH).get()
        } catch (e: Exception) {
            Timber.tag(TAG).d(e, "Error on loading image $imageUrl")
            null
        }
}
