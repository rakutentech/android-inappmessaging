package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.SharedPreferencesUtil
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test class for ReadyForDisplayMessageRepository.
 */
@RunWith(RobolectricTestRunner::class)
class ReadyForDisplayMessageRepositorySpec : BaseTest() {
    private val messageList = ArrayList<Message>()
    private val message0 = ValidTestMessage()
    private val message1 = ValidTestMessage("1234")

    @Before
    override fun setup() {
        super.setup()
        ReadyForDisplayMessageRepository.instance().clearMessages()
        PingResponseMessageRepository.instance().clearMessages()
        message0.timesClosed = 0
        message1.timesClosed = 0
        messageList.add(message0)
        messageList.add(message1)
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
    }

    @Test
    fun `should be empty after clearing`() {
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        ReadyForDisplayMessageRepository.instance().clearMessages()
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)
    }

    @Test
    fun `should not crash while clearing messages`() {
        InAppMessaging.setUninitializedInstance(true)
        ReadyForDisplayMessageRepository.instance().clearMessages()
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)
    }

    @Test
    fun `should contain correct messages after clearing then adding`() {
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        ReadyForDisplayMessageRepository.instance().clearMessages()
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)

        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
    }

    @Test
    fun `should remove matching message and increment times closed`() {
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        ReadyForDisplayMessageRepository.instance().removeMessage(message0.getCampaignId(), true)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(1)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            if (msg.getCampaignId() == message0.getCampaignId()) {
                msg.getNumberOfTimesClosed() shouldBeEqualTo 1
            } else {
                msg.getNumberOfTimesClosed() shouldBeEqualTo 0
            }
        }
    }

    @Test
    fun `should remove matching message but not increment`() {
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        ReadyForDisplayMessageRepository.instance().removeMessage(message0.getCampaignId(), false)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(1)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
        }
    }

    @Test
    fun `should not remove and not increment when no match`() {
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        ReadyForDisplayMessageRepository.instance().removeMessage("4321", false)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
        }
    }

    @Test
    fun `should not remove and not increment when no match and flag true`() {
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        ReadyForDisplayMessageRepository.instance().removeMessage("4321", true)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
        }
    }

    @Test
    fun `should clear messages and increment times closed`() {
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        ReadyForDisplayMessageRepository.instance().clearMessages(true)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldBeEmpty()
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 1
        }
    }

    @Test
    fun `should clear messages but not increment times closed`() {
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        ReadyForDisplayMessageRepository.instance().clearMessages(false)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldBeEmpty()
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
        }
    }

    @Test
    fun `should save and restore values for different users`() {
        setupAndTestMultipleUser()
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
    }

    @Test
    fun `should not crash and clear previous when forced cast exception`() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)
        PreferencesUtil.putFloat(
            ApplicationProvider.getApplicationContext(),
            SharedPreferencesUtil.getPreferencesFile(),
            ReadyForDisplayMessageRepository.READY_DISPLAY_KEY,
            1.0f
        )
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldBeEmpty()
    }

    @Test
    fun `should not crash and reset map`() {
        setupAndTestMultipleUser()
        InAppMessaging.setUninitializedInstance(true)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldBeEmpty()
    }

    private fun setupAndTestMultipleUser() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)

        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        infoProvider.userId = "user2"
        AccountRepository.instance().updateUserInfo()
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldBeEmpty()

        // revert to initial user info
        infoProvider.userId = TestUserInfoProvider.TEST_USER_ID
        AccountRepository.instance().updateUserInfo()
    }

    private fun initializeInstance(infoProvider: UserInfoProvider) {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        InAppMessaging.instance().registerPreference(infoProvider)
    }
}
