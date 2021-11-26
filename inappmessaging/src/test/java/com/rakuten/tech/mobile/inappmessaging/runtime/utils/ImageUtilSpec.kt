package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class ImageUtilSpec {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun before() {
        initializePicassoInstance()
    }

    @Test
    fun `should not load an image from invalid url`() = runBlocking {
        withContext(Dispatchers.IO) {
            ImageUtil.fetchBitmap("invalid-url")
        }.shouldBeNull()
    }

    @Test
    fun `should load an image`(): Unit = runBlocking {
        withContext(Dispatchers.IO) {
            ImageUtil.fetchBitmap(VALID_URL)
        }.shouldNotBeNull()
    }

    private fun initializePicassoInstance() {
        try {
            val picasso = Picasso.Builder(context).build()
            picasso.isLoggingEnabled = true
            Picasso.setSingletonInstance(picasso)
        } catch (ignored: IllegalStateException) {
            // Picasso instance was already initialized
        }
    }

    companion object {
        internal const val VALID_URL =
            "https://en.wikipedia.org/wiki/Android_(operating_system)#/media/File:Android-robot-googleplex-2008.jpg"
    }
}
