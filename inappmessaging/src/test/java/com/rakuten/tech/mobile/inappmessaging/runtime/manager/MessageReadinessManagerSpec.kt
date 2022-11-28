package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.*
import com.rakuten.tech.mobile.inappmessaging.runtime.api.MessageMixerRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.DisplayPermissionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseEndpoints
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.displaypermission.DisplayPermissionResponse
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Call
import java.util.*
import kotlin.collections.ArrayList

/**
 * Test class for MessageReadinessManager.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
open class MessageReadinessManagerSpec : BaseTest() {
    private var configResponseData = Mockito.mock(ConfigResponseData::class.java)
    private var configResponseEndpoints = Mockito.mock(ConfigResponseEndpoints::class.java)

    @Before
    override fun setup() {
        super.setup()
        AccountRepository.instance().userInfoProvider = TestUserInfoProvider()
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        `when`(configResponseData.endpoints).thenReturn(configResponseEndpoints)
        `when`(configResponseEndpoints.displayPermission).thenReturn(DISPLAY_PERMISSION_URL)
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
        messageList.add(ValidTestMessage("1", false))
        messageList.add(ValidTestMessage("2", true, maxImpressions = 5))
        setMessagesList(messageList)

        MessageReadinessManager.instance().getNextDisplayMessage() shouldBeEqualTo listOf(messageList[1])
    }

    @Test
    fun `should return test message when impressions is infinite`() {
        val messageList = ArrayList<Message>()
        messageList.add(ValidTestMessage("1", true, infiniteImpressions = true))
        setMessagesList(messageList)

        MessageReadinessManager.instance().getNextDisplayMessage() shouldBeEqualTo listOf(messageList[0])
    }

    @Test
    fun `should return empty when impressions is not infinite`() {
        val messageList = ArrayList<Message>()
        messageList.add(ValidTestMessage("1", true, maxImpressions = 0, infiniteImpressions = false))
        setMessagesList(messageList)

        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
    }

    @Test
    fun `should get display permission request with all attributes`() {
        val message = ValidTestMessage()
        val request = MessageReadinessManager.instance().getDisplayPermissionRequest(message)

        request.campaignId shouldBeEqualTo message.getCampaignId()
        request.appVersion shouldBeEqualTo InAppMessagingTestConstants.APP_VERSION
        request.sdkVersion shouldBeEqualTo BuildConfig.VERSION_NAME
        request.locale shouldBeEqualTo InAppMessagingTestConstants.LOCALE.toString()
            .replace("_", "-")
            .lowercase(Locale.getDefault())
    }

    @Test
    fun `should next ready message be empty when no events and opted out`() {
        val messageList = ArrayList<Message>()
        val message = ValidTestMessage("1", false).apply {
            isOptedOut = true
        }
        messageList.add(message)
        messageList.add(ValidTestMessage("2", false))
        messageList.add(ValidTestMessage("3", false))
        setMessagesList(messageList)

        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
        MessageReadinessManager.shouldRetry.get().shouldBeFalse()
    }

    @Test
    fun `should next ready message be empty with ping required`() {
        initializeInApp()
        createMessageList()
        ConfigResponseRepository.instance().addConfigResponse(
            Gson().fromJson(CONFIG_RESPONSE.trimIndent(), ConfigResponse::class.java).data
        )
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                "rakuten.com.tech.mobile.test",
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                "2", InAppMessagingTestConstants.LOCALE
            )
        )
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
    }

    @Test
    fun `should next ready message be empty with empty display impression`() {
        initializeInApp()

        createMessageList()
        ConfigResponseRepository.instance().addConfigResponse(
            Gson().fromJson(CONFIG_RESPONSE_EMPTY.trimIndent(), ConfigResponse::class.java).data
        )
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                "rakuten.com.tech.mobile.test",
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                "2", InAppMessagingTestConstants.LOCALE
            )
        )
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
    }

    @Test
    fun `should return empty for valid message when max impression`() {
        val messageList = ArrayList<Message>()
        messageList.add(ValidTestMessage("10", false, maxImpressions = 0))
        setMessagesList(messageList)

        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
    }

    private fun createMessageList() {
        val messageList = ArrayList<Message>()
        messageList.add(ValidTestMessage("1", false))
        messageList.add(ValidTestMessage("2", false))
        messageList.add(ValidTestMessage("3", false))
        setMessagesList(messageList)
    }

    private fun setMessagesList(messages: ArrayList<Message>) {
        // simulate sync while updating last ping timestamp
        CampaignRepository.instance().syncWith(messages, LAST_PING_MILLIS)

        MessageReadinessManager.instance().clearMessages()
        for (message in messages) {
            MessageReadinessManager.instance().addMessageToQueue(message.getCampaignId())
        }
    }

    private fun initializeInApp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID, "test_device_id"
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

class MessageReadinessManagerRequestSpec : BaseTest() {
    private val server = MockWebServer()
    private var data = Mockito.mock(ConfigResponseData::class.java)
    private var endpoint = Mockito.mock(ConfigResponseEndpoints::class.java)

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
                InAppMessagingTestConstants.LOCALE
            )
        )
        ConfigResponseRepository.instance().addConfigResponse(data)
        setMessagesList(arrayListOf(ValidTestMessage(CAMPAIGN_ID, false)))

        server.start()
        `when`(data.endpoints).thenReturn(endpoint)
        `when`(endpoint.displayPermission).thenReturn(server.url("client").toString())
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
        val mgr = MessageReadinessManager.instance()
        (mgr as MessageReadinessManager.MessageReadinessManagerImpl).queuedMessages.shouldHaveSize(1)
    }

    @Test
    fun `should return null for invalid id`() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(DISPLAY_RESPONSE)
        server.enqueue(mockResponse)
        val mgr = MessageReadinessManager.instance()
        mgr.clearMessages()
        (mgr as MessageReadinessManager.MessageReadinessManagerImpl).queuedMessages.add("invalid")
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
        mgr.clearMessages()
    }

    @Test
    fun `should return remove message`() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(DISPLAY_RESPONSE)
        server.enqueue(mockResponse)
        val message = MessageReadinessManager.instance().getNextDisplayMessage()[0]
        verifyValidResponse(message)
        val readinessMgr = MessageReadinessManager.instance()
        (readinessMgr as MessageReadinessManager.MessageReadinessManagerImpl).queuedMessages.shouldHaveSize(1)
        MessageReadinessManager.instance().removeMessageFromQueue(message!!.getCampaignId())
        readinessMgr.queuedMessages.shouldBeEmpty()
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
            MessageReadinessManager.instance().addMessageToQueue(message.getCampaignId())
        }
    }

    private fun verifyFailedResponse(isRetry: Boolean) {
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
        MessageReadinessManager.shouldRetry.get() shouldBeEqualTo isRetry
    }

    private fun verifyValidResponse(message: Message?) {
        message.shouldNotBeNull()
        message.getCampaignId() shouldBeEqualTo CAMPAIGN_ID
    }

    companion object {
        private const val DISPLAY_RESPONSE = "{\"display\":true, \"performPing\":false}"
        private const val NOT_DISPLAY_RESPONSE = "{\"display\":false, \"performPing\":false}"
        private const val DISPLAY_PING_RESPONSE = "{\"display\":true, \"performPing\":true}"
        private const val CAMPAIGN_ID = "1"
    }
}

class MessageReadinessManagerCallSpec : MessageReadinessManagerSpec() {
    private var mockRequest = Mockito.mock(DisplayPermissionRequest::class.java)

    @Test
    fun `should response call contain two headers`() {
        val responseCall: Call<DisplayPermissionResponse> =
            MessageReadinessManager.instance().getDisplayCall(DISPLAY_PERMISSION_URL, mockRequest)
        responseCall.request().headers().size() shouldBeEqualTo 2
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
        responseCall.request().header(MessageMixerRetrofitService.SUBSCRIPTION_ID_HEADER) shouldBeEqualTo
            InAppMessagingTestConstants.SUB_KEY
    }
}
