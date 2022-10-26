package com.rakuten.tech.mobile.inappmessaging.runtime.service

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.util.DisplayMetrics
import android.view.View
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.SerialExecutor
import androidx.work.impl.utils.taskexecutor.TaskExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageMixerResponseSpec
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Resource
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import com.rakuten.tech.mobile.inappmessaging.runtime.runnable.DisplayMessageRunnable
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ImageUtilSpec
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.DisplayMessageWorker
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.verification.VerificationMode
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for DisplayMessageJobIntentService.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
open class DisplayMessageWorkerSpec : BaseTest() {

    private val activity = Mockito.mock(Activity::class.java)
    internal val displayWorker = TestListenableWorkerBuilder<DisplayMessageWorker>(getApplicationContext()).build()
    internal val mockMessageManager = Mockito.mock(MessageReadinessManager::class.java)

    private val configResponseData = Mockito.mock(ConfigResponseData::class.java)
    internal val payload = MessageMixerResponseSpec.response.data[0].campaignData.getMessagePayload()
    private val handler = Mockito.mock(Handler::class.java)

    @Before
    override fun setup() {
        super.setup()
        displayWorker.messageReadinessManager = mockMessageManager
        displayWorker.handler = handler
        `when`(configResponseData.rollOutPercentage).thenReturn(100)
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        InAppMessaging.initialize(getApplicationContext(), true)
        InAppMessaging.instance().registerMessageDisplayActivity(activity)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        ConfigResponseRepository.resetInstance()
        MessageReadinessManager.instance().clearMessages()
    }

    @Test
    fun `should return successful if no message`() {
        runBlocking { displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success() }
        Mockito.verify(handler, never()).post(any())
    }

    @Test
    fun `should not throw exception with mock parameters`() {
        val mockParams = Mockito.mock(WorkerParameters::class.java)
        val mockExecutor = Mockito.mock(TaskExecutor::class.java)
        `when`(mockParams.taskExecutor).thenReturn(mockExecutor)
        `when`(mockExecutor.backgroundExecutor).thenReturn(Mockito.mock(SerialExecutor::class.java))
        val worker = DisplayMessageWorker(Mockito.mock(Context::class.java), mockParams)
        runBlocking { worker.doWork() shouldBeEqualTo ListenableWorker.Result.success() }
        Mockito.verify(handler, never()).post(any())
    }

    @Test
    fun `should return successful with valid message with empty string url`() {
        val message = setupValidMessage()
        val mockPayload = Mockito.mock(MessagePayload::class.java)
        val mockResource = Mockito.mock(Resource::class.java)
        `when`(message.getMessagePayload()).thenReturn(mockPayload)
        `when`(mockPayload.resource).thenReturn(mockResource)
        `when`(mockResource.imageUrl).thenReturn("")
        runBlocking { displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success() }
        Mockito.verify(handler).post(any())
    }

    @Test
    fun `should return successful with valid message and null url`() {
        val message = setupValidMessage()
        `when`(message.getMessagePayload()).thenReturn(payload)
        runBlocking { displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success() }
        Mockito.verify(handler).post(any())
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
        runBlocking { displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success() }

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
        val worker = TestListenableWorkerBuilder<DisplayMessageWorker>(getApplicationContext()).build()
        worker.messageReadinessManager = mockMessageManager
        val message = setupMessageWithImage("https://imageurl.jpg")
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(message).thenReturn(null)
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(message)
        val mockResource = Mockito.mock(Resources::class.java)
        `when`(activity.resources).thenReturn(mockResource)
        `when`(mockResource.displayMetrics).thenReturn(Mockito.mock(DisplayMetrics::class.java))
        ImageUtilSpec.IS_VALID = isValid
        worker.picasso = ImageUtilSpec.setupMockPicasso()
        worker.handler = handler
        runBlocking { worker.doWork() shouldBeEqualTo ListenableWorker.Result.success() }

        Mockito.verify(handler, mode).post(ArgumentMatchers.any(DisplayMessageRunnable::class.java))
    }

    @Test
    fun `should display the message if null image url`() {
        val message = setupMessageWithImage(null)
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(message)
        runBlocking { displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success() }

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

    private fun setupValidMessage(): Message {
        val message = Mockito.mock(Message::class.java)
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(message)
        `when`(message.getCampaignId()).thenReturn("1")
        `when`(message.isTest()).thenReturn(true)
        `when`(message.getMaxImpressions()).thenReturn(10)
        return message
    }

    private fun verifyHandlerCalled(shouldCall: Boolean = false) {
        val message = Mockito.mock(Message::class.java)

        setupMocking(message)
        runBlocking { displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success() }

        if (shouldCall) {
            Mockito.verify(handler).post(any())
        } else {
            Mockito.verify(handler, never()).post(any())
        }
    }

    internal fun setupMocking(message: Message) {
        `when`(message.getCampaignId()).thenReturn("1")
        `when`(message.isTest()).thenReturn(false)
        `when`(message.getMaxImpressions()).thenReturn(1)
        `when`(message.getMessagePayload()).thenReturn(payload)
        `when`(message.getContexts()).thenReturn(listOf("ctx"))
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(message).thenReturn(null)
    }
}

class DisplayMessageWorkerVerifyContextSpec : DisplayMessageWorkerSpec() {

    private val onVerifyContexts = Mockito.mock(InAppMessaging.instance().onVerifyContext.javaClass)

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
        runBlocking {
            displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success()
        }

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
        runBlocking {
            displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success()
        }

        Mockito.verify(onVerifyContexts, never()).invoke(any(), any())
    }

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

    private fun setupNextCampaign(): Message {
        val message = Mockito.mock(Message::class.java)

        `when`(onVerifyContexts.invoke(any(), any())).thenReturn(false)
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        setupMocking(message)
        runBlocking {
            displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success()
        }

        return message
    }

    private fun setupCampaign() {
        val message = Mockito.mock(Message::class.java)

        `when`(onVerifyContexts.invoke(any(), any())).thenReturn(true)
        InAppMessaging.instance().onVerifyContext = onVerifyContexts

        setupMocking(message)
        runBlocking {
            displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success()
        }
    }
}
