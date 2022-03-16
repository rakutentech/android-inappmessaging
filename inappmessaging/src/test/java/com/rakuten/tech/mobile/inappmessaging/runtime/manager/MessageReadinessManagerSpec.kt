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

    private var mockRequest = Mockito.mock(DisplayPermissionRequest::class.java)
    private var testMessage = Mockito.mock(Message::class.java)
    private var configResponseData = Mockito.mock(ConfigResponseData::class.java)
    private var configResponseEndpoints = Mockito.mock(ConfigResponseEndpoints::class.java)

    @Before
    override fun setup() {
        super.setup()
        AccountRepository.instance().userInfoProvider = TestUserInfoProvider()
        HostAppInfoRepository.instance().addHostInfo(HostAppInfo(
                InAppMessagingTestConstants.APP_ID,
                InAppMessagingTestConstants.DEVICE_ID,
                InAppMessagingTestConstants.APP_VERSION,
                InAppMessagingTestConstants.SUB_KEY,
                InAppMessagingTestConstants.LOCALE))
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        PingResponseMessageRepository.instance().lastPingMillis = LAST_PING_MILLIS
        `when`(configResponseData.endpoints).thenReturn(configResponseEndpoints)
        `when`(configResponseEndpoints.displayPermission).thenReturn(DISPLAY_PERMISSION_URL)
        `when`(testMessage.isTest()).thenReturn(true)
        `when`(testMessage.getCampaignId()).thenReturn("2")
        `when`(testMessage.getMaxImpressions()).thenReturn(1)
    }

    @Test
    fun `should return null if no message in ready repo`() {
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeNull()
    }

    @Test
    fun `should next ready message be null when no events`() {
        createMessageList()
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeNull()
    }

    @Test
    fun `should return test message`() {
        val messageList = ArrayList<Message>()
        messageList.add(ValidTestMessage("1", false))
        messageList.add(testMessage)
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        MessageReadinessManager.instance().getNextDisplayMessage() shouldBeEqualTo testMessage
    }

    @Test
    fun `should get display permission request with all attributes`() {
        val message = ValidTestMessage()
        val request = MessageReadinessManager.instance().getDisplayPermissionRequest(message)
        request.campaignId shouldBeEqualTo message.getCampaignId()
        request.appVersion shouldBeEqualTo InAppMessagingTestConstants.APP_VERSION
        request.sdkVersion shouldBeEqualTo BuildConfig.VERSION_NAME
        request.lastPingInMillis shouldBeEqualTo LAST_PING_MILLIS
        request.locale shouldBeEqualTo InAppMessagingTestConstants.LOCALE.toString()
                .replace("_", "-").lowercase(Locale.getDefault())
    }

    @Test
    fun `should response call contain two headers`() {
        val responseCall: Call<DisplayPermissionResponse> =
                MessageReadinessManager.instance().getDisplayPermissionResponseCall(DISPLAY_PERMISSION_URL, mockRequest)
        responseCall.request().headers().size() shouldBeEqualTo 2
    }

    @Test
    fun `should add token to header`() {
        val responseCall: Call<DisplayPermissionResponse> =
                MessageReadinessManager.instance().getDisplayPermissionResponseCall(DISPLAY_PERMISSION_URL, mockRequest)
        responseCall.request().header(MessageMixerRetrofitService.ACCESS_TOKEN_HEADER) shouldBeEqualTo
                "OAuth2 " + TestUserInfoProvider.TEST_USER_ACCESS_TOKEN
    }

    @Test
    fun `should add sub id header`() {
        val responseCall: Call<DisplayPermissionResponse> =
                MessageReadinessManager.instance().getDisplayPermissionResponseCall(DISPLAY_PERMISSION_URL, mockRequest)
        responseCall.request().header(MessageMixerRetrofitService.SUBSCRIPTION_ID_HEADER) shouldBeEqualTo
                InAppMessagingTestConstants.SUB_KEY
    }

    @Test
    fun `should next ready message be null when no events and opted out `() {
        val messageList = ArrayList<Message>()
        val message = ValidTestMessage("1", false)
        messageList.add(message)
        messageList.add(ValidTestMessage("2", false))
        messageList.add(ValidTestMessage("3", false))
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        LocalOptedOutMessageRepository.instance().addMessage(message)
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeNull()
        MessageReadinessManager.shouldRetry.get().shouldBeFalse()
    }

    @Test
    fun `should next ready message be null with ping required`() {
        initializeInApp()
        createMessageList()
        ConfigResponseRepository.instance().addConfigResponse(
                Gson().fromJson(CONFIG_RESPONSE.trimIndent(), ConfigResponse::class.java).data)
        HostAppInfoRepository.instance().addHostInfo(HostAppInfo("rakuten.com.tech.mobile.test",
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                "2", InAppMessagingTestConstants.LOCALE))
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeNull()
    }

    @Test
    fun `should next ready message be null with empty display impression`() {
        initializeInApp()

        createMessageList()
        ConfigResponseRepository.instance().addConfigResponse(
                Gson().fromJson(CONFIG_RESPONSE_EMPTY.trimIndent(), ConfigResponse::class.java).data)
        HostAppInfoRepository.instance().addHostInfo(HostAppInfo("rakuten.com.tech.mobile.test",
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                "2", InAppMessagingTestConstants.LOCALE))
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeNull()
    }

    private fun createMessageList() {
        val messageList = ArrayList<Message>()
        messageList.add(ValidTestMessage("1", false))
        messageList.add(ValidTestMessage("2", false))
        messageList.add(ValidTestMessage("3", false))
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
    }

    @Test
    fun `should return null for valid message when max impression`() {
        val message = ValidTestMessage("10", false)
        message.setMaxImpression(0)
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(
            arrayListOf(message))
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeNull()
    }

    private fun initializeInApp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID, "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
    }

    companion object {
        private const val DISPLAY_PERMISSION_URL = "https:/host/display_permission/"
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
        HostAppInfoRepository.instance().addHostInfo(HostAppInfo(
            InAppMessagingTestConstants.APP_ID,
            InAppMessagingTestConstants.DEVICE_ID,
            InAppMessagingTestConstants.APP_VERSION,
            InAppMessagingTestConstants.SUB_KEY,
            InAppMessagingTestConstants.LOCALE))
        ConfigResponseRepository.instance().addConfigResponse(data)
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(
            arrayListOf(ValidTestMessage(CAMPAIGN_ID, false)))
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
        verifyValidResponse(MessageReadinessManager.instance().getNextDisplayMessage())
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
        verifyValidResponse(MessageReadinessManager.instance().getNextDisplayMessage())
    }

    @Test
    fun `should return null if display not allowed`() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(NOT_DISPLAY_RESPONSE)
        server.enqueue(mockResponse)
        verifyFailedResponse(true)
    }

    @Test
    fun `should return null on valid response but need ping`() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(DISPLAY_PING_RESPONSE)
        server.enqueue(mockResponse)
        val message = MessageReadinessManager.instance().getNextDisplayMessage()
        message.shouldBeNull()
    }

    private fun verifyFailedResponse(isRetry: Boolean) {
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeNull()
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
