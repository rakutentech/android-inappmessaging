package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.*
import com.rakuten.tech.mobile.inappmessaging.runtime.api.MessageMixerRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.Impression
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ConfigResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import okhttp3.ResponseBody
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import retrofit2.Call
import retrofit2.Response
import java.net.HttpURLConnection
import java.util.*

/**
 * Test class for ImpressionWorker.
 */
@RunWith(RobolectricTestRunner::class)
@Ignore("base class")
open class ImpressionWorkerSpec : BaseTest() {

    internal val ctx = Mockito.mock(Context::class.java)
    internal val workParam = Mockito.mock(WorkerParameters::class.java)
    private val impReq = Mockito.mock(ImpressionRequest::class.java)
    private var worker: ImpressionWorker? = null
    internal val context = ApplicationProvider.getApplicationContext<Context>()

    internal var respBodyCall: Call<ResponseBody>? = null

    @Mock
    internal val mockResp: Response<ResponseBody>? = null

    @Before
    override fun setup() {
        super.setup()
        MockitoAnnotations.initMocks(this)
        setupRepo()
        worker = ImpressionWorker(ctx, workParam)
        respBodyCall = worker?.createReportImpressionCall(ENDPOINT, impReq)

        initializeInApp()
    }

    internal fun createWorkRequest(
        impressionRequest: ImpressionRequest,
        key: String,
        isValid: Boolean = true,
    ): OneTimeWorkRequest = OneTimeWorkRequest.Builder(ImpressionWorker::class.java)
        .setInputData(if (isValid) getInputData(impressionRequest, key) else getInvalidInputData(key))
        .addTag("iam_impression_work")
        .build()

    private fun getInputData(impressionRequest: ImpressionRequest, key: String): Data {
        // Convert ImpressionRequest object into a Json String before setting it as input data.
        val impressionRequestJsonString = Gson().toJson(impressionRequest)
        // Create input data objects.
        return Data.Builder().putString(key, impressionRequestJsonString).build()
    }

    private fun getInvalidInputData(key: String): Data {
        // Convert ImpressionRequest object into a Json String before setting it as input data.
        val impressionRequestJsonString = Gson().toJson("{\"test\":\"invalid\"}")
        // Create input data objects.
        return Data.Builder().putString(key, impressionRequestJsonString).build()
    }

    private fun setupRepo() {
        AccountRepository.instance().userInfoProvider = TestUserInfoProvider()
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                InAppMessagingTestConstants.APP_ID,
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                InAppMessagingTestConstants.SUB_KEY, InAppMessagingTestConstants.LOCALE,
            ),
        )
    }

    private fun initializeInApp() {
        // setup synchronous worker
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG) // Set log level to Log.DEBUG to make it easier to debug
            .setExecutor(SynchronousExecutor()) // Use a SynchronousExecutor here to make it easier to write tests
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID, "test_device_id",
        )
        InAppMessaging.initialize(context, true)
        ImpressionWorker.serverErrorCounter.set(0)
    }

    companion object {
        private const val ENDPOINT = "https://host/impression/"
        internal const val CONFIG_RESPONSE_INVALID = """{
            "data":{
                "enabled":true,
                "endpoints":{
                    "displayPermission":"https://sample.display.permission/",
                    "impression":"https://host/impression/",
                    "ping":"https://sample.ping.url/ping"
                }
            }
        }"""

        internal const val REQUEST = """
            {
                "appVersion":"0.0.1",
                "campaignId":"1234567890",
                "impressions":[
                    {
                    "impType":"IMPRESSION",
                    "timestamp":1583851442449,
                    "type":1
                    },
                    {
                    "impType":"EXIT",
                    "timestamp":1583851442449,
                    "type":4}
                ],
                "isTest":false,
                "sdkVersion":"1.6.0-SNAPSHOT",
                "userIdentifiers":[]}
        """
    }
}

class ImpressionWorkerEndpointSpec : ImpressionWorkerSpec() {
    @Test
    fun `should return failure for null endpoint`() {
        val impressionRequest = ImpressionRequest(
            campaignId = "testId",
            isTest = true,
            appVersion = HostAppInfoRepository.instance().getVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            userIdentifiers = RuntimeUtil.getUserIdentifiers(),
            impressions = listOf(Impression(ImpressionType.ACTION_ONE, Date().time)),
        )
        val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
        val request = createWorkRequest(impressionRequest, ImpressionWorker.IMPRESSION_REQUEST_KEY)

        val response = Gson().fromJson(CONFIG_RESPONSE_NULL.trimIndent(), ConfigResponse::class.java)
        ConfigResponseRepository.instance().addConfigResponse(response.data)
        workManager.enqueue(request).result.get()
        val workInfo = workManager.getWorkInfoById(request.id).get()
        workInfo.state shouldBeEqualTo WorkInfo.State.FAILED
    }

    @Test
    fun `should return failure for empty endpoint`() {
        val impressionRequest = ImpressionRequest(
            campaignId = "testId",
            isTest = true,
            appVersion = HostAppInfoRepository.instance().getVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            userIdentifiers = RuntimeUtil.getUserIdentifiers(),
            impressions = listOf(Impression(ImpressionType.ACTION_ONE, Date().time)),
        )
        val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
        val request = createWorkRequest(impressionRequest, ImpressionWorker.IMPRESSION_REQUEST_KEY)

        val response = Gson().fromJson(CONFIG_RESPONSE_EMPTY.trimIndent(), ConfigResponse::class.java)
        ConfigResponseRepository.instance().addConfigResponse(response.data)

        workManager.enqueue(request).result.get()
        val workInfo = workManager.getWorkInfoById(request.id).get()
        workInfo.state shouldBeEqualTo WorkInfo.State.FAILED
    }

    companion object {
        private const val CONFIG_RESPONSE_NULL = """{
            "data":{
                "enabled":true,
                "endpoints":{
                    "displayPermission":"https://sample.display.permission/",
                    "ping":"https://sample.ping.url/ping"
                }
            }
        }"""

        private const val CONFIG_RESPONSE_EMPTY = """{
            "data":{
                "enabled":true,
                "endpoints":{
                    "displayPermission":"https://sample.display.permission/",
                    "impression":"",
                    "ping":"https://sample.ping.url/ping"
                }
            }
        }"""
    }
}

class ImpressionWorkerRespSpec : ImpressionWorkerSpec() {
    @Test
    fun `should response body call contains token header`() {
        respBodyCall!!.request().header(MessageMixerRetrofitService.ACCESS_TOKEN_HEADER) shouldBeEqualTo
            "OAuth2 " + TestUserInfoProvider.TEST_USER_ACCESS_TOKEN
    }

    @Test
    fun `should response body call contains sub id header`() {
        respBodyCall!!.request().header(MessageMixerRetrofitService.SUBSCRIPTION_ID_HEADER) shouldBeEqualTo
            InAppMessagingTestConstants.SUB_KEY
    }

    @Test
    fun `should response body call contains device id header`() {
        respBodyCall!!.request().header(MessageMixerRetrofitService.DEVICE_ID_HEADER) shouldBeEqualTo
            InAppMessagingTestConstants.DEVICE_ID
    }
}

class ImpressionWorkerFailSpec : ImpressionWorkerSpec() {
    @Test
    fun `should return retry if server fail less than 3 times`() {
        val worker = setupWorker(HttpURLConnection.HTTP_INTERNAL_ERROR)

        worker.onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.retry()
    }

    @Test
    fun `should return failure if server fail more than 3 times`() {
        val worker = setupWorker(HttpURLConnection.HTTP_INTERNAL_ERROR)

        repeat(3) { worker.onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.retry() }
        worker.onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return failure if bad request`() {
        val worker = setupWorker(HttpURLConnection.HTTP_BAD_REQUEST)

        worker.onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return success if ok`() {
        val worker = setupWorker(HttpURLConnection.HTTP_OK)

        worker.onResponse(mockResp!!) shouldBeEqualTo ListenableWorker.Result.success()
    }

    private fun setupWorker(code: Int): ImpressionWorker {
        `when`(mockResp?.isSuccessful).thenReturn(false)
        `when`(mockResp?.code()).thenReturn(code)
        return ImpressionWorker(ctx, workParam)
    }
}

class ImpressionWorkerBaseSpec : ImpressionWorkerSpec() {

    @Test
    fun `should return failure for null data`() {
        val impressionRequest = ImpressionRequest(
            campaignId = "testId",
            isTest = true,
            appVersion = HostAppInfoRepository.instance().getVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            userIdentifiers = RuntimeUtil.getUserIdentifiers(),
            impressions = listOf(Impression(ImpressionType.ACTION_ONE, Date().time)),
        )
        val workManager = WorkManager.getInstance(context)

        val request = createWorkRequest(impressionRequest, "test-key")

        retrieveValidConfig()
        workManager.enqueue(request).result.get()
        val workInfo = workManager.getWorkInfoById(request.id).get()
        workInfo.state shouldBeEqualTo WorkInfo.State.FAILED
    }

    @Test
    fun `should return failure for invalid json`() {
        val impressionRequest = ImpressionRequest(
            campaignId = "testId",
            isTest = true,
            appVersion = HostAppInfoRepository.instance().getVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            userIdentifiers = RuntimeUtil.getUserIdentifiers(),
            impressions = listOf(Impression(ImpressionType.ACTION_ONE, Date().time)),
        )
        val workManager = WorkManager.getInstance(context)

        val request = createWorkRequest(impressionRequest, ImpressionWorker.IMPRESSION_REQUEST_KEY, false)

        retrieveValidConfig()
        val response = Gson().fromJson(CONFIG_RESPONSE_INVALID.trimIndent(), ConfigResponse::class.java)
        ConfigResponseRepository.instance().addConfigResponse(response.data)
        workManager.enqueue(request).result.get()
        val workInfo = workManager.getWorkInfoById(request.id).get()
        workInfo.state shouldBeEqualTo WorkInfo.State.FAILED
    }

    @Test
    fun `should return retry invalid impression call`() {
        val impressionRequest = ImpressionRequest(
            campaignId = "testId",
            isTest = true,
            appVersion = HostAppInfoRepository.instance().getVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            userIdentifiers = RuntimeUtil.getUserIdentifiers(),
            impressions = listOf(Impression(ImpressionType.ACTION_ONE, Date().time)),
        )
        val workManager = WorkManager.getInstance(context)

        val request = createWorkRequest(impressionRequest, ImpressionWorker.IMPRESSION_REQUEST_KEY)

        val response = Gson().fromJson(CONFIG_RESPONSE_INVALID.trimIndent(), ConfigResponse::class.java)
        ConfigResponseRepository.instance().addConfigResponse(response.data)
        workManager.enqueue(request).result.get()
        val workInfo = workManager.getWorkInfoById(request.id).get()
        workInfo.state shouldBeEqualTo WorkInfo.State.ENQUEUED
    }

    @Test
    fun `should return failure due invalid token`() {
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                "rakuten.com.tech.mobile.test", InAppMessagingTestConstants.DEVICE_ID,
                InAppMessagingTestConstants.APP_VERSION, "sample-key", InAppMessagingTestConstants.LOCALE,
            ),
        )
        val workManager = WorkManager.getInstance(context)

        val request = createWorkRequest(
            Gson().fromJson(REQUEST.trimIndent(), ImpressionRequest::class.java),
            ImpressionWorker.IMPRESSION_REQUEST_KEY,
        )

        retrieveValidConfig()
        workManager.enqueue(request).result.get()
        val workInfo = workManager.getWorkInfoById(request.id).get()
        workInfo.state shouldBeEqualTo WorkInfo.State.ENQUEUED
    }

    @Test
    fun `should response body call contains two headers`() {
        respBodyCall!!.request().headers().size() shouldBeEqualTo 3
    }

    private fun retrieveValidConfig() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val mockHost = Mockito.mock(HostAppInfoRepository::class.java)
        val mockSched = Mockito.mock(MessageMixerPingScheduler::class.java)
        `when`(mockHost.getPackageName()).thenReturn(ctx.packageName)
        val version = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
        `when`(mockHost.getVersion()).thenReturn(version)
        `when`(mockHost.getSubscriptionKey()).thenReturn("test_key")
        val worker = ConfigWorker(this.ctx, workParam, mockHost, ConfigResponseRepository.instance(), mockSched)
        worker.doWork()
    }
}
