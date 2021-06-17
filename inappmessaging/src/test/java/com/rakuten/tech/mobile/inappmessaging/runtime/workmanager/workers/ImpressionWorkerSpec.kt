package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import android.os.Build
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
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Impression
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import okhttp3.ResponseBody
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Call
import java.util.*

/**
 * Test class for ImpressionWorker.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@Suppress("LargeClass")
class ImpressionWorkerSpec : BaseTest() {

    private val context = Mockito.mock(Context::class.java)
    private val workerParameters = Mockito.mock(WorkerParameters::class.java)
    private val impressionRequest = Mockito.mock(ImpressionRequest::class.java)
    private var worker: ImpressionWorker? = null

    private var responseBodyCall: Call<ResponseBody>? = null

    @Before
    fun setup() {
        AccountRepository.instance().userInfoProvider = TestUserInfoProvider()
        HostAppInfoRepository.instance().addHostInfo(HostAppInfo(InAppMessagingTestConstants.APP_ID,
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                InAppMessagingTestConstants.SUB_KEY, InAppMessagingTestConstants.LOCALE))
        worker = ImpressionWorker(context, workerParameters)
        responseBodyCall = worker?.createReportImpressionCall(ENDPOINT, impressionRequest)

        // setup synchronous worker
        val config = Configuration.Builder()
                // Set log level to Log.DEBUG to make it easier to debug
                .setMinimumLoggingLevel(Log.DEBUG)
                // Use a SynchronousExecutor here to make it easier to write tests
                .setExecutor(SynchronousExecutor()).build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext(), config)
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID, "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
    }

    @Test
    fun `should response body call contains two headers`() {
        responseBodyCall!!.request().headers().size() shouldBeEqualTo 3
    }

    @Test
    fun `should response body call contains token header`() {
        responseBodyCall!!.request().header(MessageMixerRetrofitService.RAE_TOKEN_HEADER) shouldBeEqualTo
                "OAuth2 " + TestUserInfoProvider.TEST_USER_RAE_TOKEN
    }

    @Test
    fun `should response body call contains sub id header`() {
        responseBodyCall!!.request().header(MessageMixerRetrofitService.SUBSCRIPTION_ID_HEADER) shouldBeEqualTo
                InAppMessagingTestConstants.SUB_KEY
    }

    @Test
    fun `should response body call contains device id header`() {
        responseBodyCall!!.request().header(MessageMixerRetrofitService.DEVICE_ID_HEADER) shouldBeEqualTo
                InAppMessagingTestConstants.DEVICE_ID
    }

    @Test
    fun `should return failure for null endpoint`() {
        val impressionRequest = ImpressionRequest(
                campaignId = "testId",
                isTest = true,
                appVersion = HostAppInfoRepository.instance().getVersion(),
                sdkVersion = BuildConfig.VERSION_NAME,
                userIdentifiers = RuntimeUtil.getUserIdentifiers(),
                impressions = listOf(Impression(ImpressionType.ACTION_ONE, Date().time)))
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
                impressions = listOf(Impression(ImpressionType.ACTION_ONE, Date().time)))
        val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
        val request = createWorkRequest(impressionRequest, ImpressionWorker.IMPRESSION_REQUEST_KEY)

        val response = Gson().fromJson(CONFIG_RESPONSE_EMPTY.trimIndent(), ConfigResponse::class.java)
        ConfigResponseRepository.instance().addConfigResponse(response.data)

        workManager.enqueue(request).result.get()
        val workInfo = workManager.getWorkInfoById(request.id).get()
        workInfo.state shouldBeEqualTo WorkInfo.State.FAILED
    }

    @Test
    fun `should return failure for null data`() {
        val impressionRequest = ImpressionRequest(
                campaignId = "testId",
                isTest = true,
                appVersion = HostAppInfoRepository.instance().getVersion(),
                sdkVersion = BuildConfig.VERSION_NAME,
                userIdentifiers = RuntimeUtil.getUserIdentifiers(),
                impressions = listOf(Impression(ImpressionType.ACTION_ONE, Date().time)))
        val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
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
                impressions = listOf(Impression(ImpressionType.ACTION_ONE, Date().time)))
        val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
        val request = createWorkRequest(
                impressionRequest, ImpressionWorker.IMPRESSION_REQUEST_KEY, false)

        retrieveValidConfig()
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
                impressions = listOf(Impression(ImpressionType.ACTION_ONE, Date().time)))
        val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
        val request = createWorkRequest(
                impressionRequest, ImpressionWorker.IMPRESSION_REQUEST_KEY)

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
                        "rakuten.com.tech.mobile.test",
                        InAppMessagingTestConstants.DEVICE_ID,
                        InAppMessagingTestConstants.APP_VERSION,
                        "sample-key",
                        InAppMessagingTestConstants.LOCALE))
        val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
        val request = createWorkRequest(
                Gson().fromJson(REQUEST.trimIndent(), ImpressionRequest::class.java),
                ImpressionWorker.IMPRESSION_REQUEST_KEY)

        retrieveValidConfig()
        workManager.enqueue(request).result.get()
        val workInfo = workManager.getWorkInfoById(request.id).get()
        workInfo.state shouldBeEqualTo WorkInfo.State.FAILED
    }

    private fun retrieveValidConfig() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val mockHostRespository = Mockito.mock(HostAppInfoRepository::class.java)
        val mockMessageScheduler = Mockito.mock(MessageMixerPingScheduler::class.java)
        When calling mockHostRespository.getPackageName() itReturns ctx.packageName
        val version = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
        When calling mockHostRespository.getVersion() itReturns version
        val worker = ConfigWorker(context, workerParameters, mockHostRespository, ConfigResponseRepository.instance(),
                mockMessageScheduler)
        worker.doWork()
    }

    private fun createWorkRequest(impressionRequest: ImpressionRequest, key: String, isValid: Boolean = true):
            OneTimeWorkRequest = OneTimeWorkRequest.Builder(ImpressionWorker::class.java)
            .setInputData(if (isValid) getInputData(impressionRequest, key) else getInvalidInputData(key))
            .addTag("iam_impression_work")
            .build()

    private fun getInputData(impressionRequest: ImpressionRequest, key: String): Data {
        // Convert ImpressionRequest object into a Json String before setting it as input data.
        val impressionRequestJsonString = Gson().toJson(impressionRequest)
        // Create input data objects.
        return Data.Builder()
                .putString(key, impressionRequestJsonString)
                .build()
    }

    private fun getInvalidInputData(key: String): Data {
        // Convert ImpressionRequest object into a Json String before setting it as input data.
        val impressionRequestJsonString = Gson().toJson("{\"test\":\"invalid\"}")
        // Create input data objects.
        return Data.Builder()
                .putString(key, impressionRequestJsonString)
                .build()
    }

    companion object {
        private const val ENDPOINT = "https://host/impression/"
        private const val CONFIG_RESPONSE_INVALID = """{
            "data":{
                "enabled":true,
                "endpoints":{
                    "displayPermission":"https://sample.display.permission/",
                    "impression":"https://host/impression/",
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
        private const val CONFIG_RESPONSE_NULL = """{
            "data":{
                "enabled":true,
                "endpoints":{
                    "displayPermission":"https://sample.display.permission/",
                    "ping":"https://sample.ping.url/ping"
                }
            }
        }"""
        private const val REQUEST = """
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
