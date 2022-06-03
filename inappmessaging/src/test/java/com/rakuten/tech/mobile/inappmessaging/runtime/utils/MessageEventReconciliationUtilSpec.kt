package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalDisplayedMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalEventRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.DisplaySettings
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageSettings
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.TriggerAttribute
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.MessageEventReconciliationWorker
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*
import kotlin.collections.ArrayList

/**
 * Test class for MessageEventReconciliationUtil.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@Ignore("base class")
open class MessageEventReconciliationUtilSpec : BaseTest() {

    val appStartEvent = AppStartEvent()
    val customEvent = CustomEvent("custom")
    internal val message1 = Mockito.mock(Message::class.java)
    internal val message2 = Mockito.mock(Message::class.java)
    private val messageSettings = Mockito.mock(MessageSettings::class.java)
    private val displaySettings = Mockito.mock(DisplaySettings::class.java)
    internal val payload = Mockito.mock(MessagePayload::class.java)
    internal val testMessage = Mockito.mock(Message::class.java)
    internal val trigger1 = Mockito.mock(Trigger::class.java)
    private val trigger2 = Mockito.mock(Trigger::class.java)
    private val trigger3 = Mockito.mock(Trigger::class.java)
    private val trigger4 = Mockito.mock(Trigger::class.java)

    /**
     * Setting up data and mocks.
     */
    @Before
    @SuppressWarnings("LongMethod")
    override fun setup() {
        super.setup()
        // Arranging message1's data.
        val trigger1List = ArrayList<Trigger>()
        trigger1List.add(trigger1)
        trigger1List.add(trigger2)
        trigger1List.add(trigger3)
        trigger1List.add(trigger4)
        `when`(trigger1.eventType).thenReturn(1)
        `when`(trigger2.eventType).thenReturn(2)
        `when`(trigger3.eventType).thenReturn(3)
        `when`(trigger4.eventType).thenReturn(4)

        // non outdated message
        `when`(displaySettings.endTimeMillis).thenReturn(Date().time + 60 * 60 * 60 * 1000)
        `when`(messageSettings.displaySettings).thenReturn(displaySettings)
        `when`(payload.messageSettings).thenReturn(messageSettings)
        `when`(message1.getMaxImpressions()).thenReturn(1)
        `when`(message1.getTriggers()).thenReturn(trigger1List)
        `when`(message1.getCampaignId()).thenReturn("message1")
        `when`(message1.isTest()).thenReturn(false)
        `when`(message1.getMessagePayload()).thenReturn(payload)
        `when`(message2.getMessagePayload()).thenReturn(payload)

        // Arranging message2's data.
        val trigger2List = ArrayList<Trigger>()
        trigger2List.add(trigger1)
        `when`(message2.getMaxImpressions()).thenReturn(2)
        `when`(message2.getTriggers()).thenReturn(trigger2List)
        `when`(message2.getCampaignId()).thenReturn("message2")
        `when`(message2.isTest()).thenReturn(false)
        `when`(message2.getMessagePayload()).thenReturn(payload)
        // Arrange test message's data.
        `when`(testMessage.getCampaignId()).thenReturn("test")
        `when`(testMessage.isTest()).thenReturn(true)

        LocalDisplayedMessageRepository.instance().clearMessages()
        LocalEventRepository.instance().clearEvents()
    }

    internal fun arrangeInputDataWithDate(attriName: String): MutableList<Message> {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val currDate = Date()
        val triggerAttr = TriggerAttribute("name", currDate.time.toString(), 5, 1)
        val attrList = ArrayList<TriggerAttribute>()
        attrList.add(triggerAttr)
        `when`(trigger1.triggerAttributes).thenReturn(attrList)
        `when`(trigger1.eventType).thenReturn(4)
        `when`(trigger1.eventName).thenReturn("custom")
        customEvent.addAttribute(attriName, currDate)
        LocalEventRepository.instance().addEvent(customEvent)
        // Act.
        return MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
    }
}

class MessageEventReconciliationUtilExtractSpec : MessageEventReconciliationUtilSpec() {

    @Test
    fun `should extract test messages`() {
        val messageList = ArrayList<Message>()
        messageList.add(message1)
        messageList.add(message2)
        messageList.add(testMessage)
        val testMessageList = MessageEventReconciliationUtil.instance().extractTestMessages(messageList)
        testMessageList[0] shouldBeEqualTo testMessage
        testMessageList.shouldHaveSize(1)
    }
}

@SuppressWarnings("LargeClass")
class MessageEventReconciliationUtilReconcileSpec : MessageEventReconciliationUtilSpec() {

    @Test
    fun `should reconcile message with base attributes`() {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        LocalEventRepository.instance().addEvent(appStartEvent)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList[0] shouldBeEqualTo message2
        outputMessageList.shouldHaveSize(1)
    }

    @Test
    fun `should not add same message multiple times`() {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        outputMessageList.shouldHaveSize(0)
    }

    @Test
    fun `should not reconcile test message`() {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(testMessage)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(0)
    }

    @Test
    fun `should check message for max impressions`() {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message1)
        // Act.
        MessageEventReconciliationUtil.instance().reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        Mockito.verify(message1).getMaxImpressions()
    }

    @Test
    fun `should reconcile message with valid string trigger`() {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        setupCustomEventStringAttribute()
        LocalEventRepository.instance().addEvent(customEvent)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(1)
    }

    @Test
    fun `should reconcile message with valid integer trigger`() {
        // Arrange input data.
        val inputMessageList = setupMessageTrigger()
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(1)
    }

    @Test
    fun `should reconcile message with valid double trigger`() {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val triggerAttr = TriggerAttribute("name", "1.0", 3, 1)
        val attrList = ArrayList<TriggerAttribute>()
        attrList.add(triggerAttr)
        `when`(trigger1.triggerAttributes).thenReturn(attrList)
        `when`(trigger1.eventType).thenReturn(4)
        `when`(trigger1.eventName).thenReturn("custom")
        customEvent.addAttribute("name", 1.0)
        LocalEventRepository.instance().addEvent(customEvent)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(1)
    }

    @Test
    fun `should reconcile message with valid boolean trigger`() {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val triggerAttr = TriggerAttribute("name", "true", 4, 1)
        val attrList = ArrayList<TriggerAttribute>()
        attrList.add(triggerAttr)
        `when`(trigger1.triggerAttributes).thenReturn(attrList)
        `when`(trigger1.eventType).thenReturn(4)
        `when`(trigger1.eventName).thenReturn("custom")
        customEvent.addAttribute("name", true)
        LocalEventRepository.instance().addEvent(customEvent)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(1)
    }

    @Test
    fun `should not reconcile message when events are not enough due to times closed`() {
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        `when`(message2.getNumberOfTimesClosed()).thenReturn(1)
        setupCustomEventStringAttribute()
        LocalEventRepository.instance().addEvent(customEvent)

        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)

        outputMessageList.shouldBeEmpty()
    }

    @Test
    fun `should reconcile message when events are enough with to times closes`() {
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        `when`(message2.getNumberOfTimesClosed()).thenReturn(1)
        setupCustomEventStringAttribute()
        LocalEventRepository.instance().addEvent(customEvent)
        LocalEventRepository.instance().addEvent(customEvent)

        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)

        outputMessageList.shouldHaveSize(1)
    }

    @Test
    fun `should reconcile message with valid time trigger`() {
        val outputMessageList = arrangeInputDataWithDate("name")

        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(1)
    }

    @Test
    fun `should be called once`() {
        val mockPingRepo = Mockito.mock(CampaignMessageRepository::class.java)
        val mockReconUtil = Mockito.mock(MessageEventReconciliationUtil::class.java)
        val messageList = ArrayList<Message>()
        messageList.add(message1)
        messageList.add(message2)
        `when`(mockPingRepo.getAllMessagesCopy()).thenReturn(messageList)
        `when`(mockReconUtil.extractTestMessages(messageList)).thenReturn(messageList)
        `when`(mockReconUtil.reconcileMessagesAndEvents(messageList)).thenReturn(messageList)

        val workerParameters = Mockito.mock(WorkerParameters::class.java)
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        val worker = MessageEventReconciliationWorker(
            ApplicationProvider.getApplicationContext(), workerParameters,
            mockPingRepo, mockReconUtil
        )
        worker.doWork()

        Mockito.verify(mockReconUtil).extractTestMessages(messageList)
        Mockito.verify(mockReconUtil).reconcileMessagesAndEvents(messageList)
    }

    @Test
    fun `should check required set of satisfied triggers for persistent type (app start)`() {
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val attrList = ArrayList<TriggerAttribute>()
        // App Start (Persistent Type)
        `when`(trigger1.triggerAttributes).thenReturn(attrList)
        `when`(trigger1.eventType).thenReturn(EventType.APP_START.typeId)
        `when`(trigger1.eventName).thenReturn(EventType.APP_START.name)

        // only App Start Event is added in local repo
        LocalEventRepository.instance().addEvent(appStartEvent)

        // first display
        var outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(1)
        outputMessageList[0].getCampaignId() shouldBeEqualTo "message2"

        LocalDisplayedMessageRepository.instance().addMessage(message2) // increment "displayed" times

        // second display
        outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldBeEmpty()
    }

    @Test
    fun `should not reconcile message with invalid event`() {
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val attrList = ArrayList<TriggerAttribute>()
        `when`(trigger1.triggerAttributes).thenReturn(attrList)
        `when`(trigger1.eventType).thenReturn(EventType.INVALID.typeId)
        `when`(trigger1.eventName).thenReturn(EventType.APP_START.name)

        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        outputMessageList.shouldHaveSize(0)
    }

    @Test
    fun `should not reconcile message with invalid event type`() {
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val attrList = ArrayList<TriggerAttribute>()
        `when`(trigger1.triggerAttributes).thenReturn(attrList)
        `when`(trigger1.eventType).thenReturn(10)
        `when`(trigger1.eventName).thenReturn(EventType.APP_START.name)

        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        outputMessageList.shouldHaveSize(0)
    }

    @Test
    fun `should consider required set of satisfied triggers for non-persistent type (login)`() {
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val attrList = ArrayList<TriggerAttribute>()
        // Login Successful (Non-Persistent Type)
        `when`(trigger1.triggerAttributes).thenReturn(attrList)
        `when`(trigger1.eventType).thenReturn(EventType.LOGIN_SUCCESSFUL.typeId)
        `when`(trigger1.eventName).thenReturn(EventType.LOGIN_SUCCESSFUL.name)

        // only custom event is added in local repo
        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())

        // first display
        var outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(1)
        outputMessageList[0].getCampaignId() shouldBeEqualTo "message2"

        LocalDisplayedMessageRepository.instance().addMessage(message2) // increment "displayed" times

        // second display
        outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(0)
    }

    @Test
    fun `should consider required set of satisfied triggers for non-persistent type (purchase)`() {
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val attrList = ArrayList<TriggerAttribute>()
        // Purchase Successful (Non-Persistent Type)
        `when`(trigger1.triggerAttributes).thenReturn(attrList)
        `when`(trigger1.eventType).thenReturn(EventType.PURCHASE_SUCCESSFUL.typeId)
        `when`(trigger1.eventName).thenReturn(EventType.PURCHASE_SUCCESSFUL.name)

        // only custom event is added in local repo
        LocalEventRepository.instance().addEvent(PurchaseSuccessfulEvent())

        // first display
        var outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(1)
        outputMessageList[0].getCampaignId() shouldBeEqualTo "message2"

        LocalDisplayedMessageRepository.instance().addMessage(message2) // increment "displayed" times

        // second display
        outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(0)
    }

    @Test
    fun `should consider required set of satisfied triggers for non-persistent type (custom)`() {
        val inputMessageList = setupMessageTrigger()

        // first display
        var outputMessageList = MessageEventReconciliationUtil.instance().reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(1)
        outputMessageList[0].getCampaignId() shouldBeEqualTo "message2"

        LocalDisplayedMessageRepository.instance().addMessage(message2) // increment "displayed" times

        // second display
        outputMessageList = MessageEventReconciliationUtil.instance().reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(0)
    }

    @Test
    fun `should reconcile message with upper case from response`() {
        confirmCustomEvent("EVENT", "ATTRIBUTE", "VALUE", "event", "attribute", "value")
    }

    @Test
    fun `should reconcile message with lower case from response`() {
        confirmCustomEvent("event", "attribute", "value", "EVENT", "ATTRIBUTE", "VALUE")
    }

    @SuppressWarnings("LongMethod")
    private fun confirmCustomEvent(
        triggerName: String,
        triggerAttri: String,
        triggerValue: String,
        eventName: String,
        eventAttri: String,
        eventValue: String
    ) {
        val customAttribute = TriggerAttribute(triggerAttri, triggerValue, 1, 1)
        val customTrigger = Trigger(1, 4, triggerName, mutableListOf(customAttribute))
        val customMsg = Mockito.mock(Message::class.java)
        `when`(customMsg.getCampaignId()).thenReturn("test-id")
        `when`(customMsg.getMaxImpressions()).thenReturn(1)
        `when`(customMsg.getTriggers()).thenReturn(listOf(customTrigger))
        `when`(customMsg.isTest()).thenReturn(false)
        `when`(customMsg.getMessagePayload()).thenReturn(payload)

        LocalEventRepository.instance().addEvent(CustomEvent(eventName).addAttribute(eventAttri, eventValue))

        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(listOf(customMsg))
        outputMessageList.shouldHaveSize(1)
    }

    private fun setupMessageTrigger(): ArrayList<Message> {
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val triggerAttr = TriggerAttribute("name", "1", 2, 1)
        val attrList = ArrayList<TriggerAttribute>()
        attrList.add(triggerAttr)
        `when`(trigger1.triggerAttributes).thenReturn(attrList)
        `when`(trigger1.eventType).thenReturn(4)
        `when`(trigger1.eventName).thenReturn("custom")
        customEvent.addAttribute("name", 1)
        LocalEventRepository.instance().addEvent(customEvent)
        return inputMessageList
    }

    private fun setupCustomEventStringAttribute() {
        val triggerAttr = TriggerAttribute("name", "value", 1, 1)
        val attrList = ArrayList<TriggerAttribute>()
        attrList.add(triggerAttr)
        `when`(trigger1.triggerAttributes).thenReturn(attrList)
        `when`(trigger1.eventType).thenReturn(4)
        `when`(trigger1.eventName).thenReturn("custom")
        customEvent.addAttribute("name", "value")
    }
}

class MessageEventReconciliationUtilReconcileInvalidSpec : MessageEventReconciliationUtilSpec() {

    @Test(expected = UnsupportedOperationException::class)
    fun `should modify reconciled messages throw exception`() {
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(ArrayList())
        // Re-assert the output with qualifying event.
        outputMessageList.add(message1)
    }

    @Test
    fun `should not reconcile message with invalid event`() {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val mockEvent = Mockito.mock(Event::class.java)
        `when`(mockEvent.getEventType()).thenReturn(EventType.INVALID.typeId)
        `when`(mockEvent.getEventName()).thenReturn("invalid")
        LocalEventRepository.instance().addEvent(mockEvent)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(0)
    }

    @Test
    fun `should not reconcile message with mismatched attribute name`() {
        // Act.
        val outputMessageList = arrangeInputDataWithDate("name2")
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(0)
    }

    @Test
    fun `should not reconcile message with invalid value type`() {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val currDate = Date()
        val triggerAttr = TriggerAttribute("name", currDate.time.toString(), 0, 1)
        val attrList = ArrayList<TriggerAttribute>()
        attrList.add(triggerAttr)
        `when`(trigger1.triggerAttributes).thenReturn(attrList)
        `when`(trigger1.eventType).thenReturn(4)
        `when`(trigger1.eventName).thenReturn("custom")
        customEvent.addAttribute("name", currDate)
        LocalEventRepository.instance().addEvent(customEvent)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(0)
    }

    @Test
    fun `should not reconcile message with incorrect value type`() {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val currDate = Date()
        val triggerAttr = TriggerAttribute("name", currDate.time.toString(), 6, 1)
        val attrList = ArrayList<TriggerAttribute>()
        attrList.add(triggerAttr)
        `when`(trigger1.triggerAttributes).thenReturn(attrList)
        `when`(trigger1.eventType).thenReturn(4)
        `when`(trigger1.eventName).thenReturn("custom")
        customEvent.addAttribute("name", currDate)
        LocalEventRepository.instance().addEvent(customEvent)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(0)
    }

    @Test
    fun `should not reconcile message with null max impression`() {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val currDate = Date()
        val triggerAttr = TriggerAttribute("name", currDate.time.toString(), 6, 1)
        val attrList = ArrayList<TriggerAttribute>()
        attrList.add(triggerAttr)
        `when`(trigger1.triggerAttributes).thenReturn(attrList)
        `when`(trigger1.eventType).thenReturn(4)
        `when`(trigger1.eventName).thenReturn("custom")
        `when`(message2.getMaxImpressions()).thenReturn(0)
        customEvent.addAttribute("name", currDate)
        LocalEventRepository.instance().addEvent(customEvent)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
            .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(0)
    }
}
