package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CampaignRepositorySpec {
    @Before
    fun setup() {
        CampaignRepository.instance().clearMessages()
    }

    /** syncWith **/

    @Test
    fun `should load cached data`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        InAppMessaging.initialize(ctx, true)
        val message = TestDataHelper.createDummyMessage() // maxImpressions=100
        PreferencesUtil.putString(
            ctx, InAppMessaging.getPreferencesFile(), CampaignRepository.IAM_USER_CACHE,
            Gson().toJson(message),
        )
        CampaignRepository.instance().syncWith(listOf(TestDataHelper.createDummyMessage(maxImpressions = 999)), 0)

        CampaignRepository.instance().messages.values.first().maxImpressions.shouldBeEqualTo(999)
    }

    @Test
    fun `should set message list when calling syncWith()`() {
        val campaign = TestDataHelper.createDummyMessage(campaignId = "0")
        val campaign1 = TestDataHelper.createDummyMessage(campaignId = "1")
        CampaignRepository.instance().syncWith(listOf(campaign, campaign1), 0)

        CampaignRepository.instance().messages.shouldHaveSize(2)
        CampaignRepository.instance().messages.values.first() shouldBeEqualTo campaign
        CampaignRepository.instance().messages.values.last() shouldBeEqualTo campaign1
    }

    @Test
    fun `should ignore invalid message when calling syncWith()`() {
        val campaign = TestDataHelper.createDummyMessage(campaignId = "")
        val campaign1 = TestDataHelper.createDummyMessage(campaignId = "1")
        CampaignRepository.instance().syncWith(listOf(campaign, campaign1), 0)

        CampaignRepository.instance().messages.shouldHaveSize(1)
    }

    @Test
    fun `should include tooltip campaigns`() {
        val campaign = TestDataHelper.createDummyMessage(campaignId = "0")
        val campaign1 = TestDataHelper.createDummyMessage(campaignId = "1")
        CampaignRepository.instance().syncWith(listOf(campaign, campaign1), 0, false)

        CampaignRepository.instance().messages.shouldHaveSize(2)
    }

    @Test
    fun `should ignore tooltip campaigns`() {
        val campaign = TestDataHelper.createDummyMessage(campaignId = "0")
        val campaign1 = TestDataHelper.createDummyMessage(campaignId = "1", type = InAppMessageType.TOOLTIP.typeId)
        CampaignRepository.instance().syncWith(listOf(campaign, campaign1), 0, true)

        CampaignRepository.instance().messages.shouldHaveSize(1)
    }

    /** optOutCampaign **/

    @Test
    fun `should mark campaign as opted out`() {
        val campaign = TestDataHelper.createDummyMessage()
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        CampaignRepository.instance().messages.values.first().isOptedOut?.shouldBeFalse()

        CampaignRepository.instance().optOutCampaign(campaign.campaignId)
        CampaignRepository.instance().messages.values.first().isOptedOut?.shouldBeTrue()
    }

    @Test
    fun `should not cache updated campaign's opt out status if it is marked as isTest`() {
        val campaign = TestDataHelper.createDummyMessage(isTest = true)
        CampaignRepository.instance().syncWith(listOf(campaign), 0)

        CampaignRepository.instance().optOutCampaign(campaign.campaignId)
    }

    @Test
    fun `should return null when opting out unknown campaign`() {
        val campaign = TestDataHelper.createDummyMessage(campaignId = "0")
        val campaign1 = TestDataHelper.createDummyMessage(campaignId = "1")
        CampaignRepository.instance().syncWith(listOf(campaign), 0)

        CampaignRepository.instance().optOutCampaign(campaign1.campaignId).shouldBeNull()
    }

    @Test
    fun `should persist isOptedOut value`() {
        val campaign = TestDataHelper.createDummyMessage()
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        CampaignRepository.instance().messages.values.first().isOptedOut?.shouldBeFalse()

        CampaignRepository.instance().optOutCampaign(campaign.campaignId)
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        CampaignRepository.instance().messages.values.first().isOptedOut?.shouldBeTrue()
    }

    /** decrementImpressions **/

    @Test
    fun `should decrement impressionsLeft value`() {
        val campaign = TestDataHelper.createDummyMessage()
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        val impressions = campaign.impressionsLeft ?: 0

        CampaignRepository.instance().decrementImpressions(campaign.campaignId)
        CampaignRepository.instance().messages.values.first().impressionsLeft shouldBeEqualTo impressions - 1
    }

    @Test
    fun `should not decrement if impressionsLeft value is already 0`() {
        val campaign = TestDataHelper.createDummyMessage(maxImpressions = 0)
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        CampaignRepository.instance().decrementImpressions(campaign.campaignId)

        CampaignRepository.instance().messages.values.first().impressionsLeft shouldBeEqualTo 0
    }

    @Test
    fun `should persist impressionsLeft value`() {
        val campaign = TestDataHelper.createDummyMessage(maxImpressions = 3)
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        CampaignRepository.instance().decrementImpressions(campaign.campaignId)
        CampaignRepository.instance().messages.values.first().impressionsLeft shouldBeEqualTo 2

        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        CampaignRepository.instance().messages.values.first().impressionsLeft shouldBeEqualTo 2
    }

    @Test
    fun `should modify impressionsLeft if maxImpressions was updated (campaign modified)`() {
        val campaign = TestDataHelper.createDummyMessage(maxImpressions = 3)
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        CampaignRepository.instance().decrementImpressions(campaign.campaignId)
        CampaignRepository.instance().decrementImpressions(campaign.campaignId)
        CampaignRepository.instance().messages.values.first().impressionsLeft shouldBeEqualTo 1

        val updatedCampaign = TestDataHelper.createDummyMessage(maxImpressions = 6)
        CampaignRepository.instance().syncWith(listOf(updatedCampaign), 0)
        CampaignRepository.instance().messages.values.first().impressionsLeft shouldBeEqualTo 4
    }

    /** incrementImpressions **/

    @Test
    fun `should not override impressionsLeft value even if maxImpressions is smaller`() {
        val campaign = TestDataHelper.createDummyMessage(maxImpressions = 3)
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        CampaignRepository.instance().incrementImpressions(campaign.campaignId)
        CampaignRepository.instance().messages.values.first().impressionsLeft shouldBeEqualTo 4

        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        CampaignRepository.instance().messages.values.first().impressionsLeft shouldBeEqualTo 4
    }

    /** clearMessages **/

    @Test
    fun `should be empty after clearing`() {
        val campaign = TestDataHelper.createDummyMessage()
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        CampaignRepository.instance().messages.shouldHaveSize(1)

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
        val campaign = TestDataHelper.createDummyMessage()
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        CampaignRepository.instance().messages.shouldHaveSize(1)

        CampaignRepository.instance().clearMessages()
        CampaignRepository.instance().messages.shouldHaveSize(0)

        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        CampaignRepository.instance().messages.shouldHaveSize(1)
    }
}
