package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.gson.Gson
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
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import okio.Buffer
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.AdditionalMatchers
import org.mockito.Mock
import org.mockito.Mockito
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
    }

    @Test
    fun `should fail if request fail`() {
        When calling mockResponse?.isSuccessful itReturns false
        MessageMixerWorker(context, workerParameters!!)
                .onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return success`() {
        When calling mockResponse?.isSuccessful itReturns true
        When calling mockResponse?.body() as Any? itReturns null
        MessageMixerWorker(context!!, workerParameters!!)
                .onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.success()
    }

    @Test
    fun `should return retry`() {
        When calling mockResponse?.isSuccessful itReturns true
        When calling mockResponse?.body() as Any? itReturns null
        MessageMixerWorker(context!!, workerParameters!!)
                .onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.success()
    }

    @Test
    fun `should return success with valid response`() {
        When calling mockResponse?.isSuccessful itReturns true
        When calling mockResponse?.body() itReturns Gson().fromJson(MIXER_RESPONSE.trimIndent(),
                MessageMixerResponse::class.java)
        MessageMixerWorker(context!!, workerParameters!!)
                .onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.success()
    }

    @Test
    fun `should return retry with internal error response`() {
        When calling mockResponse?.isSuccessful itReturns false
        When calling mockResponse?.code() itReturns HttpURLConnection.HTTP_INTERNAL_ERROR
        MessageMixerWorker(context!!, workerParameters!!)
                .onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.retry()
    }

    @Test
    fun `should return success but will trigger next config when too many request error`() {
        When calling mockResponse?.isSuccessful itReturns false
        When calling mockResponse?.code() itReturns RetryDelayUtil.RETRY_ERROR_CODE
        When calling mockRetry.getNextDelay(any()) itReturns 1000

        MessageMixerWorker(context!!, workerParameters!!, EventMessageReconciliationScheduler.instance(),
                mockScheduler, mockRetry)
                .onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()

        Mockito.verify(mockScheduler).pingMessageMixerService(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY), anyOrNull())
        Mockito.verify(mockRetry).getNextDelay(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY))
    }

    @Test
    fun `should use correct backoff delay`() {
        When calling mockResponse?.isSuccessful itReturns false
        When calling mockResponse?.code() itReturns RetryDelayUtil.RETRY_ERROR_CODE

        val worker = MessageMixerWorker(context!!, workerParameters!!, EventMessageReconciliationScheduler.instance(),
                mockScheduler)
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockScheduler).pingMessageMixerService(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY), anyOrNull())
        MessageMixerPingScheduler.currDelay shouldBeGreaterThan (RetryDelayUtil.INITIAL_BACKOFF_DELAY * 2)

        worker.onResponse(mockResponse) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockScheduler).pingMessageMixerService(
                AdditionalMatchers.gt(RetryDelayUtil.INITIAL_BACKOFF_DELAY * 2), anyOrNull())
        MessageMixerPingScheduler.currDelay shouldBeGreaterThan (RetryDelayUtil.INITIAL_BACKOFF_DELAY * 4)
    }

    @Test
    fun `should reset initial delay`() {
        When calling mockResponse?.isSuccessful itReturns false
        When calling mockResponse?.code() itReturns RetryDelayUtil.RETRY_ERROR_CODE
        When calling mockResponse?.body() itReturns Mockito.mock(MessageMixerResponse::class.java)

        val worker = MessageMixerWorker(context!!, workerParameters!!, EventMessageReconciliationScheduler.instance(),
                mockScheduler)
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockScheduler).pingMessageMixerService(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY), anyOrNull())
        MessageMixerPingScheduler.currDelay shouldBeGreaterThan (RetryDelayUtil.INITIAL_BACKOFF_DELAY * 2)

        When calling mockResponse.isSuccessful itReturns true
        When calling mockResponse.code() itReturns 200
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
        MessageMixerWorker(context!!, workerParameters!!).doWork() shouldNotBeEqualTo ListenableWorker.Result.retry()
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
        worker.doWork() shouldNotBeEqualTo ListenableWorker.Result.retry()
        val buffer = Buffer()
        worker.responseCall!!.request().body()!!.writeTo(buffer)
        buffer.readUtf8().shouldContainAll(
                "\"id\":\"tracking1\",\"type\":2",
                "\"id\":\"user1\",\"type\":3")
    }

    private fun retrieveValidConfig() {
        val mockMessageScheduler = Mockito.mock(MessageMixerPingScheduler::class.java)
        val worker = ConfigWorker(context, workerParameters, HostAppInfoRepository.instance(),
                ConfigResponseRepository.instance(), mockMessageScheduler)
        worker.doWork()
    }

    companion object {

        private const val MIXER_RESPONSE = """
            {
                "currentPingMillis":1583890595467,
                "data":[{
                    "campaignData":{
                        "campaignId":"1234567890",
                        "isTest":false,
                        "maxImpressions":190,
                        "messagePayload":{
                            "backgroundColor":"#000000",
                            "frameColor":"#ffffff",
                            "header":"DEV-Test (Android In-App-Test) - Login",
                            "headerColor":"#ffffff",
                            "messageBody":"Login Test",
                            "messageBodyColor":"#ffffff",
                            "messageSettings":{
                                "controlSettings":{
                                    "buttons":[{
                                        "buttonBackgroundColor":"#000000",
                                        "buttonTextColor":"#ffffff",
                                        "buttonText":"Test",
                                        "buttonBehavior":{
                                            "action":1,
                                            "uri":"https://en.wikipedia.org/wiki/Test"
                                        },
                                        "campaignTrigger":{
                                            "type":1,
                                            "eventType":1,
                                            "eventName":"event",
                                            "attributes":[{
                                                "name":"attribute",
                                                "value":"attrValue",
                                                "type":1,
                                                "operator":1
                                            }]
                                        }
                                    }]
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
                                "cropType":2,
                                "imageUrl":"https://sample.image.url/test.jpg"
                            },
                            "title":"DEV-Test (Android In-App-Test)",
                            "titleColor":"#000000"
                        },
                        "triggers":[
                            {
                            "eventName":"Launch the App Event",
                            "eventType":1,
                            "attributes":[],
                            "type":1
                            },
                            {
                            "eventName":"Login Event",
                            "eventType":2,
                            "attributes":[],
                            "type":1
                            }
                        ],
                        "type":2
                    }
                }],
                "nextPingMillis":3600000
            }"""
    }
}
