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
import com.nhaarman.mockitokotlin2.any
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
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
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
    private val payload = Gson().fromJson(MessageMixerResponseSpec.MIXER_RESPONSE.trimIndent(),
        MessageMixerResponse::class.java).data[0].campaignData.getMessagePayload()

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
        `when`(activity.layoutInflater).thenReturn(
            LayoutInflater.from(ApplicationProvider.getApplicationContext()))

        `when`(configResponseData.rollOutPercentage).thenReturn(100)
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

        `when`(message.getCampaignId()).thenReturn("1")
        `when`(message.isTest()).thenReturn(true)
        `when`(message.getMaxImpressions()).thenReturn(10)
        `when`(message.getMessagePayload()).thenReturn(payload)
        displayMessageJobIntentService!!.onHandleWork(intent!!)
    }

    @Test
    fun `should not throw exception with valid message no url`() {
        val message = Mockito.mock(Message::class.java)
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(listOf(message))

        `when`(message.getCampaignId()).thenReturn("1")
        `when`(message.isTest()).thenReturn(true)
        `when`(message.getMaxImpressions()).thenReturn(10)
        displayMessageJobIntentService!!.onHandleWork(intent!!)
    }

    @Test
    fun `should call onVerifyContext for non-test campaign with contexts`() {
        setupCampaign()

        Mockito.verify(onVerifyContexts).invoke(listOf("ctx"), "DEV-Test (Android In-App-Test)")
    }

    @Test
    fun `should not call onVerifyContext for non-test campaign without contexts`() {
        val message = Mockito.mock(Message::class.java)

        `when`(onVerifyContexts.invoke(any(), any())).thenReturn(true)
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        `when`(message.getCampaignId()).thenReturn("1")
        `when`(message.isTest()).thenReturn(false)
        `when`(message.getMaxImpressions()).thenReturn(1)
        `when`(message.getMessagePayload()).thenReturn(payload)
        `when`(message.getContexts()).thenReturn(listOf())
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(message)
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        Mockito.verify(onVerifyContexts, never()).invoke(any(), any())
    }

    @Test
    fun `should not call onVerifyContext for test campaign with contexts`() {
        val message = Mockito.mock(Message::class.java)

        `when`(onVerifyContexts.invoke(any(), any())).thenReturn(true)
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        `when`(message.getCampaignId()).thenReturn("1")
        `when`(message.isTest()).thenReturn(true)
        `when`(message.getMaxImpressions()).thenReturn(1)
        `when`(message.getMessagePayload()).thenReturn(payload)
        `when`(message.getContexts()).thenReturn(listOf("ctx"))
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(message)
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        Mockito.verify(onVerifyContexts, never()).invoke(any(), any())
    }

    @SuppressWarnings("LongMethod")
    @Test
    fun `should call onVerifyContext with proper parameters`() {
        setupCampaign()

        argumentCaptor<List<String>>().apply {
            Mockito.verify(onVerifyContexts).invoke(capture(), any())
            firstValue shouldBeEqualTo listOf("ctx")
        }
        argumentCaptor<String>().apply {
            Mockito.verify(onVerifyContexts).invoke(any(), capture())
            firstValue shouldBeEqualTo "DEV-Test (Android In-App-Test)"
        }
    }

    @Test
    fun `should call getMessagePayload again when message's context was rejected`() {
        setupNextCampaign()

        Mockito.verify(mockMessageManager, Mockito.times(2)).getNextDisplayMessage()
    }

    @Test
    fun `should not add message to LocalDisplayedMessageRepository when its context was rejected`() {
        setupNextCampaign()

        Mockito.verify(mockLocalDisplayRepo, never()).addMessage(any())
    }

    @Test
    fun `should remove message from ReadyForDisplayMessageRepository when its context was rejected`() {
        val message = setupNextCampaign()

        argumentCaptor<String>().apply {
            Mockito.verify(mockReadyForDisplayRepo).removeMessage(capture(), eq(true))
            firstValue shouldBeEqualTo message.getCampaignId()
        }
    }

    @Test
    fun `should display campaign if onVerifyContext was not set (default value)`() {
        verifyActivity()
    }

    @Test
    fun `should not display campaign if activity is not registered`() {
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        Mockito.verify(activity).findViewById<View?>(ArgumentMatchers.anyInt())
        verifyActivity()
    }

    @Test
    fun `should not display campaign if payload is null`() {
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(null)
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        Mockito.verify(activity, never()).findViewById<View?>(ArgumentMatchers.anyInt())
    }

    private fun setupCampaign() {
        val message = Mockito.mock(Message::class.java)

        `when`(onVerifyContexts.invoke(any(), any())).thenReturn(true)
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        setupMocking(message)
        displayMessageJobIntentService!!.onHandleWork(intent!!)
    }

    private fun setupNextCampaign(): Message {
        val message = Mockito.mock(Message::class.java)

        `when`(onVerifyContexts.invoke(any(), any())).thenReturn(false)
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        setupMocking(message)
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        return message
    }

    private fun verifyActivity() {
        val message = Mockito.mock(Message::class.java)

        setupMocking(message)
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        Mockito.verify(activity).findViewById<View?>(ArgumentMatchers.anyInt())
    }

    private fun setupMocking(message: Message) {
        `when`(message.getCampaignId()).thenReturn("1")
        `when`(message.isTest()).thenReturn(false)
        `when`(message.getMaxImpressions()).thenReturn(1)
        `when`(message.getMessagePayload()).thenReturn(payload)
        `when`(message.getContexts()).thenReturn(listOf("ctx"))
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(message).thenReturn(null)
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
        val payload = Mockito.mock(MessagePayload::class.java)
        val resource = Mockito.mock(Resource::class.java)
        `when`(dataSource.progress).thenReturn(1f)
        `when`(message.getMessagePayload()).thenReturn(payload)
        `when`(payload.resource).thenReturn(resource)
        displayMessageJobIntentService?.ImagePrefetchSubscriber(message, activity)
                ?.onNewResult(dataSource as DataSource<Void>)
    }

    @Test
    fun `should not throw exception on failed result`() {
        val message = Mockito.mock(CampaignData::class.java)
        val activity = Mockito.mock(Activity::class.java)
        val dataSource = Mockito.mock(DataSource::class.java)
        `when`(dataSource.progress).thenReturn(1f)
        displayMessageJobIntentService?.ImagePrefetchSubscriber(message, activity)
                ?.onFailure(dataSource as DataSource<Void>)
    }
}
