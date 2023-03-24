//package com.rakuten.tech.mobile.inappmessaging.runtime.utils
//
//import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
//import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
//import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
//import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
//import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
//import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
//import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.LoginSuccessfulEvent
//import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
//import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
//import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*
//import org.amshove.kluent.*
//import org.junit.Before
//import org.junit.Ignore
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.RobolectricTestRunner
//
///**
// * Test class for MessageEventReconciliationUtil.
// */
//@RunWith(RobolectricTestRunner::class)
//@Ignore("base class")
//open class MessageEventReconciliationUtilSpec : BaseTest() {
//    internal val outdatedTestMessage = Message(
//        messagePayload = MessagePayload(
//            title = "testTitle",
//            messageBody = "testBody",
//            header = "testHeader",
//            titleColor = "#000000",
//            headerColor = "#444444",
//            messageBodyColor = "#FAFAFA",
//            backgroundColor = "#FAFAFA",
//            frameColor = "#FF2222",
//            resource = Resource(cropType = 0),
//            messageSettings = MessageSettings(
//                displaySettings = DisplaySettings(
//                    orientation = 0,
//                    slideFrom = SlideFromDirectionType.BOTTOM.typeId,
//                    endTimeMillis = 0,
//                    textAlign = 0,
//                    isOptedOut = false,
//                    isHtml = false,
//                    delay = 0,
//                ),
//                controlSettings = ControlSettings(listOf()),
//            ),
//        ),
//        type = InAppMessageType.MODAL.typeId,
//        triggers = listOf(Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf())),
//        campaignId = "test",
//        isTest = true,
//        maxImpressions = 1,
//        hasNoEndDate = false,
//        isCampaignDismissable = true,
//        areImpressionsInfinite = false,
//    )
//
//    @Before
//    override fun setup() {
//        CampaignRepository.instance().clearMessages()
//        EventMatchingUtil.instance().clearNonPersistentEvents()
//    }
//}
//
//internal class ValidatorHandler {
//    data class Element(
//        val campaign: Message,
//        val events: Set<Event>,
//    )
//    var validatedElements = mutableListOf<Element>()
//    val validatedCampaigns: List<Message>
//        get() = this.validatedElements.map { it.campaign }
//
//    var closure: (Message, Set<Event>) -> Unit = { campaign: Message, events: Set<Event> ->
//        this.validatedElements.add(Element(campaign, events))
//    }
//}
//
//class MessageEventReconciliationUtilCampaignsSpec : MessageEventReconciliationUtilSpec() {
//
//    @Test
//    fun `should accept outdated test campaign`() {
//        CampaignRepository.instance().syncWith(listOf(outdatedTestMessage), 0)
//        val event = LoginSuccessfulEvent()
//        EventMatchingUtil.instance().matchAndStore(event)
//        val handler = ValidatorHandler()
//        MessageEventReconciliationUtil.instance().validate(handler.closure)
//
//        handler.validatedElements.shouldBeEqualTo(
//            listOf(
//                ValidatorHandler.Element(outdatedTestMessage, setOf(event)),
//            ),
//        )
//    }
//
//    @Test
//    fun `should not accept test campaign with impression left less than 1`() {
//        val message = ValidTestMessage("1", maxImpressions = 1, isTest = true)
//        CampaignRepository.instance().syncWith(listOf(message), 0)
//        CampaignRepository.instance().decrementImpressions(message.getCampaignId())
//        val handler = ValidatorHandler()
//        MessageEventReconciliationUtil.instance().validate(handler.closure)
//
//        handler.validatedElements.shouldBeEmpty()
//    }
//
//    @Test
//    fun `should accept every non-test campaign matching criteria`() {
//        val campaign = ValidTestMessage(
//            campaignId = "test",
//            isTest = false,
//            maxImpressions = 2,
//            triggers = listOf(Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf())),
//        )
//        CampaignRepository.instance().syncWith(listOf(campaign), 0)
//        val event = LoginSuccessfulEvent()
//        EventMatchingUtil.instance().matchAndStore(event)
//        val handler = ValidatorHandler()
//        MessageEventReconciliationUtil.instance().validate(handler.closure)
//
//        handler.validatedElements.shouldBeEqualTo(
//            listOf(
//                ValidatorHandler.Element(campaign, setOf(event)),
//            ),
//        )
//    }
//
//    @Test
//    fun `should not accept campaigns with no impressions left`() {
//        val campaign = ValidTestMessage(
//            campaignId = "test",
//            isTest = false,
//            maxImpressions = 0,
//            triggers = listOf(Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf())),
//        )
//        CampaignRepository.instance().syncWith(listOf(campaign), 0)
//        val event = LoginSuccessfulEvent()
//        EventMatchingUtil.instance().matchAndStore(event)
//        val handler = ValidatorHandler()
//        MessageEventReconciliationUtil.instance().validate(handler.closure)
//
//        handler.validatedElements.shouldBeEmpty()
//    }
//
//    @Test
//    fun `should not accept opted out campaigns`() {
//        val campaign = ValidTestMessage(
//            campaignId = "test",
//            isTest = false,
//            maxImpressions = 2,
//            triggers = listOf(Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf())),
//        )
//        CampaignRepository.instance().syncWith(listOf(campaign), 0)
//        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
//        CampaignRepository.instance().optOutCampaign(campaign)
//        val handler = ValidatorHandler()
//        MessageEventReconciliationUtil.instance().validate(handler.closure)
//
//        handler.validatedCampaigns.shouldNotContain(campaign)
//    }
//}
//
//class MessageEventReconciliationUtilTriggerSpec : MessageEventReconciliationUtilSpec() {
//    @Test
//    fun `should accept normal campaign when triggers are satisfied`() {
//        val campaign = ValidTestMessage(
//            campaignId = "test",
//            isTest = false,
//            maxImpressions = 2,
//            triggers = listOf(
//                Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf()),
//                Trigger(0, EventType.APP_START.typeId, "testEvent2", mutableListOf()),
//            ),
//        )
//        CampaignRepository.instance().syncWith(listOf(campaign), 0)
//        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
//        EventMatchingUtil.instance().matchAndStore(AppStartEvent())
//        val handler = ValidatorHandler()
//        MessageEventReconciliationUtil.instance().validate(handler.closure)
//
//        handler.validatedCampaigns.shouldContain(campaign)
//    }
//
//    @Test
//    fun `should accept test campaign when triggers are satisfied`() {
//        val campaign = ValidTestMessage(
//            campaignId = "test",
//            isTest = true,
//            maxImpressions = 2,
//            triggers = listOf(
//                Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf()),
//                Trigger(0, EventType.APP_START.typeId, "testEvent2", mutableListOf()),
//            ),
//        )
//        CampaignRepository.instance().syncWith(listOf(campaign), 0)
//        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
//        EventMatchingUtil.instance().matchAndStore(AppStartEvent())
//        val handler = ValidatorHandler()
//        MessageEventReconciliationUtil.instance().validate(handler.closure)
//
//        handler.validatedCampaigns.shouldContain(campaign)
//    }
//
//    @Test
//    fun `should not accept normal campaign with no triggers`() {
//        val campaign = ValidTestMessage(
//            campaignId = "test",
//            isTest = false,
//            maxImpressions = 2,
//            triggers = listOf(),
//        )
//        CampaignRepository.instance().syncWith(listOf(campaign), 0)
//        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
//        val handler = ValidatorHandler()
//        MessageEventReconciliationUtil.instance().validate(handler.closure)
//
//        handler.validatedCampaigns.shouldNotContain(campaign)
//    }
//
//    @Test
//    fun `should not accept test campaign with no triggers`() {
//        val campaign = ValidTestMessage(
//            campaignId = "test",
//            isTest = true,
//            maxImpressions = 2,
//            triggers = listOf(),
//        )
//        CampaignRepository.instance().syncWith(listOf(campaign), 0)
//        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
//        val handler = ValidatorHandler()
//        MessageEventReconciliationUtil.instance().validate(handler.closure)
//
//        handler.validatedCampaigns.shouldNotContain(campaign)
//    }
//
//    @Test
//    fun `should not accept campaign when not all triggers are satisfied`() {
//        val campaign = ValidTestMessage(
//            campaignId = "test",
//            isTest = false,
//            maxImpressions = 2,
//            triggers = listOf(
//                Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf()),
//                Trigger(0, EventType.PURCHASE_SUCCESSFUL.typeId, "testEvent2", mutableListOf()),
//            ),
//        )
//        CampaignRepository.instance().syncWith(listOf(campaign), 0)
//        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
//        val handler = ValidatorHandler()
//        MessageEventReconciliationUtil.instance().validate(handler.closure)
//
//        handler.validatedCampaigns.shouldNotContain(campaign)
//    }
//}
