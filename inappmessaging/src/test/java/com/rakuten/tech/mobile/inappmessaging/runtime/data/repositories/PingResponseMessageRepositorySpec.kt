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
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.amshove.kluent.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

/**
 * Test class for PingResponseMessageRepository.
 */
@RunWith(RobolectricTestRunner::class)
@SuppressWarnings("LargeClass")
class PingResponseMessageRepositorySpec : BaseTest() {
    private val message0 = ValidTestMessage()
    private val message1 = ValidTestMessage("1234")
    private val messageList = ArrayList<Message>()

    @Before
    fun setup() {
        PingResponseMessageRepository.instance().clearMessages()
        messageList.clear()
        messageList.add(message0)
        messageList.add(message1)
    }

    @Test
    fun `should add message list with valid list`() {
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
        PingResponseMessageRepository.instance().getAllMessagesCopy()[0] shouldBeEqualTo message0
        PingResponseMessageRepository.instance().getAllMessagesCopy()[1] shouldBeEqualTo message1
    }

    @Test
    fun `should throw exception when list is null`() {
        try {
            PingResponseMessageRepository.instance().replaceAllMessages(null)
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            e.localizedMessage shouldBeEqualTo InAppMessagingConstants.ARGUMENT_IS_NULL_EXCEPTION
        }
    }

    @Test
    fun `should not throw exception when list is empty`() {
        PingResponseMessageRepository.instance().replaceAllMessages(ArrayList())
    }

    @Test
    fun `should be empty after clearing`() {
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        PingResponseMessageRepository.instance().clearMessages()
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)
    }

    @Test
    fun `should contain correct messages after clearing then adding`() {
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        PingResponseMessageRepository.instance().clearMessages()
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)

        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
    }

    @Test
    fun `should increment all matching from single item`() {
        initializeInstance(TestUserInfoProvider())
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().incrementTimesClosed(listOf(message0))
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            if (msg.getCampaignId() != message1.getCampaignId()) {
                msg.getNumberOfTimesClosed() shouldBeEqualTo 1
            } else {
                msg.getNumberOfTimesClosed() shouldBeEqualTo 0
            }
        }
    }

    @Test
    fun `should increment all matching from multiple items`() {
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().incrementTimesClosed(messageList)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 1
        }
    }

    @Test
    fun `should not increment if no matching`() {
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().incrementTimesClosed(listOf(ValidTestMessage("4321")))
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
        }
    }

    @Test
    fun `should save and restore values for different users`() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)

        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        infoProvider.rakutenId = "user2"
        AccountRepository.instance().updateUserInfo()
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldBeEmpty()

        // revert to initial user info
        infoProvider.rakutenId = TestUserInfoProvider.TEST_RAKUTEN_ID
        AccountRepository.instance().updateUserInfo()
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
    }

    @Test
    fun `should return false when not first launch`() {
        initializeInstance(TestUserInfoProvider(), false)
        PingResponseMessageRepository.isInitialLaunch = false
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            PingResponseMessageRepository.instance()
                    .shouldDisplayAppLaunchCampaign(msg.getCampaignId()!!).shouldBeFalse()
        }
    }

    @Test
    fun `should return true when first launch`() {
        initializeInstance(TestUserInfoProvider(), false)
        PingResponseMessageRepository.isInitialLaunch = true
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            PingResponseMessageRepository.instance()
                    .shouldDisplayAppLaunchCampaign(msg.getCampaignId()!!).shouldBeTrue()
        }
    }

    @Test
    fun `should return true to app launch only and false to multiple triggers`() {
        initializeInstance(TestUserInfoProvider(), false)
        PingResponseMessageRepository.isInitialLaunch = true
        val mockMessage = Mockito.mock(Message::class.java)
        When calling mockMessage.getCampaignId() itReturns "54321"
        When calling mockMessage.getTriggers() itReturns listOf(Trigger(), Trigger())
        messageList.add(mockMessage)
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            val id = msg.getCampaignId()!!
            if (id == "54321") {
                PingResponseMessageRepository.instance().shouldDisplayAppLaunchCampaign(id).shouldBeFalse()
            } else {
                PingResponseMessageRepository.instance().shouldDisplayAppLaunchCampaign(id).shouldBeTrue()
            }
        }
    }

    @Test
    fun `should not update max impression`() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)

        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getMaxImpressions() shouldBeEqualTo 1
        }

        message0.setMaxImpression(3)
        PingResponseMessageRepository.instance().replaceAllMessages(listOf(message0))
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(1)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getMaxImpressions() shouldBeEqualTo 3
        }
    }

    private fun initializeInstance(infoProvider: UserInfoProvider, isCache: Boolean = true) {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID, "test_device_id")
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test", "",
                isDebugLogging = true, isForTesting = true, isCacheHandling = isCache)
        InAppMessaging.instance().registerPreference(infoProvider)
    }
}
