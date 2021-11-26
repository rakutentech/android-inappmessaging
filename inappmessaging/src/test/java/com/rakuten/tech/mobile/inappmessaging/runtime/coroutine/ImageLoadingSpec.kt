package com.rakuten.tech.mobile.inappmessaging.runtime.coroutine

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Resource
import com.rakuten.tech.mobile.inappmessaging.runtime.service.DisplayMessageJobIntentService
import com.squareup.picasso.Picasso
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.IllegalStateException

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class ImageLoadingSpec {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val mockImageLoaderCoroutine = spy(ImageLoaderCoroutine(Dispatchers.IO))

    @Before
    fun before() {
        initializePicassoInstance()
    }

    @Test
    fun `should not load an image from invalid url`() = runBlocking {
        val bitmap: Bitmap? = mockImageLoaderCoroutine.fetch("invalid-url")
        assertNull(bitmap)
    }

    @Test
    fun `should load an image`() = runBlocking {
        val imageUrl =
        "https://en.wikipedia.org/wiki/Android_(operating_system)#/media/File:Android-robot-googleplex-2008.jpg"
        val bitmap: Bitmap? = mockImageLoaderCoroutine.fetch(imageUrl)
        assertNotNull(bitmap)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `should call fetch image` () = runBlockingTest {
        val serviceController = Robolectric.buildService(DisplayMessageJobIntentService::class.java)
        val displayMessageJobIntentService = serviceController?.bind()?.create()?.get()
        displayMessageJobIntentService?.imageLoader = mockImageLoaderCoroutine
        val imageUrl =
            "https://en.wikipedia.org/wiki/Android_(operating_system)#/media/File:Android-robot-googleplex-2008.jpg"
        val message = setupMessageWithImage(imageUrl)
        val activity = Mockito.mock(Activity::class.java)
        displayMessageJobIntentService?.fetchImageThenDisplayMessage(message, activity, imageUrl)

        verify(mockImageLoaderCoroutine).fetch(ArgumentMatchers.anyString())
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

    private fun setupMessageWithImage(imageUrl: String): Message {
        val message = Mockito.mock(Message::class.java)
        val payload = Mockito.mock(MessagePayload::class.java)
        val resource = Mockito.mock(Resource::class.java)

        Mockito.`when`(resource.imageUrl).thenReturn(imageUrl)
        Mockito.`when`(payload.resource).thenReturn(resource)
        Mockito.`when`(message.getMessagePayload()).thenReturn(payload)
        Mockito.`when`(message.getCampaignId()).thenReturn("1")
        Mockito.`when`(message.isTest()).thenReturn(true)
        Mockito.`when`(message.getMaxImpressions()).thenReturn(1)
        Mockito.`when`(message.getMessagePayload()).thenReturn(payload)
        Mockito.`when`(message.getContexts()).thenReturn(listOf("ctx"))
        return message
    }
}
