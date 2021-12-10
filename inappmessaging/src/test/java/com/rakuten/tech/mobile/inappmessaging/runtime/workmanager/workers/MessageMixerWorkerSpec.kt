package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessagingTestConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageMixerResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageMixerResponseSpec
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import okhttp3.ResponseBody
import okio.Buffer
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.AdditionalMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response
import java.net.HttpURLConnection

/**
 * Test class for MessageMixerWorker.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@SuppressWarnings("LargeClass")
class MessageMixerWorkerSpec : BaseTest() {
    @Mock
    private val mockResponse: Response<MessageMixerResponse>? = null
    private val context = Mockito.mock(Context::class.java)
    private val workerParameters = Mockito.mock(WorkerParameters::class.java)
    private val mockRetry = Mockito.mock(RetryDelayUtil::class.java)
    private val mockScheduler = Mockito.mock(MessageMixerPingScheduler::class.java)

    @Before
    override fun setup() {
        super.setup()
        MockitoAnnotations.initMocks(this)
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        MessageMixerPingScheduler.currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
        MessageMixerWorker.serverErrorCounter.set(0)
    }

    @Test
    fun `should fail if request fail`() {
        `when`(mockResponse?.isSuccessful).thenReturn(false)
        MessageMixerWorker(context, workerParameters!!)
                .onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return success`() {
        `when`(mockResponse?.isSuccessful).thenReturn(true)
        `when`(mockResponse?.body() as Any?).thenReturn(null)
        MessageMixerWorker(context!!, workerParameters!!)
                .onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.success()
    }

    @Test
    fun `should return retry`() {
        `when`(mockResponse?.isSuccessful).thenReturn(true)
        `when`(mockResponse?.body() as Any?).thenReturn(null)
        MessageMixerWorker(context!!, workerParameters!!)
                .onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.success()
    }

    @Test
    fun `should return success with valid response`() {
        `when`(mockResponse?.isSuccessful).thenReturn(true)
        `when`(mockResponse?.body()).thenReturn(MessageMixerResponseSpec.response)
        MessageMixerWorker(context!!, workerParameters!!)
                .onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.success()
    }

    @Test
    fun `should use correct backoff delay if server fail less than 3 times`() {
        setupResponse(HttpURLConnection.HTTP_INTERNAL_ERROR)
        verifyBackoff()
    }

    @Test
    fun `should return failure if server fail more than 3 times`() {
        setupResponse(HttpURLConnection.HTTP_INTERNAL_ERROR)
        setupErrorBody()
        val worker = MessageMixerWorker(
            context!!, workerParameters!!, EventMessageReconciliationScheduler.instance(),
            mockScheduler
        )
        repeat(3) {
            worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()
        }
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return success but will trigger next config when too many request error`() {
        setupResponse(RetryDelayUtil.RETRY_ERROR_CODE)
        setupErrorBody()
        `when`(mockRetry.getNextDelay(any())).thenReturn(1000)

        MessageMixerWorker(context!!, workerParameters!!, EventMessageReconciliationScheduler.instance(),
                mockScheduler, mockRetry)
                .onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()

        Mockito.verify(mockScheduler).pingMessageMixerService(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY), anyOrNull())
        Mockito.verify(mockRetry).getNextDelay(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY))
    }

    @Test
    fun `should use correct backoff delay`() {
        setupResponse(RetryDelayUtil.RETRY_ERROR_CODE)
        verifyBackoff()
    }

    @Test
    fun `should return failure on bad request`() {
        setupResponse(HttpURLConnection.HTTP_BAD_REQUEST)
        setupErrorBody()
        val worker = MessageMixerWorker(context!!, workerParameters!!, EventMessageReconciliationScheduler.instance(),
            mockScheduler)
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should reset initial delay`() {
        setupResponse(RetryDelayUtil.RETRY_ERROR_CODE)
        `when`(mockResponse?.body()).thenReturn(Mockito.mock(MessageMixerResponse::class.java))

        val worker = MessageMixerWorker(context!!, workerParameters!!, EventMessageReconciliationScheduler.instance(),
                mockScheduler)
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockScheduler).pingMessageMixerService(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY), anyOrNull())
        MessageMixerPingScheduler.currDelay shouldBeGreaterThan (RetryDelayUtil.INITIAL_BACKOFF_DELAY * 2)

        `when`(mockResponse.isSuccessful).thenReturn(true)
        `when`(mockResponse.code()).thenReturn(200)
        worker.onResponse(mockResponse) shouldBeEqualTo ListenableWorker.Result.Success()
        MessageMixerPingScheduler.currDelay shouldBeEqualTo RetryDelayUtil.INITIAL_BACKOFF_DELAY
    }

    @Test
    fun `should return success in api call`() {
        HostAppInfoRepository.instance().addHostInfo(
                HostAppInfo(
                        "rakuten.com.tech.mobile.test",
                        InAppMessagingTestConstants.DEVICE_ID,
                        InAppMessagingTestConstants.APP_VERSION,
                        "test-key",
                        InAppMessagingTestConstants.LOCALE))
        retrieveValidConfig()
        MessageMixerWorker(context!!, workerParameters!!).doWork() shouldBeEqualTo ListenableWorker.Result.retry()
    }

    @Test
    fun `should have valid request payload`() {
        HostAppInfoRepository.instance().addHostInfo(
                HostAppInfo("rakuten.com.tech.mobile.test", InAppMessagingTestConstants.DEVICE_ID,
                        InAppMessagingTestConstants.APP_VERSION, "test-key",
                        InAppMessagingTestConstants.LOCALE))
        AccountRepository.instance().userInfoProvider = object : UserInfoProvider {
            override fun provideAccessToken() = ""
            override fun provideUserId() = "user1"
            override fun provideIdTrackingIdentifier() = "tracking1"
        }
        retrieveValidConfig()
        val worker = MessageMixerWorker(context!!, workerParameters!!)
        worker.doWork() shouldBeEqualTo ListenableWorker.Result.retry()
        val buffer = Buffer()
        worker.responseCall!!.request().body()!!.writeTo(buffer)
        buffer.readUtf8().shouldContainAll(
                "\"id\":\"tracking1\",\"type\":2",
                "\"id\":\"user1\",\"type\":3")
    }

    private fun verifyBackoff() {
        val worker = MessageMixerWorker(
            context!!, workerParameters!!, EventMessageReconciliationScheduler.instance(),
            mockScheduler
        )
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockScheduler).pingMessageMixerService(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY), anyOrNull())
        MessageMixerPingScheduler.currDelay shouldBeGreaterThan (RetryDelayUtil.INITIAL_BACKOFF_DELAY * 2)

        worker.onResponse(mockResponse) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockScheduler).pingMessageMixerService(
            AdditionalMatchers.gt(RetryDelayUtil.INITIAL_BACKOFF_DELAY * 2), anyOrNull()
        )
        MessageMixerPingScheduler.currDelay shouldBeGreaterThan (RetryDelayUtil.INITIAL_BACKOFF_DELAY * 4)
    }

    private fun retrieveValidConfig() {
        val mockMessageScheduler = Mockito.mock(MessageMixerPingScheduler::class.java)
        val worker = ConfigWorker(context, workerParameters, HostAppInfoRepository.instance(),
                ConfigResponseRepository.instance(), mockMessageScheduler)
        worker.doWork()
    }

    private fun setupResponse(code: Int) {
        `when`(mockResponse?.isSuccessful).thenReturn(false)
        `when`(mockResponse?.code()).thenReturn(code)
    }

    private fun setupErrorBody() {
        val errorBody = Mockito.mock(ResponseBody::class.java)
        `when`(mockResponse?.errorBody()).thenReturn(errorBody)
        `when`(errorBody.string()).thenReturn("error")
    }
}
