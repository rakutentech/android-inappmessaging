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
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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
class MessageReadinessManagerSpec : BaseTest() {

    private var mockRequest = Mockito.mock(DisplayPermissionRequest::class.java)
    private var testMessage = Mockito.mock(Message::class.java)
    private var configResponseData = Mockito.mock(ConfigResponseData::class.java)
    private var configResponseEndpoints = Mockito.mock(ConfigResponseEndpoints::class.java)

    @Before
    fun setup() {
        AccountRepository.instance().userInfoProvider = TestUserInfoProvider()
        HostAppInfoRepository.instance().addHostInfo(HostAppInfo(
                InAppMessagingTestConstants.APP_ID,
                InAppMessagingTestConstants.DEVICE_ID,
                InAppMessagingTestConstants.APP_VERSION,
                InAppMessagingTestConstants.SUB_KEY,
                InAppMessagingTestConstants.LOCALE))
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        PingResponseMessageRepository.instance().lastPingMillis = LAST_PING_MILLIS
        When calling configResponseData.endpoints itReturns configResponseEndpoints
        When calling configResponseEndpoints.displayPermission itReturns DISPLAY_PERMISSION_URL
        When calling testMessage.isTest() itReturns true
        When calling testMessage.getCampaignId() itReturns "2"
        When calling testMessage.getMaxImpressions() itReturns 1
    }

    @Test
    fun `should return null if no message in ready repo`() {
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeNull()
    }

    @Test
    fun `should next ready message be null when no events`() {
        val messageList = ArrayList<Message>()
        messageList.add(ValidTestMessage("1", false))
        messageList.add(ValidTestMessage("2", false))
        messageList.add(ValidTestMessage("3", false))
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
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
                .replace("_", "-").toLowerCase(Locale.getDefault())
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
        responseCall.request().header(MessageMixerRetrofitService.RAE_TOKEN_HEADER) shouldBeEqualTo
                "OAuth2 " + TestUserInfoProvider.TEST_USER_RAE_TOKEN
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
    }

    @Test
    fun `should next ready message be null with ping required`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID, "test_device_id")
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test", "",
                isDebugLogging = true, isForTesting = true)

        val messageList = ArrayList<Message>()
        // will not be displayed when campaign expires)
        messageList.add(ValidTestMessage("1", false))
        messageList.add(ValidTestMessage("2", false))
        messageList.add(ValidTestMessage("3", false))
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        ConfigResponseRepository.instance().addConfigResponse(
                Gson().fromJson(CONFIG_RESPONSE.trimIndent(), ConfigResponse::class.java).data)
        HostAppInfoRepository.instance().addHostInfo(HostAppInfo("rakuten.com.tech.mobile.test",
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                "2", InAppMessagingTestConstants.LOCALE))
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeNull()
    }

    @Test
    fun `should next ready message be null with empty display impression`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID, "test_device_id")
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test", "",
                isDebugLogging = true, isForTesting = true)

        val messageList = ArrayList<Message>()
        // will not be displayed when campaign expires)
        messageList.add(ValidTestMessage("1", false))
        messageList.add(ValidTestMessage("2", false))
        messageList.add(ValidTestMessage("3", false))
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        ConfigResponseRepository.instance().addConfigResponse(
                Gson().fromJson(CONFIG_RESPONSE_EMPTY.trimIndent(), ConfigResponse::class.java).data)
        HostAppInfoRepository.instance().addHostInfo(HostAppInfo("rakuten.com.tech.mobile.test",
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                "2", InAppMessagingTestConstants.LOCALE))
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeNull()
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
