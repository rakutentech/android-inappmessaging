package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
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
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.CampaignType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ConfigResponseEndpoints
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageMixerResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
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
import retrofit2.Response
import java.net.HttpURLConnection

/**
 * Test class for MessageMixerWorker.
 */
@RunWith(RobolectricTestRunner::class)
open class MessageMixerWorkerSpec : BaseTest() {
    @Mock
    internal val mockResp: Response<MessageMixerResponse>? = null
    internal val ctx = Mockito.mock(Context::class.java)
    internal val workParam = Mockito.mock(WorkerParameters::class.java)
    internal val mockRetry = Mockito.mock(RetryDelayUtil::class.java)
    internal val mockSched = Mockito.mock(MessageMixerPingScheduler::class.java)

    @Before
    override fun setup() {
        super.setup()
        MockitoAnnotations.initMocks(this)
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id",
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        MessageMixerPingScheduler.currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
        MessageMixerWorker.serverErrorCounter.set(0)
    }

    @Test
    fun `should fail if request fail`() {
        `when`(mockResp?.isSuccessful).thenReturn(false)
        MessageMixerWorker(ctx, workParam!!).onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return success`() {
        `when`(mockResp?.isSuccessful).thenReturn(true)
        `when`(mockResp?.body() as Any?).thenReturn(null)
        MessageMixerWorker(ctx!!, workParam!!).onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.success()
    }

    @Test
    fun `should return retry`() {
        `when`(mockResp?.isSuccessful).thenReturn(true)
        `when`(mockResp?.body() as Any?).thenReturn(null)
        MessageMixerWorker(ctx!!, workParam!!).onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.success()
    }

    @Test
    fun `should return success with valid response`() {
        `when`(mockResp?.isSuccessful).thenReturn(true)
        `when`(mockResp?.body()).thenReturn(TestDataHelper.messageMixerResponse)
        MessageMixerWorker(ctx!!, workParam!!).onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.success()
    }

    @Test
    fun `should return success in api call`() {
        setupHostApp()
        setupValidConfig()
        MessageMixerWorker(ctx!!, workParam!!).doWork() shouldBeEqualTo ListenableWorker.Result.retry()
    }

    @Test
    fun `should have valid request payload`() {
        setupHostApp()
        AccountRepository.instance().userInfoProvider = object : UserInfoProvider {
            override fun provideAccessToken() = ""
            override fun provideUserId() = "user1"
            override fun provideIdTrackingIdentifier() = "tracking1"
        }
        setupValidConfig()
        val worker = MessageMixerWorker(ctx!!, workParam!!)
        worker.doWork() shouldBeEqualTo ListenableWorker.Result.retry()
        val buffer = Buffer()
        worker.testResponse!!.request().body()!!.writeTo(buffer)
        buffer.readUtf8().shouldContainAll("\"id\":\"tracking1\",\"type\":2", "\"id\":\"user1\",\"type\":3")
    }

    @Test
    fun `should return regular type only`() {
        val list = setupWorker(false)
        list.shouldHaveSize(1)
        list[0] shouldBeEqualTo CampaignType.REGULAR.typeId
    }

    @Test
    fun `should return both types`() {
        val list = setupWorker(true)
        list.shouldHaveSize(2)
        list[0] shouldBeEqualTo CampaignType.REGULAR.typeId
        list[1] shouldBeEqualTo CampaignType.PUSH_PRIMER.typeId
    }

    private fun setupWorker(isTiramisu: Boolean): ArrayList<Int> {
        val worker = MessageMixerWorker(ctx!!, workParam!!)
        val mockChecker = Mockito.mock(BuildVersionChecker::class.java)
        `when`(mockChecker.isAndroidTAndAbove()).thenReturn(isTiramisu)
        return worker.getSupportedCampaign(mockChecker)
    }

    private fun setupValidConfig() {
        ConfigResponseRepository.instance().addConfigResponse(
            ConfigResponseData(
                endpoints = ConfigResponseEndpoints("https://test"),
                100,
            ),
        )
    }

    private fun setupHostApp() {
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                "rakuten.com.tech.mobile.test", InAppMessagingTestConstants.DEVICE_ID,
                InAppMessagingTestConstants.APP_VERSION, "test-key", InAppMessagingTestConstants.LOCALE,
            ),
        )
    }
}

class MessageMixerWorkerFailSpec : MessageMixerWorkerSpec() {
    @Test
    fun `should use correct backoff delay if server fail less than 3 times`() {
        setupResponse(HttpURLConnection.HTTP_INTERNAL_ERROR)
        verifyBackoff()
    }

    @Test
    fun `should return failure if server fail more than 3 times`() {
        setupResponse(HttpURLConnection.HTTP_INTERNAL_ERROR)
        setupErrorBody()
        val worker = MessageMixerWorker(ctx!!, workParam!!, EventMessageReconciliationScheduler.instance(), mockSched)
        repeat(3) {
            worker.onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.Success()
        }
        worker.onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return success but will trigger next config when too many request error`() {
        setupResponse(RetryDelayUtil.RETRY_ERROR_CODE)
        setupErrorBody()
        `when`(mockRetry.getNextDelay(any())).thenReturn(1000)

        MessageMixerWorker(ctx!!, workParam!!, EventMessageReconciliationScheduler.instance(), mockSched, mockRetry)
            .onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.Success()

        Mockito.verify(mockSched).pingMessageMixerService(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY), anyOrNull())
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
        val worker = MessageMixerWorker(ctx!!, workParam!!, EventMessageReconciliationScheduler.instance(), mockSched)
        worker.onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should reset initial delay`() {
        setupResponse(RetryDelayUtil.RETRY_ERROR_CODE)
        `when`(mockResp?.body()).thenReturn(Mockito.mock(MessageMixerResponse::class.java))

        val worker = MessageMixerWorker(ctx!!, workParam!!, EventMessageReconciliationScheduler.instance(), mockSched)
        worker.onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockSched).pingMessageMixerService(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY), anyOrNull())
        MessageMixerPingScheduler.currDelay shouldBeGreaterThan (RetryDelayUtil.INITIAL_BACKOFF_DELAY * 2)

        `when`(mockResp.isSuccessful).thenReturn(true)
        `when`(mockResp.code()).thenReturn(200)
        worker.onResponse(mockResp) shouldBeEqualTo ListenableWorker.Result.Success()
        MessageMixerPingScheduler.currDelay shouldBeEqualTo RetryDelayUtil.INITIAL_BACKOFF_DELAY
    }

    private fun verifyBackoff() {
        val worker = MessageMixerWorker(ctx!!, workParam!!, EventMessageReconciliationScheduler.instance(), mockSched)
        worker.onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockSched).pingMessageMixerService(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY), anyOrNull())
        MessageMixerPingScheduler.currDelay shouldBeGreaterThan (RetryDelayUtil.INITIAL_BACKOFF_DELAY * 2)

        worker.onResponse(mockResp) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockSched).pingMessageMixerService(
            AdditionalMatchers.gt(RetryDelayUtil.INITIAL_BACKOFF_DELAY * 2), anyOrNull(),
        )
        MessageMixerPingScheduler.currDelay shouldBeGreaterThan (RetryDelayUtil.INITIAL_BACKOFF_DELAY * 4)
    }

    private fun setupResponse(code: Int) {
        `when`(mockResp?.isSuccessful).thenReturn(false)
        `when`(mockResp?.code()).thenReturn(code)
    }

    private fun setupErrorBody() {
        val errorBody = Mockito.mock(ResponseBody::class.java)
        `when`(mockResp?.errorBody()).thenReturn(errorBody)
        `when`(errorBody.string()).thenReturn("error")
    }
}
