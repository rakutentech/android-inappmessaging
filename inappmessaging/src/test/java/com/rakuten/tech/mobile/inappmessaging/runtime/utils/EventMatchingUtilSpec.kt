package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.LoginSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class EventMatchingUtilSpec {

    private val mockCampaignRepo = Mockito.mock(CampaignRepository::class.java)
    private val appStartEv = AppStartEvent()
    private val loginEv = LoginSuccessfulEvent()
    private val purchaseEv = PurchaseSuccessfulEvent()
    private val campaign = TestDataHelper.createDummyMessage()
    private val eventMatchingUtil = EventMatchingUtil.EventMatchingUtilImpl(mockCampaignRepo)

    @Before
    fun setup() {
        `when`(mockCampaignRepo.messages).thenReturn(linkedMapOf(campaign.campaignId to campaign))
    }

    /** removeSetOfMatchedEvents **/

    @Test
    fun `should return false if events for a given campaign weren't found`() {
        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv), campaign)
            .shouldBeFalse()
    }

    @Test
    fun `should return false if all events weren't found`() {
        eventMatchingUtil.matchAndStore(loginEv)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv, loginEv), campaign)
            .shouldBeFalse()
    }

    @Test
    fun `should return false if one of requested events doesn't match given campaign`() {
        eventMatchingUtil.matchAndStore(loginEv)
        eventMatchingUtil.matchAndStore(appStartEv)
        eventMatchingUtil.matchAndStore(purchaseEv)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv, loginEv, purchaseEv), campaign)
            .shouldBeFalse()
    }

    @Test
    fun `should return false if requested set of event isn't found`() {
        eventMatchingUtil.matchAndStore(loginEv)
        eventMatchingUtil.matchAndStore(appStartEv)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(purchaseEv), campaign)
            .shouldBeFalse()
    }

    @Test
    fun `should return false if tooltip is already displayed`() {
        val campaign = TestDataHelper.createDummyMessage(type = InAppMessageType.TOOLTIP.typeId)
        `when`(mockCampaignRepo.messages).thenReturn(linkedMapOf(campaign.campaignId to campaign))
        eventMatchingUtil.matchAndStore(loginEv)
        eventMatchingUtil.matchAndStore(appStartEv)
        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv, loginEv), campaign)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(loginEv), campaign)
            .shouldBeFalse()
    }

    @Test
    fun `should not persist normal events`() {
        eventMatchingUtil.matchAndStore(loginEv)
        eventMatchingUtil.matchAndStore(appStartEv)
        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv, loginEv), campaign)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv, loginEv), campaign)
            .shouldBeFalse()
    }

    @Test
    fun `should return true if all events are found`() {
        eventMatchingUtil.matchAndStore(appStartEv)
        eventMatchingUtil.matchAndStore(loginEv)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv, loginEv), campaign)
            .shouldBeTrue()
    }

    @Test
    fun `should only remove one copy of non-persistent event`() {
        eventMatchingUtil.matchAndStore(loginEv)
        eventMatchingUtil.matchAndStore(loginEv)
        eventMatchingUtil.matchAndStore(appStartEv)
        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv, loginEv), campaign)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv, loginEv), campaign)
            .shouldBeTrue()
    }

    @Test
    fun `should return true without the need for persistent event to be logged`() {
        eventMatchingUtil.matchAndStore(loginEv)
        eventMatchingUtil.matchAndStore(appStartEv)
        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv, loginEv), campaign)
        eventMatchingUtil.matchAndStore(loginEv)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv, loginEv), campaign)
            .shouldBeTrue()
    }

    @Test
    fun `should return true if only persistent events are required`() {
        val campaign = TestDataHelper.createDummyMessage(
            triggers = listOf(Trigger(type = 1, eventType = 1,
                eventName = "Launch the App Event", triggerAttributes = mutableListOf()))
        )
        `when`(mockCampaignRepo.messages).thenReturn(linkedMapOf(campaign.campaignId to campaign))
        eventMatchingUtil.matchAndStore(appStartEv)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv), campaign)
            .shouldBeTrue()
    }

    @Test
    fun `should return true only once if only persistent events are required`() {
        val campaign = TestDataHelper.createDummyMessage(
            triggers = listOf(Trigger(type = 1, eventType = 1,
                eventName = "Launch the App Event", triggerAttributes = mutableListOf()))
        )
        `when`(mockCampaignRepo.messages).thenReturn(linkedMapOf(campaign.campaignId to campaign))
        eventMatchingUtil.matchAndStore(appStartEv)
        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv), campaign)
            .shouldBeTrue()

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(appStartEv), campaign)
            .shouldBeFalse()
    }

    /** matchedEvents **/

    @Test
    fun `should properly match persistent events`() {
        eventMatchingUtil.matchAndStore(appStartEv)

        eventMatchingUtil.matchedEvents(campaign).shouldContain(appStartEv)
    }

    @Test
    fun `should properly match non-persistent events`() {
        eventMatchingUtil.matchAndStore(loginEv)

        eventMatchingUtil.matchedEvents(campaign).shouldContain(loginEv)
    }

    /** containsAllMatchedEvents **/

    @Test
    fun `should return true if all required events were stored`() {
        eventMatchingUtil.matchAndStore(appStartEv)
        eventMatchingUtil.matchAndStore(loginEv)

        eventMatchingUtil.containsAllMatchedEvents(campaign).shouldBeTrue()
    }

    @Test
    fun `should return true if more events than required were stored`() {
        eventMatchingUtil.matchAndStore(appStartEv)
        eventMatchingUtil.matchAndStore(loginEv)
        eventMatchingUtil.matchAndStore(loginEv)
        eventMatchingUtil.matchAndStore(purchaseEv)

        eventMatchingUtil.containsAllMatchedEvents(campaign).shouldBeTrue()
    }

    @Test
    fun `should return false if not all required events were stored`() {
        eventMatchingUtil.matchAndStore(appStartEv)

        eventMatchingUtil.containsAllMatchedEvents(campaign).shouldBeFalse()
    }

    @Test
    fun `should return false if none of required events were stored`() {
        eventMatchingUtil.containsAllMatchedEvents(campaign).shouldBeFalse()
    }

    @Test
    fun `should return false if triggers are null or empty`() {
        val campaign = TestDataHelper.createDummyMessage(
            triggers = listOf()
        )
        eventMatchingUtil.containsAllMatchedEvents(campaign).shouldBeFalse()
    }

    /** clearNonPersistentEvents **/

    @Test
    fun `should clear all matched non-persistent events`() {
        eventMatchingUtil.matchAndStore(loginEv)
        eventMatchingUtil.matchedEvents(campaign).shouldNotBeEmpty()

        eventMatchingUtil.clearNonPersistentEvents()
        eventMatchingUtil.matchedEvents(campaign).shouldBeEmpty()
    }

    @Test
    fun `should not clear all persistent events`() {
        eventMatchingUtil.matchAndStore(appStartEv)
        eventMatchingUtil.matchedEvents(campaign).shouldNotBeEmpty()

        eventMatchingUtil.clearNonPersistentEvents()
        eventMatchingUtil.matchedEvents(campaign).shouldNotBeEmpty()
    }

    @Test
    fun `should not clear triggered persistent campaigns list`() {
        eventMatchingUtil.triggeredPersistentCampaigns.clear()
        eventMatchingUtil.triggeredPersistentCampaigns.add("app-start-campaign")

        eventMatchingUtil.clearNonPersistentEvents()

        eventMatchingUtil.triggeredPersistentCampaigns.shouldHaveSize(1)
    }

    /** addToEventBuffer **/

    @Test
    fun `should add to event buffer`() {
        eventMatchingUtil.addToEventBuffer(appStartEv)
        eventMatchingUtil.addToEventBuffer(purchaseEv)

        eventMatchingUtil.eventBuffer.size.shouldBeEqualTo(2)
    }

    @Test
    fun `should flush event buffer`() {
        eventMatchingUtil.addToEventBuffer(appStartEv)
        eventMatchingUtil.addToEventBuffer(purchaseEv)

        eventMatchingUtil.flushEventBuffer()

        eventMatchingUtil.eventBuffer.shouldBeEmpty()
    }
}