package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.LoginSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import org.amshove.kluent.*
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

open class EventMatchingUtilSpec : BaseTest() {

    // Mocks
    internal val mockCampaignRepo = Mockito.mock(CampaignRepository::class.java)
    internal val mockCampaign = Mockito.mock(Message::class.java)
    internal val mockPersistentOnlyCampaign = Mockito.mock(Message::class.java)
    internal val mockAppStartEv = AppStartEvent()
    internal val mockLoginEv = LoginSuccessfulEvent()
    internal val mockPurchaseEv = PurchaseSuccessfulEvent()
    internal val mockCampaignMap = linkedMapOf("1" to mockCampaign)

    // In Test
    internal val eventMatchingUtil = EventMatchingUtil.EventMatchingUtilImpl(mockCampaignRepo)

    init {
        setupTestCampaigns()
    }

    private fun setupTestCampaigns() {
        `when`(mockCampaign.getCampaignId()).thenReturn("test")
        `when`(mockCampaign.getTriggers()).thenReturn(
            listOf(
                Trigger(0, EventType.APP_START.typeId, "appStartTest", mutableListOf()),
                Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "loginSuccessfulTest", mutableListOf())
            )
        )

        `when`(mockPersistentOnlyCampaign.getCampaignId()).thenReturn("test")
        `when`(mockPersistentOnlyCampaign.getMaxImpressions()).thenReturn(2)
        `when`(mockPersistentOnlyCampaign.getTriggers()).thenReturn(
            listOf(
                Trigger(
                    0, EventType.APP_START.typeId, "appStartTest", mutableListOf()
                )
            )
        )
    }
}

class EventMatchingUtilSetSpec : EventMatchingUtilSpec() {
    @Test
    fun `should return false if events for a given campaign weren't found`() {
        eventMatchingUtil.removeSetOfMatchedEvents(setOf(mockAppStartEv), mockCampaign).shouldBeFalse()
    }

    @Test
    fun `should return false if all events weren't found`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap)
        eventMatchingUtil.matchAndStore(mockLoginEv)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(mockAppStartEv, mockLoginEv), mockCampaign)
            .shouldBeFalse()
    }

    @Test
    fun `should return false if one of requested events doesn't match given campaign`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap) // login & appstart only
        eventMatchingUtil.matchAndStore(mockLoginEv)
        eventMatchingUtil.matchAndStore(mockAppStartEv)
        eventMatchingUtil.matchAndStore(mockPurchaseEv)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(mockAppStartEv, mockLoginEv, mockPurchaseEv), mockCampaign)
            .shouldBeFalse()
    }

    @Test
    fun `should return false if requested set of event isn't found`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap)
        eventMatchingUtil.matchAndStore(mockLoginEv)
        eventMatchingUtil.matchAndStore(mockAppStartEv)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(mockPurchaseEv), mockCampaign)
            .shouldBeFalse()
    }

    @Test
    fun `should not persist normal events`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap)
        eventMatchingUtil.matchAndStore(mockLoginEv)
        eventMatchingUtil.matchAndStore(mockAppStartEv)
        eventMatchingUtil.removeSetOfMatchedEvents(setOf(mockAppStartEv, mockLoginEv), mockCampaign)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(mockAppStartEv, mockLoginEv), mockCampaign)
            .shouldBeFalse()
    }

    @Test
    fun `should return true if all events are found`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap)
        eventMatchingUtil.matchAndStore(mockLoginEv)
        eventMatchingUtil.matchAndStore(mockAppStartEv)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(mockAppStartEv, mockLoginEv), mockCampaign)
            .shouldBeTrue()
    }

    @Test
    fun `should only remove one copy of non-persistent event`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap)
        eventMatchingUtil.matchAndStore(mockLoginEv)
        eventMatchingUtil.matchAndStore(mockLoginEv)
        eventMatchingUtil.matchAndStore(mockAppStartEv)
        eventMatchingUtil.removeSetOfMatchedEvents(setOf(mockAppStartEv, mockLoginEv), mockCampaign)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(mockAppStartEv, mockLoginEv), mockCampaign)
            .shouldBeTrue()
    }

    @Test
    fun `should return true without the need for persistent event to be logged`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap)
        eventMatchingUtil.matchAndStore(mockLoginEv)
        eventMatchingUtil.matchAndStore(mockAppStartEv)
        eventMatchingUtil.removeSetOfMatchedEvents(setOf(mockAppStartEv, mockLoginEv), mockCampaign)
        eventMatchingUtil.matchAndStore(mockLoginEv)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(mockAppStartEv, mockLoginEv), mockCampaign)
            .shouldBeTrue()
    }

    @Test
    fun `should return true if only persistent events are required`() {
        `when`(mockCampaignRepo.messages).thenReturn(linkedMapOf("1" to mockPersistentOnlyCampaign))
        eventMatchingUtil.matchAndStore(mockAppStartEv)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(mockAppStartEv), mockPersistentOnlyCampaign)
            .shouldBeTrue()
    }

    @Test
    fun `should return true only once if only persistent events are required`() {
        `when`(mockCampaignRepo.messages).thenReturn(linkedMapOf("1" to mockPersistentOnlyCampaign))
        eventMatchingUtil.matchAndStore(mockAppStartEv)
        eventMatchingUtil.removeSetOfMatchedEvents(setOf(mockAppStartEv), mockPersistentOnlyCampaign)

        eventMatchingUtil.removeSetOfMatchedEvents(setOf(AppStartEvent()), mockPersistentOnlyCampaign)
            .shouldBeFalse()
    }
}

class EventMatchingUtilEventsSpec : EventMatchingUtilSpec() {
    @Test
    fun `should properly match persistent events`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap)
        eventMatchingUtil.matchAndStore(mockAppStartEv)

        eventMatchingUtil.matchedEvents(mockCampaign).shouldContain(mockAppStartEv)
    }

    @Test
    fun `should properly match non-persistent events`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap)
        eventMatchingUtil.matchAndStore(mockLoginEv)

        eventMatchingUtil.matchedEvents(mockCampaign).shouldContain(mockLoginEv)
    }
}

class EventMatchingUtilContainsSpec : EventMatchingUtilSpec() {
    @Test
    fun `should return true if all required events were stored`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap)
        eventMatchingUtil.matchAndStore(mockAppStartEv)
        eventMatchingUtil.matchAndStore(mockLoginEv)

        eventMatchingUtil.containsAllMatchedEvents(mockCampaign).shouldBeTrue()
    }

    @Test
    fun `should return true if more events than required were stored`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap)
        eventMatchingUtil.matchAndStore(mockAppStartEv)
        eventMatchingUtil.matchAndStore(mockLoginEv)
        eventMatchingUtil.matchAndStore(mockLoginEv)
        eventMatchingUtil.matchAndStore(mockPurchaseEv)

        eventMatchingUtil.containsAllMatchedEvents(mockCampaign).shouldBeTrue()
    }

    @Test
    fun `should return false if not all required events were stored`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap)
        eventMatchingUtil.matchAndStore(mockAppStartEv)

        eventMatchingUtil.containsAllMatchedEvents(mockCampaign).shouldBeFalse()
    }

    @Test
    fun `should return false if none of required events were stored`() {
        eventMatchingUtil.containsAllMatchedEvents(mockCampaign).shouldBeFalse()
    }

    @Test
    fun `should return false if triggers are null or empty`() {
        val campaign = ValidTestMessage(
            campaignId = "test",
            triggers = listOf(),
        )
        eventMatchingUtil.containsAllMatchedEvents(campaign).shouldBeFalse()
    }
}

class EventMatchingUtilClearSpec : EventMatchingUtilSpec() {
    @Test
    fun `should clear all matched non-persistent events`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap)
        eventMatchingUtil.matchAndStore(mockLoginEv)
        eventMatchingUtil.matchedEvents(mockCampaign).shouldNotBeEmpty()

        eventMatchingUtil.clearNonPersistentEvents()
        eventMatchingUtil.matchedEvents(mockCampaign).shouldBeEmpty()
    }

    @Test
    fun `should not clear all persistent events`() {
        `when`(mockCampaignRepo.messages).thenReturn(mockCampaignMap)
        eventMatchingUtil.matchAndStore(mockAppStartEv)
        eventMatchingUtil.matchedEvents(mockCampaign).shouldNotBeEmpty()

        eventMatchingUtil.clearNonPersistentEvents()
        eventMatchingUtil.matchedEvents(mockCampaign).shouldNotBeEmpty()
    }
}
