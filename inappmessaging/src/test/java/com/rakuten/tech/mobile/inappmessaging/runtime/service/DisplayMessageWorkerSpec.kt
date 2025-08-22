package com.rakuten.tech.mobile.inappmessaging.runtime.service

import android.app.Activity
import android.content.res.Resources
import android.os.Handler
import android.util.DisplayMetrics
import android.view.View
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Resource
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import com.rakuten.tech.mobile.inappmessaging.runtime.runnable.DisplayMessageRunnable
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
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

@SuppressWarnings(
    "LargeClass",
)
@RunWith(RobolectricTestRunner::class)
class DisplayMessageWorkerSpec : BaseTest() {

    private val activity = Mockito.mock(Activity::class.java)
    private val displayWorker = TestListenableWorkerBuilder<DisplayMessageWorker>(getApplicationContext()).build()
    private val mockMessageManager = Mockito.mock(MessageReadinessManager::class.java)
    private val onVerifyContexts = Mockito.mock(InAppMessaging.instance().onVerifyContext.javaClass)

    private val configResponseData = Mockito.mock(ConfigResponseData::class.java)
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
    fun `should return successful with valid message with empty string url`() {
        val message = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(
                resource = Resource(imageUrl = "", cropType = 0),
            ),
        )
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(listOf(message))
        runBlocking { displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success() }
        verify(handler).post(any())
    }

    @Test
    fun `should return successful with valid message and null url`() {
        val message = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(
                resource = Resource(imageUrl = null, cropType = 0),
            ),
        )
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(listOf(message))
        runBlocking { displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success() }
        verify(handler).post(any())
    }

    @Test
    fun `should display campaign if onVerifyContext was not set (default value)`() {
        verifyHandlerCalled(true)
    }

    @Test
    fun `should not display campaign if activity is not registered`() {
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        Mockito.verify(activity, atLeastOnce()).findViewById<View?>(ArgumentMatchers.anyInt())
        verifyHandlerCalled()
    }

    @Test
    fun `should not display campaign if payload is null`() {
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(listOf())
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
        val message = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(
                resource = Resource(imageUrl = "https://imageurl.jpg", cropType = 0),
            ),
        )
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(listOf(message)).thenReturn(null)
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(listOf(message))
        val mockResource = Mockito.mock(Resources::class.java)
        `when`(activity.resources).thenReturn(mockResource)
        `when`(mockResource.displayMetrics).thenReturn(Mockito.mock(DisplayMetrics::class.java))
        ImageUtilSpec.IS_VALID = isValid
        worker.picasso = ImageUtilSpec.setupMockPicasso()
        worker.handler = handler
        runBlocking { worker.doWork() shouldBeEqualTo ListenableWorker.Result.success() }

        verify(handler, mode).post(ArgumentMatchers.any(DisplayMessageRunnable::class.java))
    }

    @Test
    fun `should display the message if null image url`() {
        val message = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(
                resource = Resource(imageUrl = null, cropType = 0),
            ),
        )
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(listOf(message))
        runBlocking { displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success() }

        verify(handler).post(ArgumentMatchers.any(DisplayMessageRunnable::class.java))
    }

    @Test
    fun `should call onVerifyContext for non-test campaign with contexts`() {
        val message = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(
                title = "[ctx] DEV-Test (Android In-App-Test)",
            ),
        )
        `when`(onVerifyContexts.invoke(any(), any())).thenReturn(true)
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(listOf(message)).thenReturn(listOf())
        InAppMessaging.instance().onVerifyContext = onVerifyContexts
        runBlocking {
            displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success()
        }

        verify(onVerifyContexts).invoke(message.contexts, message.messagePayload.title)
    }

    @Test
    fun `should not call onVerifyContext for non-test campaign without contexts`() {
        val message = TestDataHelper.createDummyMessage()
        `when`(onVerifyContexts.invoke(any(), any())).thenReturn(true)
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(listOf(message)).thenReturn(listOf())
        InAppMessaging.instance().onVerifyContext = onVerifyContexts
        runBlocking {
            displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success()
        }

        verify(onVerifyContexts, never()).invoke(any(), any())
    }

    @Test
    fun `should not call onVerifyContext for test campaign with contexts`() {
        val message = TestDataHelper.createDummyMessage(
            isTest = true,
            messagePayload = TestDataHelper.createDummyPayload(
                title = "[ctx] DEV-Test (Android In-App-Test)",
            ),
        )
        `when`(onVerifyContexts.invoke(any(), any())).thenReturn(true)
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(listOf(message)).thenReturn(listOf())

        verify(onVerifyContexts, never()).invoke(any(), any())
    }

    @Test
    fun `should skip message when context was rejected`() {
        val message = TestDataHelper.createDummyMessage(
            messagePayload = TestDataHelper.createDummyPayload(
                title = "[ctx] DEV-Test (Android In-App-Test)",
            ),
        )
        `when`(onVerifyContexts.invoke(any(), any())).thenReturn(false)
        InAppMessaging.instance().onVerifyContext = onVerifyContexts
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(listOf(message)).thenReturn(listOf())
        runBlocking {
            displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success()
        }

        Mockito.verify(mockMessageManager, Mockito.times(2)).getNextDisplayMessage()
        Mockito.verify(mockMessageManager).removeMessageFromQueue(message.campaignId)
    }

    private fun verifyHandlerCalled(shouldCall: Boolean = false) {
        `when`(mockMessageManager.getNextDisplayMessage()).thenReturn(
            listOf(
                TestDataHelper.createDummyMessage(
                    messagePayload = TestDataHelper.createDummyPayload(
                        title = "[ctx] DEV-Test (Android In-App-Test)",
                    ),
                ),
            ),
        )
        runBlocking { displayWorker.doWork() shouldBeEqualTo ListenableWorker.Result.success() }

        if (shouldCall) {
            verify(handler).post(any())
        } else {
            verify(handler, never()).post(any())
        }
    }
}
