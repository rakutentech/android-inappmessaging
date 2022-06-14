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
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.collections.ArrayList

/**
 * Test class for CampaignRepository.
 */
@RunWith(RobolectricTestRunner::class)
@SuppressWarnings("LargeClass")
class CampaignRepositorySpec : BaseTest() {
    private val message0 = ValidTestMessage(
        maxImpressions = 3
    )
    private val message1 = ValidTestMessage("1234")
    private val messageList = ArrayList<Message>()

    @Before
    override fun setup() {
        super.setup()
        CampaignRepository.instance().clearMessages()
        messageList.clear()
        messageList.add(message0)
        messageList.add(message1)
    }

    // region syncWith

    @Test
    fun `should add message list with valid list`() {
        CampaignRepository.instance().syncWith(messageList, 0)
        CampaignRepository.instance().messages.shouldHaveSize(2)
        CampaignRepository.instance().messages[0] shouldBeEqualTo message0
        CampaignRepository.instance().messages[1] shouldBeEqualTo message1
    }

    @Test
    fun `should not throw exception when list is empty`() {
        CampaignRepository.instance().syncWith(ArrayList(), 0)
    }

    @Test
    fun `should ignore invalid message in the list`() {
        CampaignRepository.instance().syncWith(
            listOf(InvalidTestMessage(), message0, ValidTestMessage("")),
            0
        )
        CampaignRepository.instance().messages.shouldHaveSize(1)
    }

    @Test
    fun `should be empty after clearing`() {
        CampaignRepository.instance().syncWith(messageList, 0)
        CampaignRepository.instance().messages.shouldHaveSize(2)

        CampaignRepository.instance().clearMessages()
        CampaignRepository.instance().messages.shouldHaveSize(0)
    }

    @Test
    fun `should not crash while clearing messages`() {
        InAppMessaging.setNotConfiguredInstance(true)
        CampaignRepository.instance().clearMessages()
        CampaignRepository.instance().messages.shouldHaveSize(0)
    }

    @Test
    fun `should contain correct messages after clearing then adding`() {
        CampaignRepository.instance().syncWith(messageList, 0)
        CampaignRepository.instance().messages.shouldHaveSize(2)

        CampaignRepository.instance().clearMessages()
        CampaignRepository.instance().messages.shouldHaveSize(0)

        CampaignRepository.instance().syncWith(messageList, 0)
        CampaignRepository.instance().messages.shouldHaveSize(2)
    }

    @Test
    fun `should persist impressionsLeft value`() {
        CampaignRepository.instance().syncWith(listOf(message0), 0)
        CampaignRepository.instance().decrementImpressions(message0.getCampaignId())
        CampaignRepository.instance().messages.first().impressionsLeft shouldBeEqualTo 2

        CampaignRepository.instance().syncWith(listOf(message0), 0)
        CampaignRepository.instance().messages.first().impressionsLeft shouldBeEqualTo 2
    }

    @Test
    fun `should persist isOptedOut value`() {
        CampaignRepository.instance().syncWith(listOf(message0), 0)
        CampaignRepository.instance().messages.first().isOptedOut?.shouldBeFalse()

        CampaignRepository.instance().optOutCampaign(message0)
        CampaignRepository.instance().syncWith(listOf(message0), 0)
        CampaignRepository.instance().messages.first().isOptedOut?.shouldBeTrue()
    }

    @Test
    fun `should not override impressionsLeft value even if maxImpressions is smaller`() {
        CampaignRepository.instance().syncWith(listOf(message0), 0)
        CampaignRepository.instance().incrementImpressions(message0.getCampaignId())
        CampaignRepository.instance().messages.first().impressionsLeft shouldBeEqualTo 4

        CampaignRepository.instance().syncWith(listOf(message0), 0)
        CampaignRepository.instance().messages.first().impressionsLeft shouldBeEqualTo 4
    }

    @Test
    fun `should modify impressionsLeft if maxImpressions was updated (campaign modified)`() {
        CampaignRepository.instance().syncWith(listOf(message0), 0)
        CampaignRepository.instance().decrementImpressions(message0.getCampaignId())
        CampaignRepository.instance().decrementImpressions(message0.getCampaignId())
        CampaignRepository.instance().messages.first().impressionsLeft shouldBeEqualTo 1

        val updatedCampaign = ValidTestMessage(
            campaignId = message0.getCampaignId(),
            maxImpressions = 6
        )
        CampaignRepository.instance().syncWith(listOf(updatedCampaign), 0)
        CampaignRepository.instance().messages.first().impressionsLeft shouldBeEqualTo 4
    }

    // endregion

    // region optOutCampaign

    @Test
    fun `should mark campaign as opted out`() {
        CampaignRepository.instance().syncWith(listOf(message0), 0)
        CampaignRepository.instance().messages.first().isOptedOut?.shouldBeFalse()

        val updatedCampaign = CampaignRepository.instance().optOutCampaign(message0)
        updatedCampaign?.isOptedOut?.shouldBeTrue()
        CampaignRepository.instance().messages.first().isOptedOut?.shouldBeTrue()
    }

    @Test
    fun `should not cache updated campaign's opt out status if it is marked as isTest`() {
        val testCampaign = ValidTestMessage(isTest = true)
        CampaignRepository.instance().syncWith(listOf(testCampaign), 0)

        CampaignRepository.instance().optOutCampaign(testCampaign)
    }

    @Test
    fun `should return null when opting out unknown campaign`() {
        val testCampaign = ValidTestMessage("0")
        val testCampaign1 = ValidTestMessage("1")
        CampaignRepository.instance().syncWith(listOf(testCampaign), 0)

        CampaignRepository.instance().optOutCampaign(testCampaign1).shouldBeNull()
    }

    // endregion

    // region decrementImpressionsLeft

    @Test
    fun `should decrement impressionsLeft value`() {
        CampaignRepository.instance().syncWith(listOf(message0), 0)
        val impressions = message0.impressionsLeft ?: 0

        val updatedCampaign = CampaignRepository.instance().decrementImpressions(message0.getCampaignId())
        updatedCampaign?.impressionsLeft shouldBeEqualTo impressions - 1
        CampaignRepository.instance().messages.first().impressionsLeft shouldBeEqualTo impressions - 1
    }

    @Test
    fun `should not decrement if impressionsLeft value is already 0`() {
        val testCampaign = ValidTestMessage(maxImpressions = 0)
        CampaignRepository.instance().syncWith(listOf(testCampaign), 0)
        CampaignRepository.instance().decrementImpressions(testCampaign.getCampaignId())

        CampaignRepository.instance().messages.first().impressionsLeft shouldBeEqualTo 0
    }

    // endregion

    @Test
    fun `should save and restore values for different users`() {
        setupAndTestMultipleUser()
        CampaignRepository.instance().messages.shouldHaveSize(2)
    }

    @Test
    fun `should not update max impression`() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)

        CampaignRepository.instance().syncWith(listOf(message0), 0)
        CampaignRepository.instance().messages.shouldHaveSize(1)
        CampaignRepository.instance().messages.first().getMaxImpressions() shouldBeEqualTo 3

        message0.setMaxImpression(6)
        CampaignRepository.instance().syncWith(listOf(message0), 0)
        CampaignRepository.instance().messages.shouldHaveSize(1)
        CampaignRepository.instance().messages.first().getMaxImpressions() shouldBeEqualTo 6
    }

    @Test
    fun `should not crash and clear previous when forced cast exception`() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)
        PreferencesUtil.putInt(
            ApplicationProvider.getApplicationContext(),
            InAppMessaging.getPreferencesFile(),
            CampaignRepository.IAM_USER_CACHE,
            1
        )
        CampaignRepository.instance().messages.shouldBeEmpty()
    }

    @Test
    fun `should not crash and reset map`() {
        setupAndTestMultipleUser()
        InAppMessaging.setNotConfiguredInstance(true)
        CampaignRepository.instance().messages.shouldBeEmpty()
    }

    private fun setupAndTestMultipleUser() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)

        CampaignRepository.instance().syncWith(messageList, 0)
        CampaignRepository.instance().messages.shouldHaveSize(2)

        infoProvider.userId = "user2"
        AccountRepository.instance().updateUserInfo()
        CampaignRepository.instance().messages.shouldBeEmpty()

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
