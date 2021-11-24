package com.rakuten.tech.mobile.inappmessaging.runtime.coroutine

import android.graphics.Bitmap
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

internal class ImageLoaderCoroutine(
    private val coroutineContext: CoroutineContext = Dispatchers.IO,
    private val isTest: Boolean = false
) {
    @SuppressWarnings("TooGenericExceptionCaught")
    fun fetch(imageUrl: String): Bitmap? {
        return if (isTest) {
            runBlocking(coroutineContext) {
                fetchBitmap(imageUrl)
            }
        } else {
            fetchBitmap(imageUrl)
        }
    }

    private fun fetchBitmap(imageUrl: String): Bitmap? {
        return try {
            Picasso.get().load(imageUrl).priority(Picasso.Priority.HIGH).get()
        } catch (e: Exception) {
            Timber.tag(TAG).d(e, "Error on loading image $imageUrl")
            null
        }
    }

    companion object {
        private const val TAG = "IAM_ImageLoader"
    }
}
