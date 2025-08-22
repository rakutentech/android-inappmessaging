package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.rakuten.tech.mobile.inappmessaging.runtime.*
import com.rakuten.tech.mobile.inappmessaging.runtime.api.MessageMixerRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.DisplayPermissionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ConfigResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ConfigResponseEndpoints
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.DisplayPermissionResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TooltipHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.SUBSCRIPTION_ID_HEADER
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ViewUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Call
import java.util.*
import kotlin.collections.ArrayList

/**
 * Test class for MessageReadinessManager.
 */
@RunWith(RobolectricTestRunner::class)
open class MessageReadinessManagerSpec : BaseTest() {
    @Before
    override fun setup() {
        super.setup()
        AccountRepository.instance().userInfoProvider = TestUserInfoProvider()
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                InAppMessagingTestConstants.APP_ID,
                InAppMessagingTestConstants.DEVICE_ID,
                InAppMessagingTestConstants.APP_VERSION,
                InAppMessagingTestConstants.SUB_KEY,
                InAppMessagingTestConstants.LOCALE,
            ),
        )

        ConfigResponseRepository.instance().addConfigResponse(
            ConfigResponseData(
                ConfigResponseEndpoints(displayPermission = DISPLAY_PERMISSION_URL), 100,
            ),
        )
        MessageReadinessManager.instance().clearMessages()
    }

    @Test
    fun `should return empty if no queued message for display`() {
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
    }

    @Test
    fun `should return empty if there are no events`() {
        createMessageList()

        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
    }

    @Test
    fun `should return test message`() {
        val messageList = ArrayList<Message>()
        messageList.add(TestDataHelper.createDummyMessage(campaignId = "1"))
        messageList.add(TestDataHelper.createDummyMessage(campaignId = "2", isTest = true, maxImpressions = 5))
        setMessagesList(messageList)

        MessageReadinessManager.instance().getNextDisplayMessage() shouldBeEqualTo listOf(messageList[1])
    }

    @Test
    fun `should return test message when impressions is infinite`() {
        val messageList = ArrayList<Message>()
        messageList.add(
            TestDataHelper
                .createDummyMessage(campaignId = "1", isTest = true, areImpressionsInfinite = true),
        )
        setMessagesList(messageList)

        MessageReadinessManager.instance().getNextDisplayMessage() shouldBeEqualTo listOf(messageList[0])
    }

    @Test
    fun `should return empty when impressions is not infinite`() {
        val messageList = ArrayList<Message>()
        messageList.add(
            TestDataHelper.createDummyMessage(
                campaignId = "1",
                isTest = true,
                maxImpressions = 0,
                areImpressionsInfinite = false,
            ),
        )
        setMessagesList(messageList)

        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
    }

    @Test
    fun `should get display permission request with all attributes`() {
        val message = TestDataHelper.createDummyMessage()
        val request = MessageReadinessManager.instance().getDisplayPermissionRequest(message)

        request.campaignId shouldBeEqualTo message.campaignId
        request.appVersion shouldBeEqualTo InAppMessagingTestConstants.APP_VERSION
        request.sdkVersion shouldBeEqualTo BuildConfig.VERSION_NAME
        request.locale shouldBeEqualTo InAppMessagingTestConstants.LOCALE.toString()
            .replace("_", "-")
            .lowercase(Locale.getDefault())
    }

    @Test
    fun `should next ready message be empty when no events and opted out`() {
        val messageList = ArrayList<Message>()
        val message = TestDataHelper.createDummyMessage(campaignId = "1").apply {
            isOptedOut = true
        }
        messageList.add(message)
        messageList.add(TestDataHelper.createDummyMessage(campaignId = "2"))
        messageList.add(TestDataHelper.createDummyMessage(campaignId = "3"))
        setMessagesList(messageList)

        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
    }

    @Test
    fun `should next ready message be empty with ping required`() {
        initializeInApp()
        createMessageList()
        ConfigResponseRepository.instance().addConfigResponse(
            Gson().fromJson(CONFIG_RESPONSE.trimIndent(), ConfigResponse::class.java).data,
        )
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                "rakuten.com.tech.mobile.test",
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                "2", InAppMessagingTestConstants.LOCALE,
            ),
        )
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
    }

    @Test
    fun `should next ready message be empty with empty display impression`() {
        initializeInApp()

        createMessageList()
        ConfigResponseRepository.instance().addConfigResponse(
            Gson().fromJson(CONFIG_RESPONSE_EMPTY.trimIndent(), ConfigResponse::class.java).data,
        )
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                "rakuten.com.tech.mobile.test",
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                "2", InAppMessagingTestConstants.LOCALE,
            ),
        )
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
    }

    @Test
    fun `should return empty for valid message when max impression`() {
        val messageList = ArrayList<Message>()
        messageList.add(TestDataHelper.createDummyMessage(campaignId = "10", maxImpressions = 0))
        setMessagesList(messageList)

        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
    }

    private fun createMessageList() {
        val messageList = ArrayList<Message>()
        messageList.add(TestDataHelper.createDummyMessage(campaignId = "1"))
        messageList.add(TestDataHelper.createDummyMessage(campaignId = "2"))
        messageList.add(TestDataHelper.createDummyMessage(campaignId = "3"))
        setMessagesList(messageList)
    }

    private fun setMessagesList(messages: ArrayList<Message>) {
        // simulate sync while updating last ping timestamp
        CampaignRepository.instance().syncWith(messages, LAST_PING_MILLIS)

        MessageReadinessManager.instance().clearMessages()
        for (message in messages) {
            MessageReadinessManager.instance().addMessageToQueue(message.campaignId)
        }
    }

    private fun initializeInApp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID, "test_device_id",
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
    }

    companion object {
        internal const val DISPLAY_PERMISSION_URL = "https:/host/display_permission/"
        private const val LAST_PING_MILLIS = 123456L
        private const val CONFIG_RESPONSE = """{
            "data":{
                "enabled":true,
                "endpoints":{
                    "displayPermission":"https://sample.display.permission",
                    "impression":"https://sample.impression",
                    "ping":"https://sample.ping"
                }
            }
        }"""
        private const val CONFIG_RESPONSE_EMPTY = """{
            "data":{
                "enabled":true,
                "endpoints":{
                    "displayPermission":"",
                    "impression":"https://sample.impression",
                    "ping":"https://sample.ping"
                }
            }
        }"""
    }
}

@SuppressWarnings("LongMethod")
class MessageReadinessManagerRequestSpec : BaseTest() {
    private val server = MockWebServer()

    @Before
    override fun setup() {
        super.setup()
        AccountRepository.instance().userInfoProvider = TestUserInfoProvider()
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                InAppMessagingTestConstants.APP_ID,
                InAppMessagingTestConstants.DEVICE_ID,
                InAppMessagingTestConstants.APP_VERSION,
                InAppMessagingTestConstants.SUB_KEY,
                InAppMessagingTestConstants.LOCALE,
            ),
        )
        ConfigResponseRepository.instance().addConfigResponse(
            ConfigResponseData(
                ConfigResponseEndpoints(displayPermission = server.url("client").toString()), 100,
            ),
        )
        setMessagesList(arrayListOf(TestDataHelper.createDummyMessage(campaignId = CAMPAIGN_ID)))
        InAppMessaging.errorCallback = null
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun `should retry on network error`() {
        InAppMessaging.errorCallback = {
            // ignore
        }
        verifyFailedResponse(false)
    }

    @Test
    fun `should retry once for 500 response code`() {
        InAppMessaging.errorCallback = {
            // ignore
        }
        val mockResponse = MockResponse().setResponseCode(500)
        server.enqueue(mockResponse)
        server.enqueue(mockResponse)
        verifyFailedResponse(false)
    }

    @Test
    fun `should return valid on retry after first 500 response code`() {
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(200).setBody(DISPLAY_RESPONSE))
        verifyValidResponse(MessageReadinessManager.instance().getNextDisplayMessage()[0])
    }

    @Test
    fun `should not retry for 4xx response code`() {
        val mockResponse = MockResponse().setResponseCode(400)
        server.enqueue(mockResponse)
        verifyFailedResponse(true)
    }

    @Test
    fun `should return valid message`() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(DISPLAY_RESPONSE)
        server.enqueue(mockResponse)
        verifyValidResponse(MessageReadinessManager.instance().getNextDisplayMessage()[0])
    }

    @Test
    fun `should return remove message`() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(DISPLAY_RESPONSE)
        server.enqueue(mockResponse)
        val message = MessageReadinessManager.instance().getNextDisplayMessage()[0]
        verifyValidResponse(message)
    }

    @Test
    fun `should return null if display not allowed`() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(NOT_DISPLAY_RESPONSE)
        server.enqueue(mockResponse)
        verifyFailedResponse(true)
    }

    @Test
    fun `should return empty on valid response but need ping`() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(DISPLAY_PING_RESPONSE)
        server.enqueue(mockResponse)
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
    }

    private fun setMessagesList(messages: ArrayList<Message>) {
        CampaignRepository.instance().syncWith(messages, 0)

        MessageReadinessManager.instance().clearMessages()
        for (message in messages) {
            MessageReadinessManager.instance().addMessageToQueue(message.campaignId)
        }
    }

    private fun verifyFailedResponse(isRetry: Boolean) {
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
        MessageReadinessManager.shouldRetry.get() shouldBeEqualTo isRetry
    }

    private fun verifyValidResponse(message: Message?) {
        message.shouldNotBeNull()
        message.campaignId shouldBeEqualTo CAMPAIGN_ID
    }

    companion object {
        private const val DISPLAY_RESPONSE = "{\"display\":true, \"performPing\":false}"
        private const val NOT_DISPLAY_RESPONSE = "{\"display\":false, \"performPing\":false}"
        private const val DISPLAY_PING_RESPONSE = "{\"display\":true, \"performPing\":true}"
        private const val CAMPAIGN_ID = "1"
    }
}

class MessageReadinessManagerCallSpec : MessageReadinessManagerSpec() {
    private var mockRequest = mock(DisplayPermissionRequest::class.java)

    @Test
    fun `should response call contain three headers`() {
        val responseCall: Call<DisplayPermissionResponse> =
            MessageReadinessManager.instance().getDisplayCall(DISPLAY_PERMISSION_URL, mockRequest)
        responseCall.request().headers().size() shouldBeEqualTo 3
    }

    @Test
    fun `should add token to header`() {
        val responseCall: Call<DisplayPermissionResponse> =
            MessageReadinessManager.instance().getDisplayCall(DISPLAY_PERMISSION_URL, mockRequest)
        responseCall.request().header(MessageMixerRetrofitService.ACCESS_TOKEN_HEADER) shouldBeEqualTo
            "OAuth2 " + TestUserInfoProvider.TEST_USER_ACCESS_TOKEN
    }

    @Test
    fun `should add sub id header`() {
        val responseCall: Call<DisplayPermissionResponse> =
            MessageReadinessManager.instance().getDisplayCall(DISPLAY_PERMISSION_URL, mockRequest)
        responseCall.request().header(SUBSCRIPTION_ID_HEADER) shouldBeEqualTo
            InAppMessagingTestConstants.SUB_KEY
    }
}

@RunWith(RobolectricTestRunner::class)
class MessageReadinessTooltipSpec {
    private val manager = MessageReadinessManager(
        campaignRepo = mock(CampaignRepository::class.java),
        configResponseRepo = mock(ConfigResponseRepository::class.java),
        hostAppInfoRepo = mock(HostAppInfoRepository::class.java),
        accountRepo = mock(AccountRepository::class.java),
        pingScheduler = mock(MessageMixerPingScheduler::class.java),
        viewUtil = mock(ViewUtil::class.java),
    )

    private val mockActivity = mock(Activity::class.java)
    private val testTooltip = TooltipHelper.createMessage().copy(isTest = true)

    @Before
    fun setup() {
        `when`(manager.campaignRepo.messages).thenReturn(linkedMapOf(testTooltip.campaignId to testTooltip))
        `when`(manager.hostAppInfoRepo.getRegisteredActivity()).thenReturn(mockActivity)
        `when`(manager.hostAppInfoRepo.getContext()).thenReturn(mock(Context::class.java))
        `when`(manager.configResponseRepo.getDisplayPermissionEndpoint()).thenReturn("http://sample")
    }

    @After
    fun tearDown() {
        manager.clearMessages()
    }

    @Test
    fun `should not add message if does not exist in list`() {
        `when`(manager.campaignRepo.messages).thenReturn(linkedMapOf())

        manager.addMessageToQueue("sample-id")
        manager.getNextDisplayMessage().shouldBeEmpty()
    }

    @Test
    fun `should remove message`() {
        val campaign = TestDataHelper.createDummyMessage()
        `when`(manager.campaignRepo.messages).thenReturn(linkedMapOf(campaign.campaignId to campaign))

        manager.removeMessageFromQueue(campaign.campaignId)
        manager.getNextDisplayMessage().shouldBeEmpty()
    }

    @Test
    fun `should remove tooltip message`() {
        `when`(manager.campaignRepo.messages).thenReturn(linkedMapOf(testTooltip.campaignId to testTooltip))

        manager.removeMessageFromQueue(testTooltip.campaignId)
        manager.getNextDisplayMessage().shouldBeEmpty()
    }

    @Test
    fun `should return empty if message does not exist anymore`() {
        manager.addMessageToQueue(testTooltip.campaignId)

        `when`(manager.campaignRepo.messages).thenReturn(linkedMapOf())
        manager.getNextDisplayMessage().shouldBeEmpty()
    }

    @Test
    fun `should return tooltip if target is visible when calling getNextDisplayMessage()`() {
        manager.addMessageToQueue(testTooltip.campaignId)
        `when`(manager.viewUtil.isViewByNameVisible(mockActivity, testTooltip.getTooltipConfig()!!.id))
            .thenReturn(true)

        manager.getNextDisplayMessage().shouldContain(testTooltip)
    }

    @Test
    fun `should not return tooltip if target is not visible when calling getNextDisplayMessage()`() {
        `when`(manager.viewUtil.isViewByNameVisible(mockActivity, testTooltip.getTooltipConfig()!!.id))
            .thenReturn(false)
        manager.addMessageToQueue(testTooltip.campaignId)

        manager.getNextDisplayMessage().shouldNotContain(testTooltip)
    }

    @Test
    fun `should not return tooltip if opted-out when calling getNextDisplayMessage()`() {
        val testTooltip = testTooltip.apply { isOptedOut = true }
        `when`(manager.campaignRepo.messages).thenReturn(linkedMapOf(testTooltip.campaignId to testTooltip))
        manager.addMessageToQueue(testTooltip.campaignId)

        manager.getNextDisplayMessage().shouldNotContain(testTooltip)
    }

    @Test
    fun `should not return tooltip if activity becomes null when calling getNextDisplayMessage()`() {
        `when`(manager.hostAppInfoRepo.getRegisteredActivity()).thenReturn(null)
        manager.addMessageToQueue(testTooltip.campaignId)

        manager.getNextDisplayMessage().shouldNotContain(testTooltip)
    }

    @Test
    fun `should not return tooltip if tooltip config is null when calling getNextDisplayMessage()`() {
        val testTooltip = TestDataHelper.createDummyMessage(type = InAppMessageType.TOOLTIP.typeId)
        `when`(manager.campaignRepo.messages).thenReturn(linkedMapOf(testTooltip.campaignId to testTooltip))
        manager.addMessageToQueue(testTooltip.campaignId)

        manager.getNextDisplayMessage().shouldNotContain(testTooltip)
    }

    @Test
    fun `should get display permission request for tooltip`() {
        `when`(manager.campaignRepo.lastSyncMillis).thenReturn(null)
        val message = TooltipHelper.createMessage()
        val request = manager.getDisplayPermissionRequest(message)

        request.lastPingInMillis.shouldBeEqualTo(0)
    }

    @Test
    fun `should get display call for tooltip`() {
        `when`(manager.hostAppInfoRepo.getSubscriptionKey()).thenReturn("test-key")
        `when`(manager.hostAppInfoRepo.getDeviceId()).thenReturn("duMMyDeviceId")
        `when`(manager.accountRepo.getAccessToken()).thenReturn("test-token")
        val request = manager.getDisplayPermissionRequest(testTooltip)

        val call = manager.getDisplayCall("test-url", request)
        call.request().headers().size() shouldBeEqualTo 3
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class MessageReadinessPushPrimerSpec {

    private val manager = MessageReadinessManager(
        campaignRepo = mock(CampaignRepository::class.java),
        configResponseRepo = mock(ConfigResponseRepository::class.java),
        hostAppInfoRepo = mock(HostAppInfoRepository::class.java),
        accountRepo = mock(AccountRepository::class.java),
        pingScheduler = mock(MessageMixerPingScheduler::class.java),
        viewUtil = mock(ViewUtil::class.java),
    )

    private val mockContext = mock(Context::class.java)
    private val testPushPrimer = TestDataHelper.createDummyMessage(
        isTest = true,
        customJson = JsonParser.parseString("""{"pushPrimer": { "button": 1 }}""").asJsonObject,
    )
    private val mockRmcHelper = mockStatic(RmcHelper::class.java)

    @Before
    fun setup() {
        `when`(manager.campaignRepo.messages).thenReturn(linkedMapOf(testPushPrimer.campaignId to testPushPrimer))
        `when`(manager.hostAppInfoRepo.getContext()).thenReturn(mockContext)
        mockRmcHelper.`when`<Any> { RmcHelper.isRmcIntegrated() }.thenReturn(true)
    }

    @After
    fun tearDown() {
        manager.clearMessages()
        mockRmcHelper.close()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `should not return push primer campaign if device is below Android 13()`() {
        manager.addMessageToQueue(testPushPrimer.campaignId)

        manager.getNextDisplayMessage() shouldNotContain testPushPrimer
    }

    @Test
    fun `should not return push primer campaign if permission is granted()`() {
        `when`(mockContext.checkSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_GRANTED)
        manager.addMessageToQueue(testPushPrimer.campaignId)

        manager.getNextDisplayMessage() shouldNotContain testPushPrimer
    }

    @Test
    fun `should return push primer campaign if device is supported and permission is not yet granted()`() {
        `when`(mockContext.checkSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_DENIED)
        manager.addMessageToQueue(testPushPrimer.campaignId)

        manager.getNextDisplayMessage() shouldContain testPushPrimer
    }
}
