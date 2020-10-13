package com.rakuten.tech.mobile.inappmessaging.runtime.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.facebook.datasource.DataSource
import com.facebook.soloader.SoLoader
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ReadyForDisplayMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.CampaignData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.validateMockitoUsage
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

/**
 * Test class for DisplayMessageJobIntentService.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class DisplayMessageJobIntentServiceSpec : BaseTest() {

    private val intent = Mockito.mock(Intent::class.java)
    private val activity = Mockito.mock(Activity::class.java)
    private var serviceController: ServiceController<DisplayMessageJobIntentService>? = null
    private var displayMessageJobIntentService: DisplayMessageJobIntentService? = null
    private val mockMessageManager = Mockito.mock(MessageReadinessManager::class.java)

    @Before
    fun setup() {
        SoLoader.setInTestMode()
        MockitoAnnotations.initMocks(this)
        serviceController = Robolectric.buildService(DisplayMessageJobIntentService::class.java)
        displayMessageJobIntentService = serviceController?.bind()?.create()?.get()
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        serviceController!!.destroy()
        validateMockitoUsage()
    }

    @Test
    fun `should post message not throw exception`() {
        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        displayMessageJobIntentService!!.onHandleWork(intent!!)
    }

    @Test
    fun `should not throw exception with valid message`() {
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID, "test_device_id")
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test-key", "")
        InAppMessaging.instance().registerMessageDisplayActivity(activity)

        val message = Mockito.mock(Message::class.java)
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(listOf(message))

        When calling message.getCampaignId() itReturns "1"
        When calling message.isTest() itReturns true
        When calling message.getMaxImpressions() itReturns 10
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD.trimIndent(),
                MessagePayload::class.java)
        When calling activity
                .layoutInflater itReturns LayoutInflater.from(ApplicationProvider.getApplicationContext())
        displayMessageJobIntentService!!.onHandleWork(intent!!)
    }

    @Test
    fun `should not throw exception with valid message no url`() {
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID, "test_device_id")
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test-key", "")
        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        val message = Mockito.mock(Message::class.java)
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(listOf(message))

        When calling message.getCampaignId() itReturns "1"
        When calling message.isTest() itReturns true
        When calling message.getMaxImpressions() itReturns 10
        When calling activity
                .layoutInflater itReturns LayoutInflater.from(ApplicationProvider.getApplicationContext())
        displayMessageJobIntentService!!.onHandleWork(intent!!)
    }

// TODO: Fix tests crash when calling verifyCampaignContextsCallback
//
//    @Test
//    fun `should call verifyCampaignContextsCallback for non-test campaign with contexts`() {
//        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
//                Settings.Secure.ANDROID_ID, "test_device_id")
//        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test-key", "")
//        InAppMessaging.instance().registerMessageDisplayActivity(activity)
//
//        val message = Mockito.mock(Message::class.java)
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(listOf(message))
//        val callback = mock<(List<String>, String) -> Boolean>()
//        DisplayMessageJobIntentService.verifyCampaignContextsCallback = callback
//
//        When calling message.getCampaignId() itReturns "1"
//        When calling message.isTest() itReturns false
//        When calling message.getMaxImpressions() itReturns 1
//        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
//                MessagePayload::class.java)
//        When calling message.getContexts() itReturns listOf("ctx")
//        When calling mockMessageManager.getNextDisplayMessage() itReturns message
//        When calling activity
//                .layoutInflater itReturns LayoutInflater.from(ApplicationProvider.getApplicationContext())
//        displayMessageJobIntentService!!.messageReadinessManager = mockMessageManager
//        displayMessageJobIntentService!!.onHandleWork(intent!!)
//
//        Mockito.verify(callback, Mockito.times(1)).invoke(listOf("ctx"), "[ctx] Campaign Title")
//    }
//
//    @Test
//    fun `should not call verifyCampaignContextsCallback for non-test campaign without contexts`() {
//        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
//                Settings.Secure.ANDROID_ID, "test_device_id")
//        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test-key", "")
//        InAppMessaging.instance().registerMessageDisplayActivity(activity)
//
//        val message = Mockito.mock(Message::class.java)
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(listOf(message))
//        val callback = mock<(List<String>, String) -> Boolean>()
//        DisplayMessageJobIntentService.verifyCampaignContextsCallback = callback
//
//        When calling message.getCampaignId() itReturns "1"
//        When calling message.isTest() itReturns false
//        When calling message.getMaxImpressions() itReturns 1
//        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
//                MessagePayload::class.java)
//        When calling message.getContexts() itReturns listOf()
//        When calling mockMessageManager.getNextDisplayMessage() itReturns message
//        When calling activity
//                .layoutInflater itReturns LayoutInflater.from(ApplicationProvider.getApplicationContext())
//        displayMessageJobIntentService!!.messageReadinessManager = mockMessageManager
//        displayMessageJobIntentService!!.onHandleWork(intent!!)
//
//        Mockito.verify(callback, Mockito.times(0)).invoke(any(), any())
//    }
//
//    @Test
//    fun `should not call verifyCampaignContextsCallback for test campaign with contexts`() {
//        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
//                Settings.Secure.ANDROID_ID, "test_device_id")
//        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test-key", "")
//        InAppMessaging.instance().registerMessageDisplayActivity(activity)
//
//        val message = Mockito.mock(Message::class.java)
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(listOf(message))
//        val callback = mock<(List<String>, String) -> Boolean>()
//        DisplayMessageJobIntentService.verifyCampaignContextsCallback = callback
//
//        When calling message.getCampaignId() itReturns "1"
//        When calling message.isTest() itReturns true
//        When calling message.getMaxImpressions() itReturns 1
//        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
//                MessagePayload::class.java)
//        When calling message.getContexts() itReturns listOf("ctx")
//        When calling mockMessageManager.getNextDisplayMessage() itReturns message
//        When calling activity
//                .layoutInflater itReturns LayoutInflater.from(ApplicationProvider.getApplicationContext())
//        displayMessageJobIntentService!!.messageReadinessManager = mockMessageManager
//        displayMessageJobIntentService!!.onHandleWork(intent!!)
//
//        Mockito.verify(callback, Mockito.times(0)).invoke(any(), any())
//    }
//
//    @Test
//    fun `should call verifyCampaignContextsCallback with proper parameters`() {
//        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
//                Settings.Secure.ANDROID_ID, "test_device_id")
//        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test-key", "")
//        InAppMessaging.instance().registerMessageDisplayActivity(activity)
//
//        val message = Mockito.mock(Message::class.java)
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(listOf(message))
//        val callback = mock<(List<String>, String) -> Boolean>()
//        DisplayMessageJobIntentService.verifyCampaignContextsCallback = callback
//
//        When calling message.getCampaignId() itReturns "1"
//        When calling message.isTest() itReturns false
//        When calling message.getMaxImpressions() itReturns 1
//        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
//                MessagePayload::class.java)
//        When calling message.getContexts() itReturns listOf("ctx")
//        When calling mockMessageManager.getNextDisplayMessage() itReturns message
//        When calling activity
//                .layoutInflater itReturns LayoutInflater.from(ApplicationProvider.getApplicationContext())
//        displayMessageJobIntentService!!.messageReadinessManager = mockMessageManager
//        displayMessageJobIntentService!!.onHandleWork(intent!!)
//
//        argumentCaptor<List<String>>().apply {
//            Mockito.verify(callback).invoke(capture(), any())
//            firstValue shouldEqual listOf("ctx")
//        }
//        argumentCaptor<String>().apply {
//            Mockito.verify(callback).invoke(any(), capture())
//            firstValue shouldBeEqualTo "[ctx] Campaign Title"
//        }
//    }

    companion object {
        private const val MESSAGE_PAYLOAD = """
            {
                "backgroundColor":"#000000",
                "frameColor":"#ffffff",
                "header":"Test Header",
                "headerColor":"#ffffff",
                "messageBody":"Login Test",
                "messageBodyColor":"#ffffff",
                "messageSettings":{
                    "controlSettings":{
                        "buttons":[]
                    },
                    "displaySettings":{
                        "endTimeMillis":1584109800000,
                        "optOut":true,
                        "orientation":1,
                        "slideFrom":1,
                        "textAlign":2
                    }
                },
                "resource":{
                    "cropType":2,
                    "imageUrl":"https://sample.image.url/test.jpg"
                },
                "title":"[ctx] Campaign Title",
                "titleColor":"#000000"
            }
        """
        private const val MESSAGE_PAYLOAD_NO_URL = """
            {
                "backgroundColor":"#000000",
                "frameColor":"#ffffff",
                "header":"Test Header",
                "headerColor":"#ffffff",
                "messageBody":"Login Test",
                "messageBodyColor":"#ffffff",
                "messageSettings":{
                    "controlSettings":{
                        "buttons":[]
                    },
                    "displaySettings":{
                        "endTimeMillis":1584109800000,
                        "optOut":false,
                        "orientation":1,
                        "slideFrom":1,
                        "textAlign":2
                    }
                },
                "resource":{
                    "cropType":2
                },
                "title":"[ctx] Campaign Title",
                "titleColor":"#000000"
            }
        """
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ImagePrefetchSubscriberSpec {
    private var serviceController: ServiceController<DisplayMessageJobIntentService>? = null
    private var displayMessageJobIntentService: DisplayMessageJobIntentService? = null

    @Before
    fun setup() {
        SoLoader.setInTestMode()
        MockitoAnnotations.initMocks(this)
        serviceController = Robolectric.buildService(DisplayMessageJobIntentService::class.java)
        displayMessageJobIntentService = serviceController?.bind()?.create()?.get()
    }

    @Test
    fun `should not throw exception on new result`() {
        val message = Mockito.mock(CampaignData::class.java)
        val activity = Mockito.mock(Activity::class.java)
        val dataSource = Mockito.mock(DataSource::class.java)
        When calling dataSource.progress itReturns 1f
        displayMessageJobIntentService?.ImagePrefetchSubscriber(message, activity)
                ?.onNewResult(dataSource as DataSource<Void>?)
    }

    @Test
    fun `should not throw exception on failed result`() {
        val message = Mockito.mock(CampaignData::class.java)
        val activity = Mockito.mock(Activity::class.java)
        val dataSource = Mockito.mock(DataSource::class.java)
        When calling dataSource.progress itReturns 1f
        displayMessageJobIntentService?.ImagePrefetchSubscriber(message, activity)
                ?.onFailure(dataSource as DataSource<Void>?)
    }
}
