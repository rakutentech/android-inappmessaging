package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.LoginSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

/**
 * Test class for MessageEventReconciliationUtil.
 */
@SuppressWarnings(
    "LargeClass",
)
@RunWith(RobolectricTestRunner::class)
class MessageEventReconciliationUtilSpec : BaseTest() {

    private val messageEventReconciliationUtil = MessageEventReconciliationUtil(
        campaignRepo = CampaignRepository.instance(),
        eventMatchingUtil = EventMatchingUtil.instance(),
    )

    @Before
    override fun setup() {
        CampaignRepository.instance().clearMessages()
        EventMatchingUtil.instance().clearNonPersistentEvents()
    }

    @SuppressWarnings(
        "LongMethod",
    )
    @Test
    fun `should accept outdated test campaign`() {
        val outdatedTestMessage = TestDataHelper.createDummyMessage(
            isTest = true,
            messagePayload = TestDataHelper.message0Payload.copy(
                messageSettings = MessageSettings(
                    displaySettings = DisplaySettings(
                        orientation = 0, slideFrom = SlideFromDirectionType.BOTTOM.typeId,
                        endTimeMillis = 0, textAlign = 0, isOptedOut = false, isHtml = false, delay = 0,
                    ),
                    controlSettings = ControlSettings(listOf()),
                ),
            ),
            triggers = listOf(Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf())),
        )
        CampaignRepository.instance().syncWith(listOf(outdatedTestMessage), 0)
        val event = LoginSuccessfulEvent()
        EventMatchingUtil.instance().matchAndStore(event)
        val handler = ValidatorHandler()
        messageEventReconciliationUtil.validate(handler.closure)

        handler.validatedElements.shouldBeEqualTo(
            listOf(
                ValidatorHandler.Element(outdatedTestMessage, setOf(event)),
            ),
        )
    }

    @Test
    fun `should not accept test campaign with impression left less than 1`() {
        val message = TestDataHelper.createDummyMessage(campaignId = "1", isTest = true, maxImpressions = 1)
        CampaignRepository.instance().syncWith(listOf(message), 0)
        CampaignRepository.instance().decrementImpressions(message.campaignId)
        val handler = ValidatorHandler()
        messageEventReconciliationUtil.validate(handler.closure)

        handler.validatedElements.shouldBeEmpty()
    }

    @Test
    fun `should accept every non-test campaign matching criteria`() {
        val campaign = TestDataHelper.createDummyMessage(
            campaignId = "test",
            isTest = false,
            maxImpressions = 2,
            triggers = listOf(Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf())),
        )
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        val event = LoginSuccessfulEvent()
        EventMatchingUtil.instance().matchAndStore(event)
        val handler = ValidatorHandler()
        messageEventReconciliationUtil.validate(handler.closure)

        handler.validatedElements.shouldBeEqualTo(
            listOf(
                ValidatorHandler.Element(campaign, setOf(event)),
            ),
        )
    }

    @Test
    fun `should not accept campaigns with no impressions left`() {
        val campaign = TestDataHelper.createDummyMessage(
            campaignId = "test",
            isTest = false,
            maxImpressions = 0,
            triggers = listOf(Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf())),
        )
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        val event = LoginSuccessfulEvent()
        EventMatchingUtil.instance().matchAndStore(event)
        val handler = ValidatorHandler()
        messageEventReconciliationUtil.validate(handler.closure)

        handler.validatedElements.shouldBeEmpty()
    }

    @Test
    fun `should not accept opted out campaigns`() {
        val campaign = TestDataHelper.createDummyMessage(
            campaignId = "test",
            isTest = false,
            maxImpressions = 2,
            triggers = listOf(Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf())),
        )
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
        CampaignRepository.instance().optOutCampaign(campaign.campaignId)
        val handler = ValidatorHandler()
        messageEventReconciliationUtil.validate(handler.closure)

        handler.validatedCampaigns.shouldNotContain(campaign)
    }

    @Test
    fun `should accept normal campaign when triggers are satisfied`() {
        val campaign = TestDataHelper.createDummyMessage(
            campaignId = "test",
            isTest = false,
            maxImpressions = 2,
            triggers = listOf(
                Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf()),
                Trigger(0, EventType.APP_START.typeId, "testEvent2", mutableListOf()),
            ),
        )
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
        EventMatchingUtil.instance().matchAndStore(AppStartEvent())
        val handler = ValidatorHandler()
        messageEventReconciliationUtil.validate(handler.closure)

        handler.validatedCampaigns.shouldContain(campaign)
    }

    @Test
    fun `should accept test campaign when triggers are satisfied`() {
        val campaign = TestDataHelper.createDummyMessage(
            campaignId = "test",
            isTest = true,
            maxImpressions = 2,
            triggers = listOf(
                Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf()),
                Trigger(0, EventType.APP_START.typeId, "testEvent2", mutableListOf()),
            ),
        )
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
        EventMatchingUtil.instance().matchAndStore(AppStartEvent())
        val handler = ValidatorHandler()
        messageEventReconciliationUtil.validate(handler.closure)

        handler.validatedCampaigns.shouldContain(campaign)
    }

    @Test
    fun `should not accept normal campaign with no triggers`() {
        val campaign = TestDataHelper.createDummyMessage(
            campaignId = "test",
            isTest = false,
            maxImpressions = 2,
            triggers = listOf(),
        )
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
        val handler = ValidatorHandler()
        messageEventReconciliationUtil.validate(handler.closure)

        handler.validatedCampaigns.shouldNotContain(campaign)
    }

    @Test
    fun `should not accept test campaign with no triggers`() {
        val campaign = TestDataHelper.createDummyMessage(
            campaignId = "test",
            isTest = true,
            maxImpressions = 2,
            triggers = listOf(),
        )
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
        val handler = ValidatorHandler()
        messageEventReconciliationUtil.validate(handler.closure)

        handler.validatedCampaigns.shouldNotContain(campaign)
    }

    @Test
    fun `should not accept campaign when not all triggers are satisfied`() {
        val campaign = TestDataHelper.createDummyMessage(
            campaignId = "test",
            isTest = false,
            maxImpressions = 2,
            triggers = listOf(
                Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf()),
                Trigger(0, EventType.PURCHASE_SUCCESSFUL.typeId, "testEvent2", mutableListOf()),
            ),
        )
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
        val handler = ValidatorHandler()
        messageEventReconciliationUtil.validate(handler.closure)

        handler.validatedCampaigns.shouldNotContain(campaign)
    }

    @Test
    fun `should return empty when logged events is empty`() {
        val message = TestDataHelper.createDummyMessage()
        val mockEventMatchingUtil = mock(EventMatchingUtil::class.java)
        `when`(mockEventMatchingUtil.containsAllMatchedEvents(message)).thenReturn(true)
        `when`(mockEventMatchingUtil.matchedEvents(message)).thenReturn(listOf())

        CampaignRepository.instance().syncWith(listOf(message), 0)

        val util = MessageEventReconciliationUtil(
            campaignRepo = CampaignRepository.instance(),
            eventMatchingUtil = mockEventMatchingUtil,
        )
        val handler = ValidatorHandler()
        util.validate(handler.closure)
        handler.validatedCampaigns.shouldBeEmpty()
    }

    @Test
    fun `should return empty when no event is found for trigger`() {
        val message = TestDataHelper.createDummyMessage()
        val mockEventMatchingUtil = mock(EventMatchingUtil::class.java)
        `when`(mockEventMatchingUtil.containsAllMatchedEvents(message)).thenReturn(true)
        `when`(mockEventMatchingUtil.matchedEvents(message)).thenReturn(listOf(PurchaseSuccessfulEvent()))

        CampaignRepository.instance().syncWith(listOf(message), 0)

        val util = MessageEventReconciliationUtil(
            campaignRepo = CampaignRepository.instance(),
            eventMatchingUtil = mockEventMatchingUtil,
        )
        val handler = ValidatorHandler()
        util.validate(handler.closure)
        handler.validatedCampaigns.shouldBeEmpty()
    }
}

internal class ValidatorHandler {
    data class Element(
        val campaign: Message,
        val events: Set<Event>,
    )
    var validatedElements = mutableListOf<Element>()
    val validatedCampaigns: List<Message>
        get() = this.validatedElements.map { it.campaign }

    var closure: (Message, Set<Event>) -> Unit = { campaign: Message, events: Set<Event> ->
        this.validatedElements.add(Element(campaign, events))
    }
}
