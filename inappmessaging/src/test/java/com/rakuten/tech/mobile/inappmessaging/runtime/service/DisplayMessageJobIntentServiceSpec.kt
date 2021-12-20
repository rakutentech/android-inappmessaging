package com.rakuten.tech.mobile.inappmessaging.runtime.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import androidx.work.testing.WorkManagerTestInitHelper
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
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.inappmessaging.runtime.runnable.DisplayMessageRunnable
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ImageUtilSpec
import org.mockito.Mockito.`when`
import org.mockito.verification.VerificationMode

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
    private val payload = MessageMixerResponseSpec.response.data[0].campaignData.getMessagePayload()
    private val handler = Mockito.mock(Handler::class.java)
    private val context = getApplicationContext<Context>()

    @SuppressWarnings("LongMethod")
    @Before
    override fun setup() {
        super.setup()
        serviceController = Robolectric.buildService(DisplayMessageJobIntentService::class.java)
        displayMessageJobIntentService = spy(serviceController?.bind()?.create()?.get())
        displayMessageJobIntentService!!.messageReadinessManager = mockMessageManager
        displayMessageJobIntentService!!.localDisplayRepo = mockLocalDisplayRepo
        displayMessageJobIntentService!!.readyMessagesRepo = mockReadyForDisplayRepo
        displayMessageJobIntentService!!.handler = handler
        `when`(activity.layoutInflater).thenReturn(LayoutInflater.from(context))
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        `when`(configResponseData.rollOutPercentage).thenReturn(100)
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        Settings.Secure.putString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID, "test_device_id")
        InAppMessaging.initialize(context)
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
        verifyHandlerCalled(true)
    }

    @Test
    fun `should not display campaign if activity is not registered`() {
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        Mockito.verify(activity).findViewById<View?>(ArgumentMatchers.anyInt())
        verifyHandlerCalled()
    }

    @Test
    fun `should not display campaign if payload is null`() {
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(null)
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        Mockito.verify(activity, never()).findViewById<View?>(ArgumentMatchers.anyInt())
    }

    @Test
    fun `should try to display message with valid image`() {
        verifyFetchImage(true, times(1))
    }

    @Test
    fun `should not try to display message with invalid image`() {
        verifyFetchImage(false, never())
    }

    private fun verifyFetchImage(isValid: Boolean, mode: VerificationMode) {
        val service = DisplayMessageJobIntentService()
        service.messageReadinessManager = mockMessageManager
        val message = setupMessageWithImage("https://imageurl.jpg")
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(message).thenReturn(null)
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(message)
        val mockResource = Mockito.mock(Resources::class.java)
        `when`(activity.resources).thenReturn(mockResource)
        `when`(mockResource.displayMetrics).thenReturn(Mockito.mock(DisplayMetrics::class.java))
        ImageUtilSpec.IS_VALID = isValid
        service.picasso = ImageUtilSpec.setupMockPicasso()
        service.handler = handler
        service.onHandleWork(intent)

        Mockito.verify(handler, mode).post(ArgumentMatchers.any(DisplayMessageRunnable::class.java))
    }

    @Test
    fun `should display the message if null image url`() {
        val message = setupMessageWithImage(null)
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(message)
        displayMessageJobIntentService?.onHandleWork(intent)

        Mockito.verify(handler).post(ArgumentMatchers.any(DisplayMessageRunnable::class.java))
    }

    private fun setupMessageWithImage(imageUrl: String?): Message {
        val message = Mockito.mock(Message::class.java)
        val payload = Mockito.mock(MessagePayload::class.java)
        val resource = Mockito.mock(Resource::class.java)

        `when`(resource.imageUrl).thenReturn(imageUrl)
        `when`(payload.resource).thenReturn(resource)
        `when`(message.getMessagePayload()).thenReturn(payload)
        `when`(message.getCampaignId()).thenReturn("1")
        `when`(message.isTest()).thenReturn(true)
        `when`(message.getMaxImpressions()).thenReturn(1)
        `when`(message.getMessagePayload()).thenReturn(payload)
        `when`(message.getContexts()).thenReturn(listOf("ctx"))
        return message
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

    private fun verifyHandlerCalled(shouldCall: Boolean = false) {
        val message = Mockito.mock(Message::class.java)

        setupMocking(message)
        displayMessageJobIntentService!!.onHandleWork(intent!!)

        if (shouldCall) {
            Mockito.verify(handler).post(any())
        } else {
            Mockito.verify(handler, never()).post(any())
        }
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
