package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.os.Build
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.SlideFromDirectionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ValueType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.CustomEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.LoginSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

/**
 * Test class for MessageEventReconciliationUtil.
 */
@SuppressWarnings(
    "LargeClass",
    "ClassOrdering"
)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class MessageEventReconciliationUtilSpec : BaseTest() {

    @Before
    override fun setup() {
        CampaignRepository.instance().clearMessages()
        EventMatchingUtil.instance().clearNonPersistentEvents()
    }

    // region validating campaigns

    @Test
    fun `should accept test campaign (not looking at triggers)`() {
        CampaignRepository.instance().syncWith(listOf(testCampaign), 0)
        val handler = ValidatorHandler()
        MessageEventReconciliationUtil.instance().validate(handler.closure)

        handler.validatedElements.shouldBeEqualTo(
            listOf(
                ValidatorHandler.Element(testCampaign, emptySet())
            )
        )
    }

    @Test
    fun `should not accept outdated test campaign`() {
        CampaignRepository.instance().syncWith(listOf(outdatedTestCampaign), 0)
        val handler = ValidatorHandler()
        MessageEventReconciliationUtil.instance().validate(handler.closure)

        handler.validatedElements.shouldBeEmpty()
    }

    @Test
    fun `should not accept test campaign with impression left less than 1`() {
        val message = ValidTestMessage("1", maxImpressions = 1, isTest = true)
        CampaignRepository.instance().syncWith(listOf(message), 0)
        CampaignRepository.instance().decrementImpressions(message.getCampaignId())
        val handler = ValidatorHandler()
        MessageEventReconciliationUtil.instance().validate(handler.closure)

        handler.validatedElements.shouldBeEmpty()
    }

    @Test
    fun `should accept every non-test campaign matching criteria`() {
        val campaign = ValidTestMessage(
            campaignId = "test",
            isTest = false,
            maxImpressions = 2,
            triggers = listOf(Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf())),
        )
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        val event = LoginSuccessfulEvent()
        EventMatchingUtil.instance().matchAndStore(event)
        val handler = ValidatorHandler()
        MessageEventReconciliationUtil.instance().validate(handler.closure)

        handler.validatedElements.shouldBeEqualTo(
            listOf(
                ValidatorHandler.Element(campaign, setOf(event))
            )
        )
    }

    @Test
    fun `should not accept campaigns with no impressions left`() {
        val campaign = ValidTestMessage(
            campaignId = "test",
            isTest = false,
            maxImpressions = 0,
            triggers = listOf(Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf())),
        )
        CampaignRepository.instance().syncWith(listOf(campaign,), 0)
        val event = LoginSuccessfulEvent()
        EventMatchingUtil.instance().matchAndStore(event)
        val handler = ValidatorHandler()
        MessageEventReconciliationUtil.instance().validate(handler.closure)

        handler.validatedElements.shouldBeEmpty()
    }

    @Test
    fun `should not accept opted out campaigns`() {
        val campaign = ValidTestMessage(
            campaignId = "test",
            isTest = false,
            maxImpressions = 2,
            triggers = listOf(Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf())),
        )
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
        CampaignRepository.instance().optOutCampaign(campaign)
        val handler = ValidatorHandler()
        MessageEventReconciliationUtil.instance().validate(handler.closure)

        handler.validatedCampaigns.shouldNotContain(campaign)
    }

    // endregion

    // region triggers

    @Test
    fun `should accept campaign when triggers are satisfied`() {
        val campaign = ValidTestMessage(
            campaignId = "test",
            isTest = false,
            maxImpressions = 2,
            triggers = listOf(
                Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf()),
                Trigger(0, EventType.APP_START.typeId, "testEvent2", mutableListOf())
            ),
        )
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
        EventMatchingUtil.instance().matchAndStore(AppStartEvent())
        val handler = ValidatorHandler()
        MessageEventReconciliationUtil.instance().validate(handler.closure)

        handler.validatedCampaigns.shouldContain(campaign)
    }

    @Test
    fun `should not accept campaign with no triggers`() {
        val campaign = ValidTestMessage(
            campaignId = "test",
            isTest = false,
            maxImpressions = 2,
            triggers = listOf()
        )
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
        val handler = ValidatorHandler()
        MessageEventReconciliationUtil.instance().validate(handler.closure)

        handler.validatedCampaigns.shouldNotContain(campaign)
    }

    @Test
    fun `should not accept campaign when not all triggers are satisfied`() {
        val campaign = ValidTestMessage(
            campaignId = "test",
            isTest = false,
            maxImpressions = 2,
            triggers = listOf(
                Trigger(0, EventType.LOGIN_SUCCESSFUL.typeId, "testEvent", mutableListOf()),
                Trigger(0, EventType.PURCHASE_SUCCESSFUL.typeId, "testEvent2", mutableListOf())
            ),
        )
        CampaignRepository.instance().syncWith(listOf(campaign), 0)
        EventMatchingUtil.instance().matchAndStore(LoginSuccessfulEvent())
        val handler = ValidatorHandler()
        MessageEventReconciliationUtil.instance().validate(handler.closure)

        handler.validatedCampaigns.shouldNotContain(campaign)
    }

    // endregion

    // region trigger attributes

    @Test
    fun `should accept message with valid string trigger`() {
        testValueType(ValueType.STRING)
    }

    @Test
    fun `should accept message with valid integer trigger`() {
        testValueType(ValueType.INTEGER)
    }

    @Test
    fun `should accept message with valid double trigger`() {
        testValueType(ValueType.DOUBLE)
    }

    @Test
    fun `should accept message with valid boolean trigger`() {
        testValueType(ValueType.BOOLEAN)
    }

    @Test
    fun `should accept message with valid time trigger`() {
        testValueType(ValueType.TIME_IN_MILLI)
    }

    @SuppressWarnings("LongMethod")
    private fun testValueType(valueType: ValueType) {
        var trigger: Trigger? = null
        val customEvent = CustomEvent("custom")

        when (valueType) {
            ValueType.STRING -> {
                trigger = Trigger(
                    0, EventType.CUSTOM.typeId, "custom",
                    mutableListOf(
                        TriggerAttribute("name", "value", 1, 1)
                    )
                )
                customEvent.addAttribute("name", "value")
            }
            ValueType.INTEGER -> {
                trigger = Trigger(
                    0, EventType.CUSTOM.typeId, "custom",
                    mutableListOf(
                        TriggerAttribute("name", "1", 2, 1)
                    )
                )
                customEvent.addAttribute("name", 1)
            }
            ValueType.DOUBLE -> {
                trigger = Trigger(
                    0, EventType.CUSTOM.typeId, "custom",
                    mutableListOf(
                        TriggerAttribute("name", "1.0", 3, 1)
                    )
                )
                customEvent.addAttribute("name", 1.0)
            }
            ValueType.BOOLEAN -> {
                trigger = Trigger(
                    0, EventType.CUSTOM.typeId, "custom",
                    mutableListOf(
                        TriggerAttribute("name", "true", 4, 1)
                    )
                )
                customEvent.addAttribute("name", true)
            }
            ValueType.TIME_IN_MILLI -> {
                val currDate = Date()
                trigger = Trigger(
                    0, EventType.CUSTOM.typeId, "custom",
                    mutableListOf(
                        TriggerAttribute("name", currDate.time.toString(), 5, 1)
                    )
                )
                customEvent.addAttribute("name", currDate)
            }
            else -> {}
        }

        trigger?.let {
            val campaign = ValidTestMessage(
                campaignId = "test",
                isTest = false,
                maxImpressions = 2,
                triggers = listOf(it)
            )
            CampaignRepository.instance().syncWith(listOf(campaign), 0)
            EventMatchingUtil.instance().matchAndStore(customEvent)
            val handler = ValidatorHandler()
            MessageEventReconciliationUtil.instance().validate(handler.closure)

            handler.validatedElements.shouldHaveSize(1)
        }
    }

    // endregion

    // region init mock campaigns

    private val testCampaign = CampaignData(
        campaignId = "test",
        maxImpressions = 1,
        type = InAppMessageType.MODAL.typeId,
        triggers = listOf(),
        isTest = true,
        infiniteImpressions = false,
        hasNoEndDate = false,
        isCampaignDismissable = true,
        messagePayload = MessagePayload(
            title = "testTitle",
            messageBody = "testBody",
            header = "testHeader",
            titleColor = "#000000",
            headerColor = "#444444",
            messageBodyColor = "#FAFAFA",
            backgroundColor = "#FAFAFA",
            frameColor = "#FF2222",
            resource = Resource(cropType = 0),
            messageSettings = MessageSettings(
                displaySettings = DisplaySettings(
                    orientation = 0,
                    slideFrom = SlideFromDirectionType.BOTTOM.typeId,
                    endTimeMillis = Long.MAX_VALUE,
                    textAlign = 0,
                    optOut = false,
                    html = false,
                    delay = 0,
                ),
                controlSettings = ControlSettings(listOf())
            )
        )
    )

    private val outdatedTestCampaign = CampaignData(
        campaignId = "test",
        maxImpressions = 1,
        type = InAppMessageType.MODAL.typeId,
        triggers = listOf(),
        isTest = true,
        infiniteImpressions = false,
        hasNoEndDate = false,
        isCampaignDismissable = true,
        messagePayload = MessagePayload(
            title = "testTitle",
            messageBody = "testBody",
            header = "testHeader",
            titleColor = "#000000",
            headerColor = "#444444",
            messageBodyColor = "#FAFAFA",
            backgroundColor = "#FAFAFA",
            frameColor = "#FF2222",
            resource = Resource(cropType = 0),
            messageSettings = MessageSettings(
                displaySettings = DisplaySettings(
                    orientation = 0,
                    slideFrom = SlideFromDirectionType.BOTTOM.typeId,
                    endTimeMillis = 0,
                    textAlign = 0,
                    optOut = false,
                    html = false,
                    delay = 0,
                ),
                controlSettings = ControlSettings(listOf())
            )
        )
    )

    // endregion
}

internal class ValidatorHandler {
    data class Element(
        val campaign: Message,
        val events: Set<Event>
    )
    var validatedElements = mutableListOf<Element>()
    var validatedCampaigns: List<Message> = listOf()
        get() = this.validatedElements.map { it.campaign }

    var closure: (Message, Set<Event>) -> Unit = { campaign: Message, events: Set<Event> ->
        this.validatedElements.add(Element(campaign, events))
    }
}
