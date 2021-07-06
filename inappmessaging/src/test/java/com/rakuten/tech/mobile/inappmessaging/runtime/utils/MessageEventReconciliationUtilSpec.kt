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
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.PingResponseMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.TriggerAttribute
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.MessageEventReconciliationWorker
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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
        When calling trigger1.eventType itReturns 1
        When calling trigger2.eventType itReturns 2
        When calling trigger3.eventType itReturns 3
        When calling trigger4.eventType itReturns 4
        When calling message1.getMaxImpressions() itReturns 1
        When calling message1.getTriggers() itReturns trigger1List
        When calling message1.getCampaignId() itReturns "message1"
        When calling message1.isTest() itReturns false

        // Arranging message2's data.
        val trigger2List = ArrayList<Trigger>()
        trigger2List.add(trigger1)
        When calling message2.getMaxImpressions() itReturns 2
        When calling message2.getTriggers() itReturns trigger2List
        When calling message2.getCampaignId() itReturns "message2"
        When calling message2.isTest() itReturns false
        // Arrange test message's data.
        When calling testMessage.getCampaignId() itReturns "test"
        When calling testMessage.isTest() itReturns true

        LocalDisplayedMessageRepository.instance().clearMessages()
        LocalEventRepository.instance().clearEvents()
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
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val triggerAttr = TriggerAttribute("name", "1", 2, 1)
        val attrList = ArrayList<TriggerAttribute>()
        attrList.add(triggerAttr)
        When calling trigger1.triggerAttributes itReturns attrList
        When calling trigger1.eventType itReturns 4
        When calling trigger1.eventName itReturns "custom"
        customEvent.addAttribute("name", 1)
        LocalEventRepository.instance().addEvent(customEvent)
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
        When calling trigger1.triggerAttributes itReturns attrList
        When calling trigger1.eventType itReturns 4
        When calling trigger1.eventName itReturns "custom"
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
        When calling trigger1.triggerAttributes itReturns attrList
        When calling trigger1.eventType itReturns 4
        When calling trigger1.eventName itReturns "custom"
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
        When calling message2.getNumberOfTimesClosed() itReturns 1
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
        When calling message2.getNumberOfTimesClosed() itReturns 1
        setupCustomEventStringAttribute()
        LocalEventRepository.instance().addEvent(customEvent)
        LocalEventRepository.instance().addEvent(customEvent)

        val outputMessageList = MessageEventReconciliationUtil.instance()
                .reconcileMessagesAndEvents(inputMessageList)

        outputMessageList.shouldHaveSize(1)
    }

    @Test
    fun `should reconcile message with valid time trigger`() {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val currDate = Date()
        val triggerAttr = TriggerAttribute("name", currDate.time.toString(), 5, 1)
        val attrList = ArrayList<TriggerAttribute>()
        attrList.add(triggerAttr)
        When calling trigger1.triggerAttributes itReturns attrList
        When calling trigger1.eventType itReturns 4
        When calling trigger1.eventName itReturns "custom"
        customEvent.addAttribute("name", currDate)
        LocalEventRepository.instance().addEvent(customEvent)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
                .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(1)
    }

    @Test
    fun `should be called once`() {
        val mockPingRepo = Mockito.mock(PingResponseMessageRepository::class.java)
        val mockReconUtil = Mockito.mock(MessageEventReconciliationUtil::class.java)
        val messageList = ArrayList<Message>()
        messageList.add(message1)
        messageList.add(message2)
        When calling mockPingRepo.getAllMessagesCopy() itReturns messageList
        When calling mockReconUtil.extractTestMessages(messageList) itReturns messageList
        When calling mockReconUtil.reconcileMessagesAndEvents(messageList) itReturns messageList

        val workerParameters = Mockito.mock(WorkerParameters::class.java)
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        val worker = MessageEventReconciliationWorker(ApplicationProvider.getApplicationContext(), workerParameters,
                mockPingRepo, mockReconUtil)
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
        When calling trigger1.triggerAttributes itReturns attrList
        When calling trigger1.eventType itReturns EventType.APP_START.typeId
        When calling trigger1.eventName itReturns EventType.APP_START.name

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
    fun `should consider required set of satisfied triggers for non-persistent type (login)`() {
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val attrList = ArrayList<TriggerAttribute>()
        // Login Successful (Non-Persistent Type)
        When calling trigger1.triggerAttributes itReturns attrList
        When calling trigger1.eventType itReturns EventType.LOGIN_SUCCESSFUL.typeId
        When calling trigger1.eventName itReturns EventType.LOGIN_SUCCESSFUL.name

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
        When calling trigger1.triggerAttributes itReturns attrList
        When calling trigger1.eventType itReturns EventType.PURCHASE_SUCCESSFUL.typeId
        When calling trigger1.eventName itReturns EventType.PURCHASE_SUCCESSFUL.name

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
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val triggerAttr = TriggerAttribute("name", "1", 2, 1)
        val attrList = ArrayList<TriggerAttribute>()
        attrList.add(triggerAttr)
        // Custom (Non-Persistent Type)
        When calling trigger1.triggerAttributes itReturns attrList
        When calling trigger1.eventType itReturns 4
        When calling trigger1.eventName itReturns "custom"
        customEvent.addAttribute("name", 1)
        // only custom event is added in local repo
        LocalEventRepository.instance().addEvent(customEvent)

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

    private fun setupCustomEventStringAttribute() {
        val triggerAttr = TriggerAttribute("name", "value", 1, 1)
        val attrList = ArrayList<TriggerAttribute>()
        attrList.add(triggerAttr)
        When calling trigger1.triggerAttributes itReturns attrList
        When calling trigger1.eventType itReturns 4
        When calling trigger1.eventName itReturns "custom"
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
        When calling mockEvent.getEventType() itReturns EventType.INVALID.typeId
        When calling mockEvent.getEventName() itReturns "invalid"
        LocalEventRepository.instance().addEvent(mockEvent)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
                .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(0)
    }

    @Test
    fun `should not reconcile message with mismatched attribute name`() {
        // Arrange input data.
        val inputMessageList = ArrayList<Message>()
        inputMessageList.add(message2)
        val currDate = Date()
        val triggerAttr = TriggerAttribute("name1", currDate.time.toString(), 5, 1)
        val attrList = ArrayList<TriggerAttribute>()
        attrList.add(triggerAttr)
        When calling trigger1.triggerAttributes itReturns attrList
        When calling trigger1.eventType itReturns 4
        When calling trigger1.eventName itReturns "custom"
        customEvent.addAttribute("name2", currDate)
        LocalEventRepository.instance().addEvent(customEvent)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
                .reconcileMessagesAndEvents(inputMessageList)
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
        When calling trigger1.triggerAttributes itReturns attrList
        When calling trigger1.eventType itReturns 4
        When calling trigger1.eventName itReturns "custom"
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
        When calling trigger1.triggerAttributes itReturns attrList
        When calling trigger1.eventType itReturns 4
        When calling trigger1.eventName itReturns "custom"
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
        When calling trigger1.triggerAttributes itReturns attrList
        When calling trigger1.eventType itReturns 4
        When calling trigger1.eventName itReturns "custom"
        When calling message2.getMaxImpressions() itReturns null
        customEvent.addAttribute("name", currDate)
        LocalEventRepository.instance().addEvent(customEvent)
        // Act.
        val outputMessageList = MessageEventReconciliationUtil.instance()
                .reconcileMessagesAndEvents(inputMessageList)
        // Re-assert the output with qualifying event.
        outputMessageList.shouldHaveSize(0)
    }
}
