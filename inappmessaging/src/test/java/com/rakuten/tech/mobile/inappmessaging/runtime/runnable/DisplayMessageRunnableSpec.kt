package com.rakuten.tech.mobile.inappmessaging.runtime.runnable

import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.CampaignData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageMixerResponseSpec
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for DisplayMessageRunnable.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@SuppressWarnings("LargeClass")
class DisplayMessageRunnableSpec : BaseTest() {
    private val message = Mockito.mock(CampaignData::class.java)
    private val hostAppActivity = Mockito.mock(Activity::class.java)
    private val view = Mockito.mock(View::class.java)
    private val window = Mockito.mock(Window::class.java)
    private val payload = MessageMixerResponseSpec.response.data[0].campaignData.getMessagePayload()

    @Before
    override fun setup() {
        super.setup()
        `when`(view!!.id).thenReturn(12343254)
        `when`(hostAppActivity.window).thenReturn(window)
    }

    @Test
    fun `should not throw exception fo invalid message type`() {
        `when`(message.getType()).thenReturn(0)
        DisplayMessageRunnable(message, hostAppActivity).run()
    }

    @Test
    fun `should not throw exception fo invalid does not exist`() {
        `when`(message.getType()).thenReturn(100)
        DisplayMessageRunnable(message, hostAppActivity).run()
    }

    @Test(expected = NullPointerException::class)
    fun `should throw null pointer exception when modal`() {
        `when`(message.getType()).thenReturn(InAppMessageType.MODAL.typeId)
        DisplayMessageRunnable(message, hostAppActivity).run()
    }

    @Test(expected = NullPointerException::class)
    fun `should throw null pointer exception when fullscreen`() {
        `when`(message.getType()).thenReturn(InAppMessageType.FULL.typeId)
        DisplayMessageRunnable(message, hostAppActivity).run()
    }

    @Test
    fun `should throw exception with mock activity for full`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
        `when`(message.getType()).thenReturn(InAppMessageType.FULL.typeId)
        `when`(message.getMessagePayload()).thenReturn(payload)
        `when`(hostAppActivity
                .layoutInflater).thenReturn(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
        DisplayMessageRunnable(message, hostAppActivity).run()
    }

    @Test
    fun `should not throw exception with mock activity for modal`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
        `when`(message.getType()).thenReturn(InAppMessageType.MODAL.typeId)
        `when`(message.getMessagePayload()).thenReturn(payload)
        `when`(hostAppActivity
                .layoutInflater).thenReturn(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
        DisplayMessageRunnable(message, hostAppActivity).run()
    }

    @Test
    fun `should not throw exception with mock activity for slide`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
        `when`(message.getType()).thenReturn(InAppMessageType.SLIDE.typeId)
        `when`(message.getMessagePayload()).thenReturn(payload)
        `when`(hostAppActivity
                .layoutInflater).thenReturn(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
        DisplayMessageRunnable(message, hostAppActivity).run()
    }
}
