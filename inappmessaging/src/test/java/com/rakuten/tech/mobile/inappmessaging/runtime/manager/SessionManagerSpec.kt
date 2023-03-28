package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ConfigResponseEndpoints
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.SessionManager.onSessionUpdate
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.ExecutionException
import kotlin.collections.ArrayList

/**
 * Test class for SessionManager.
 */
@RunWith(RobolectricTestRunner::class)
class SessionManagerSpec : BaseTest() {

    private var configResponseData = Mockito.mock(ConfigResponseData::class.java)
    private var endpoints = Mockito.mock(ConfigResponseEndpoints::class.java)
    private val message = TestDataHelper.createDummyMessage()

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun `should start ping message mixer on log in successful`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id",
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
        `when`(configResponseData.rollOutPercentage).thenReturn(100)
        `when`(configResponseData.endpoints).thenReturn(endpoints)
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        onSessionUpdate()
        WorkManager.getInstance(ApplicationProvider.getApplicationContext())
            .getWorkInfosByTag(MESSAGE_MIXER_PING_WORKER)
            .get()
            .shouldHaveSize(1)
    }

    @Test
    fun `should clear repository with null event`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id",
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
        `when`(configResponseData.rollOutPercentage).thenReturn(0)

        addTestData()
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        onSessionUpdate()
        verifyTestData()
    }

    @Test
    fun `should clear repository with valid event`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id",
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
        `when`(configResponseData.rollOutPercentage).thenReturn(0)

        addTestData()
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        onSessionUpdate()
        verifyTestData()
    }

    @Test
    fun `should clear repository with valid persistent event`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id",
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
        `when`(configResponseData.rollOutPercentage).thenReturn(0)

        addTestData()
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        onSessionUpdate()
        verifyTestData()
    }

    @Test
    fun `should update repository data when user changes`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID, "test_device_id",
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())

        val infoProvider = TestUserInfoProvider() // test_user_id
        InAppMessaging.instance().registerPreference(infoProvider)

        CampaignRepository.instance().syncWith(listOf(TestDataHelper.createDummyMessage()), 0)
        CampaignRepository.instance().messages.shouldHaveSize(1)

        // Simulate change user
        infoProvider.userId = "test_user_id_2"
        AccountRepository.instance().updateUserInfo()
        onSessionUpdate()

        CampaignRepository.instance().messages.shouldBeEmpty()
    }

    private fun addTestData() {
        // Add messages
        val messageList = ArrayList<Message>()
        messageList.add(message)
        CampaignRepository.instance().syncWith(messageList, 0)
    }

    private fun verifyTestData() {
        // Cleared campaigns
        CampaignRepository.instance().messages.shouldHaveSize(0)
        // No ready campaigns
        MessageReadinessManager.instance().getNextDisplayMessage().shouldBeEmpty()
    }

    companion object {
        private const val MESSAGE_MIXER_PING_WORKER = "iam_message_mixer_worker"
    }
}
