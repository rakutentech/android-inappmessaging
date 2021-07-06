package com.rakuten.tech.mobile.inappmessaging.runtime.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.facebook.datasource.DataSource
import com.facebook.soloader.SoLoader
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalDisplayedMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ReadyForDisplayMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.CampaignData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.validateMockitoUsage
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

/**
 * Test class for DisplayMessageJobIntentService.
 */
@SuppressWarnings("LargeClass")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class DisplayMessageJobIntentServiceSpec : BaseTest() {

    private val intent = Mockito.mock(Intent::class.java)
    private val activity = Mockito.mock(Activity::class.java)
    private var serviceController: ServiceController<DisplayMessageJobIntentService>? = null
    private var displayMessageJobIntentService: DisplayMessageJobIntentService? = null
    private val mockMessageManager = Mockito.mock(MessageReadinessManager::class.java)
    private var mockLocalDisplayRepo = Mockito.mock(LocalDisplayedMessageRepository::class.java)
    private var mockReadyForDisplayRepo = Mockito.mock(ReadyForDisplayMessageRepository::class.java)
    private val onVerifyContexts = Mockito.mock(InAppMessaging.instance().onVerifyContext.javaClass)
    private val configResponseData = Mockito.mock(ConfigResponseData::class.java)

    @Before
    override fun setup() {
        super.setup()
        SoLoader.setInTestMode()
        serviceController = Robolectric.buildService(DisplayMessageJobIntentService::class.java)
        displayMessageJobIntentService = serviceController?.bind()?.create()?.get()
        displayMessageJobIntentService!!.messageReadinessManager = mockMessageManager
        displayMessageJobIntentService!!.localDisplayRepo = mockLocalDisplayRepo
        displayMessageJobIntentService!!.readyMessagesRepo = mockReadyForDisplayRepo
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        When calling activity.layoutInflater itReturns LayoutInflater.from(ApplicationProvider.getApplicationContext())

        When calling configResponseData.rollOutPercentage itReturns 100
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID, "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        InAppMessaging.instance().registerMessageDisplayActivity(activity)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        ConfigResponseRepository.resetInstance()
        serviceController!!.destroy()
        validateMockitoUsage()
    }

    @Test
    fun `should post message not throw exception`() {
        displayMessageJobIntentService!!.onHandleWork(intent!!)
    }

    @Test
    fun `should not throw exception with valid message`() {
        val message = Mockito.mock(Message::class.java)
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(listOf(message))

        When calling message.getCampaignId() itReturns "1"
        When calling message.isTest() itReturns true
        When calling message.getMaxImpressions() itReturns 10
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD.trimIndent(),
                MessagePayload::class.java)
        displayMessageJobIntentService!!.onHandleWork(intent!!)
    }

    @Test
    fun `should not throw exception with valid message no url`() {
        val message = Mockito.mock(Message::class.java)
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(listOf(message))

        When calling message.getCampaignId() itReturns "1"
        When calling message.isTest() itReturns true
        When calling message.getMaxImpressions() itReturns 10
        displayMessageJobIntentService!!.onHandleWork(intent!!)
    }

    @Test
    fun `should call onVerifyContext for non-test campaign with contexts`() {
        val message = Mockito.mock(Message::class.java)

        When calling onVerifyContexts.invoke(any(), any()) itReturns true
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        When calling message.getCampaignId() itReturns "1"
        When calling message.isTest() itReturns false
        When calling message.getMaxImpressions() itReturns 1
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
                MessagePayload::class.java)
        When calling message.getContexts() itReturns listOf("ctx")
        When calling mockMessageManager.getNextDisplayMessage() itReturns message
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        Mockito.verify(onVerifyContexts).invoke(listOf("ctx"), "Campaign Title")
    }

    @Test
    fun `should not call onVerifyContext for non-test campaign without contexts`() {
        val message = Mockito.mock(Message::class.java)

        When calling onVerifyContexts.invoke(any(), any()) itReturns true
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        When calling message.getCampaignId() itReturns "1"
        When calling message.isTest() itReturns false
        When calling message.getMaxImpressions() itReturns 1
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
                MessagePayload::class.java)
        When calling message.getContexts() itReturns listOf()
        When calling mockMessageManager.getNextDisplayMessage() itReturns message
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        Mockito.verify(onVerifyContexts, never()).invoke(any(), any())
    }

    @Test
    fun `should not call onVerifyContext for test campaign with contexts`() {
        val message = Mockito.mock(Message::class.java)

        When calling onVerifyContexts.invoke(any(), any()) itReturns true
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        When calling message.getCampaignId() itReturns "1"
        When calling message.isTest() itReturns true
        When calling message.getMaxImpressions() itReturns 1
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
                MessagePayload::class.java)
        When calling message.getContexts() itReturns listOf("ctx")
        When calling mockMessageManager.getNextDisplayMessage() itReturns message
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        Mockito.verify(onVerifyContexts, never()).invoke(any(), any())
    }

    @SuppressWarnings("LongMethod")
    @Test
    fun `should call onVerifyContext with proper parameters`() {
        val message = Mockito.mock(Message::class.java)

        When calling onVerifyContexts.invoke(any(), any()) itReturns true
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        When calling message.getCampaignId() itReturns "1"
        When calling message.isTest() itReturns false
        When calling message.getMaxImpressions() itReturns 1
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
                MessagePayload::class.java)
        When calling message.getContexts() itReturns listOf("ctx")
        When calling mockMessageManager.getNextDisplayMessage() itReturns message
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        argumentCaptor<List<String>>().apply {
            Mockito.verify(onVerifyContexts).invoke(capture(), any())
            firstValue shouldBeEqualTo listOf("ctx")
        }
        argumentCaptor<String>().apply {
            Mockito.verify(onVerifyContexts).invoke(any(), capture())
            firstValue shouldBeEqualTo "Campaign Title"
        }
    }

    @Test
    fun `should call getMessagePayload again when message's context was rejected`() {
        val message = Mockito.mock(Message::class.java)

        When calling onVerifyContexts.invoke(any(), any()) itReturns false
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        When calling message.getCampaignId() itReturns "1"
        When calling message.isTest() itReturns false
        When calling message.getMaxImpressions() itReturns 1
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
                MessagePayload::class.java)
        When calling message.getContexts() itReturns listOf("ctx")
        When calling mockMessageManager.getNextDisplayMessage() itReturns message itReturns null
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        Mockito.verify(mockMessageManager, Mockito.times(2)).getNextDisplayMessage()
    }

    @Test
    fun `should not add message to LocalDisplayedMessageRepository when its context was rejected`() {
        val message = Mockito.mock(Message::class.java)

        When calling onVerifyContexts.invoke(any(), any()) itReturns false
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        When calling message.getCampaignId() itReturns "1"
        When calling message.isTest() itReturns false
        When calling message.getMaxImpressions() itReturns 1
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
                MessagePayload::class.java)
        When calling message.getContexts() itReturns listOf("ctx")
        When calling mockMessageManager.getNextDisplayMessage() itReturns message itReturns null
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        Mockito.verify(mockLocalDisplayRepo, never()).addMessage(any())
    }

    @Test
    fun `should remove message from ReadyForDisplayMessageRepository when its context was rejected`() {
        val message = Mockito.mock(Message::class.java)

        When calling onVerifyContexts.invoke(any(), any()) itReturns false
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        When calling message.getCampaignId() itReturns "1"
        When calling message.isTest() itReturns false
        When calling message.getMaxImpressions() itReturns 1
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
                MessagePayload::class.java)
        When calling message.getContexts() itReturns listOf("ctx")
        When calling mockMessageManager.getNextDisplayMessage() itReturns message itReturns null
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        argumentCaptor<String>().apply {
            Mockito.verify(mockReadyForDisplayRepo).removeMessage(capture(), eq(true))
            firstValue shouldBeEqualTo message.getCampaignId()
        }
    }

    @Test
    fun `should not crash when campaign id is null`() {
        val message = Mockito.mock(Message::class.java)

        When calling onVerifyContexts.invoke(any(), any()) itReturns false
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        When calling message.getCampaignId() itReturns null
        When calling message.isTest() itReturns false
        When calling message.getMaxImpressions() itReturns 1
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
                MessagePayload::class.java)
        When calling message.getContexts() itReturns listOf("ctx")
        When calling mockMessageManager.getNextDisplayMessage() itReturns message itReturns null
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        argumentCaptor<String>().apply {
            Mockito.verify(mockReadyForDisplayRepo).removeMessage(capture(), eq(true))
            firstValue shouldBeEqualTo ""
        }
    }

    @Test
    fun `should display campaign if onVerifyContext was not set (default value)`() {
        val message = Mockito.mock(Message::class.java)

        When calling message.getCampaignId() itReturns "1"
        When calling message.isTest() itReturns false
        When calling message.getMaxImpressions() itReturns 1
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
                MessagePayload::class.java)
        When calling message.getContexts() itReturns listOf("ctx")
        When calling mockMessageManager.getNextDisplayMessage() itReturns message
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        Mockito.verify(activity).findViewById<View?>(ArgumentMatchers.anyInt())
    }

    @Test
    fun `should not display campaign if activity is not registered`() {
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        Mockito.verify(activity).findViewById<View?>(ArgumentMatchers.anyInt())
        val message = Mockito.mock(Message::class.java)

        When calling message.getCampaignId() itReturns "1"
        When calling message.isTest() itReturns false
        When calling message.getMaxImpressions() itReturns 1
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_NO_URL.trimIndent(),
                MessagePayload::class.java)
        When calling message.getContexts() itReturns listOf("ctx")
        When calling mockMessageManager.getNextDisplayMessage() itReturns message
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        // will be called only once when activity was unregistered
        Mockito.verify(activity).findViewById<View?>(ArgumentMatchers.anyInt())
    }

    @Test
    fun `should not display campaign if payload is null`() {
        When calling mockMessageManager.getNextDisplayMessage() itReturns null
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        Mockito.verify(activity, never()).findViewById<View?>(ArgumentMatchers.anyInt())
    }

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
                "title":"Campaign Title",
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
                "title":"Campaign Title",
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
                ?.onNewResult(dataSource as DataSource<Void>)
    }

    @Test
    fun `should not throw exception on failed result`() {
        val message = Mockito.mock(CampaignData::class.java)
        val activity = Mockito.mock(Activity::class.java)
        val dataSource = Mockito.mock(DataSource::class.java)
        When calling dataSource.progress itReturns 1f
        displayMessageJobIntentService?.ImagePrefetchSubscriber(message, activity)
                ?.onFailure(dataSource as DataSource<Void>)
    }
}
