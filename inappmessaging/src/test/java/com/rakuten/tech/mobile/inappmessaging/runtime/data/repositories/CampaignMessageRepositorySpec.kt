package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.InvalidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

/**
 * Test class for PingResponseMessageRepository.
 */
@RunWith(RobolectricTestRunner::class)
@SuppressWarnings("LargeClass")
class CampaignMessageRepositorySpec : BaseTest() {
    private val message0 = ValidTestMessage()
    private val message1 = ValidTestMessage("1234")
    private val messageList = ArrayList<Message>()

    @Before
    override fun setup() {
        super.setup()
        CampaignMessageRepository.instance().clearMessages()
        messageList.clear()
        messageList.add(message0)
        messageList.add(message1)
    }

    @Test
    fun `should add message list with valid list`() {
        CampaignMessageRepository.instance().syncWith(messageList)
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
        CampaignMessageRepository.instance().getAllMessagesCopy()[0] shouldBeEqualTo message0
        CampaignMessageRepository.instance().getAllMessagesCopy()[1] shouldBeEqualTo message1
    }

    @Test
    fun `should not throw exception when list is empty`() {
        CampaignMessageRepository.instance().syncWith(ArrayList())
    }

    @Test
    fun `should ignore invalid message in the list`() {
        CampaignMessageRepository.instance().syncWith(
            listOf(InvalidTestMessage(), message0, ValidTestMessage(""))
        )
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(1)
    }

    @Test
    fun `should be empty after clearing`() {
        CampaignMessageRepository.instance().syncWith(messageList)
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        CampaignMessageRepository.instance().clearMessages()
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)
    }

    @Test
    fun `should not crash while clearing messages`() {
        InAppMessaging.setNotConfiguredInstance(true)
        CampaignMessageRepository.instance().clearMessages()
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)
    }

    @Test
    fun `should contain correct messages after clearing then adding`() {
        CampaignMessageRepository.instance().syncWith(messageList)
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        CampaignMessageRepository.instance().clearMessages()
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)

        CampaignMessageRepository.instance().syncWith(messageList)
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
    }

    @Test
    fun `should increment all matching from single item`() {
        initializeInstance(TestUserInfoProvider())
        CampaignMessageRepository.instance().syncWith(messageList)
        CampaignMessageRepository.instance().incrementTimesClosed(listOf(message0))
        for (msg in CampaignMessageRepository.instance().getAllMessagesCopy()) {
            if (msg.getCampaignId() != message1.getCampaignId()) {
                msg.getNumberOfTimesClosed() shouldBeEqualTo 1
            } else {
                msg.getNumberOfTimesClosed() shouldBeEqualTo 0
            }
        }
    }

    @Test
    fun `should increment all matching from multiple items`() {
        CampaignMessageRepository.instance().syncWith(messageList)
        CampaignMessageRepository.instance().incrementTimesClosed(messageList)
        for (msg in CampaignMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 1
        }
    }

    @Test
    fun `should not increment if no matching`() {
        CampaignMessageRepository.instance().syncWith(messageList)
        CampaignMessageRepository.instance().incrementTimesClosed(listOf(ValidTestMessage("4321")))
        for (msg in CampaignMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
        }
    }

    @Test
    fun `should save and restore values for different users`() {
        setupAndTestMultipleUser()
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
    }

    @Test
    fun `should return false when not first launch`() {
        initializeInstance(TestUserInfoProvider(), false)
        CampaignMessageRepository.isInitialLaunch = false
        CampaignMessageRepository.instance().syncWith(messageList)
        for (msg in CampaignMessageRepository.instance().getAllMessagesCopy()) {
            CampaignMessageRepository.instance()
                .shouldDisplayAppLaunchCampaign(msg.getCampaignId()).shouldBeFalse()
        }
    }

    @Test
    fun `should return true when first launch`() {
        initializeInstance(TestUserInfoProvider(), false)
        CampaignMessageRepository.isInitialLaunch = true
        CampaignMessageRepository.instance().syncWith(messageList)
        for (msg in CampaignMessageRepository.instance().getAllMessagesCopy()) {
            CampaignMessageRepository.instance()
                .shouldDisplayAppLaunchCampaign(msg.getCampaignId()).shouldBeTrue()
        }
    }

    @Test
    @SuppressWarnings("LongMethod")
    fun `should return true to app launch only and false to multiple triggers`() {
        initializeInstance(TestUserInfoProvider(), false)
        CampaignMessageRepository.isInitialLaunch = true
        val mockMessage = Mockito.mock(Message::class.java)
        `when`(mockMessage.getCampaignId()).thenReturn("54321")
        `when`(mockMessage.getTriggers()).thenReturn(
            listOf(
                Trigger(
                    1, 1, "test",
                    mutableListOf()
                ),
                Trigger(1, 2, "test2", mutableListOf())
            )
        )
        messageList.add(mockMessage)
        CampaignMessageRepository.instance().syncWith(messageList)
        for (msg in CampaignMessageRepository.instance().getAllMessagesCopy()) {
            val id = msg.getCampaignId()
            if (id == "54321") {
                CampaignMessageRepository.instance().shouldDisplayAppLaunchCampaign(id).shouldBeFalse()
            } else {
                CampaignMessageRepository.instance().shouldDisplayAppLaunchCampaign(id).shouldBeTrue()
            }
        }
    }

    @Test
    fun `should not update max impression`() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)

        CampaignMessageRepository.instance().syncWith(messageList)
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
        for (msg in CampaignMessageRepository.instance().getAllMessagesCopy()) {
            msg.getMaxImpressions() shouldBeEqualTo 1
        }

        message0.setMaxImpression(3)
        CampaignMessageRepository.instance().syncWith(listOf(message0))
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(1)
        for (msg in CampaignMessageRepository.instance().getAllMessagesCopy()) {
            msg.getMaxImpressions() shouldBeEqualTo 3
        }
    }

    @Test
    fun `should not crash and clear previous when forced cast exception`() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)
        PreferencesUtil.putInt(
            ApplicationProvider.getApplicationContext(),
            InAppMessaging.getPreferencesFile(),
            CampaignMessageRepository.PING_RESPONSE_KEY,
            1
        )
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldBeEmpty()
    }

    @Test
    fun `should not crash and reset map`() {
        setupAndTestMultipleUser()
        InAppMessaging.setNotConfiguredInstance(true)
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldBeEmpty()
    }

    private fun setupAndTestMultipleUser() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)

        CampaignMessageRepository.instance().syncWith(messageList)
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        infoProvider.userId = "user2"
        AccountRepository.instance().updateUserInfo()
        CampaignMessageRepository.instance().getAllMessagesCopy().shouldBeEmpty()

        // revert to initial user info
        infoProvider.userId = TestUserInfoProvider.TEST_USER_ID
        AccountRepository.instance().updateUserInfo()
    }

    private fun initializeInstance(infoProvider: UserInfoProvider, isCache: Boolean = true) {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID, "test_device_id"
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), isCache)
        InAppMessaging.instance().registerPreference(infoProvider)
    }
}
