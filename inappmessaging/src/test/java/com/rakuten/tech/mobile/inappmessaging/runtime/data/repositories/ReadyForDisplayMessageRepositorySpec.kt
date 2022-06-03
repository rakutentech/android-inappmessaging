package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import org.amshove.kluent.*
import org.junit.Ignore
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test class for ReadyForDisplayMessageRepository.
 */
@Ignore
@RunWith(RobolectricTestRunner::class)
class ReadyForDisplayMessageRepositorySpec : BaseTest() {
    private val messageList = ArrayList<Message>()
    private val message0 = ValidTestMessage()
    private val message1 = ValidTestMessage("1234")

//    @Before
//    override fun setup() {
//        super.setup()
//        ReadyForDisplayMessageRepository.instance().clearMessages()
//        CampaignRepository.instance().clearMessages()
//        message0.timesClosed = 0
//        message1.timesClosed = 0
//        messageList.add(message0)
//        messageList.add(message1)
//        CampaignRepository.instance().syncWith(messageList, 0)
//    }
//
//    @Test
//    fun `should be empty after clearing`() {
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
//        ReadyForDisplayMessageRepository.instance().messages.shouldHaveSize(2)
//
//        ReadyForDisplayMessageRepository.instance().clearMessages()
//        ReadyForDisplayMessageRepository.instance().messages.shouldHaveSize(0)
//    }
//
//    @Test
//    fun `should not crash while clearing messages`() {
//        InAppMessaging.setNotConfiguredInstance(true)
//        ReadyForDisplayMessageRepository.instance().clearMessages()
//        ReadyForDisplayMessageRepository.instance().messages.shouldHaveSize(0)
//    }
//
//    @Test
//    fun `should contain correct messages after clearing then adding`() {
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
//        ReadyForDisplayMessageRepository.instance().messages.shouldHaveSize(2)
//
//        ReadyForDisplayMessageRepository.instance().clearMessages()
//        ReadyForDisplayMessageRepository.instance().messages.shouldHaveSize(0)
//
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
//        ReadyForDisplayMessageRepository.instance().messages.shouldHaveSize(2)
//    }
//
//    @Test
//    fun `should remove matching message and increment times closed`() {
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
//        ReadyForDisplayMessageRepository.instance().removeMessage(message0.getCampaignId(), true)
//        ReadyForDisplayMessageRepository.instance().messages.shouldHaveSize(1)
//        for (msg in CampaignRepository.instance().messages) {
//            if (msg.getCampaignId() == message0.getCampaignId()) {
//                msg.getNumberOfTimesClosed() shouldBeEqualTo 1
//            } else {
//                msg.getNumberOfTimesClosed() shouldBeEqualTo 0
//            }
//        }
//    }
//
//    @Test
//    fun `should remove matching message but not increment`() {
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
//        ReadyForDisplayMessageRepository.instance().removeMessage(message0.getCampaignId(), false)
//        ReadyForDisplayMessageRepository.instance().messages.shouldHaveSize(1)
//        for (msg in CampaignRepository.instance().messages) {
//            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
//        }
//    }
//
//    @Test
//    fun `should not remove and not increment when no match`() {
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
//        ReadyForDisplayMessageRepository.instance().removeMessage("4321", false)
//        ReadyForDisplayMessageRepository.instance().messages.shouldHaveSize(2)
//        for (msg in CampaignRepository.instance().messages) {
//            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
//        }
//    }
//
//    @Test
//    fun `should not remove and not increment when no match and flag true`() {
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
//        ReadyForDisplayMessageRepository.instance().removeMessage("4321", true)
//        ReadyForDisplayMessageRepository.instance().messages.shouldHaveSize(2)
//        for (msg in CampaignRepository.instance().messages) {
//            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
//        }
//    }
//
//    @Test
//    fun `should clear messages and increment times closed`() {
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
//        ReadyForDisplayMessageRepository.instance().clearMessages(true)
//        ReadyForDisplayMessageRepository.instance().messages.shouldBeEmpty()
//        for (msg in CampaignRepository.instance().messages) {
//            msg.getNumberOfTimesClosed() shouldBeEqualTo 1
//        }
//    }
//
//    @Test
//    fun `should clear messages but not increment times closed`() {
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
//        ReadyForDisplayMessageRepository.instance().clearMessages(false)
//        ReadyForDisplayMessageRepository.instance().messages.shouldBeEmpty()
//        for (msg in CampaignRepository.instance().messages) {
//            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
//        }
//    }
//
//    @Test
//    fun `should save and restore values for different users`() {
//        setupAndTestMultipleUser()
//        ReadyForDisplayMessageRepository.instance().messages.shouldHaveSize(2)
//    }
//
//    @Test
//    fun `should not crash and clear previous when forced cast exception`() {
//        val infoProvider = TestUserInfoProvider()
//        initializeInstance(infoProvider)
//        PreferencesUtil.putFloat(
//            ApplicationProvider.getApplicationContext(),
//            InAppMessaging.getPreferencesFile(),
//            ReadyForDisplayMessageRepository.READY_DISPLAY_KEY,
//            1.0f
//        )
//        ReadyForDisplayMessageRepository.instance().messages.shouldBeEmpty()
//    }
//
//    @Test
//    fun `should not crash and reset map`() {
//        setupAndTestMultipleUser()
//        InAppMessaging.setNotConfiguredInstance(true)
//        ReadyForDisplayMessageRepository.instance().messages.shouldBeEmpty()
//    }
//
//    private fun setupAndTestMultipleUser() {
//        val infoProvider = TestUserInfoProvider()
//        initializeInstance(infoProvider)
//
//        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
//        ReadyForDisplayMessageRepository.instance().messages.shouldHaveSize(2)
//
//        infoProvider.userId = "user2"
//        AccountRepository.instance().updateUserInfo()
//        ReadyForDisplayMessageRepository.instance().messages.shouldBeEmpty()
//
//        // revert to initial user info
//        infoProvider.userId = TestUserInfoProvider.TEST_USER_ID
//        AccountRepository.instance().updateUserInfo()
//    }

    private fun initializeInstance(infoProvider: UserInfoProvider) {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id"
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        InAppMessaging.instance().registerPreference(infoProvider)
    }
}
