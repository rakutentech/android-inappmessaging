package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.*
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler
import org.amshove.kluent.mock
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldHaveSize
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

/**
 * Test class for LocalEventRepository.
 */
class LocalEventRepositorySpec : BaseTest() {

    @Test
    fun `should throw exception when event has empty event name`() {
        LocalEventRepository.instance().clearEvents()
        try {
            LocalEventRepository.instance().addEvent(CustomEvent(""))
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            e.localizedMessage shouldEqual InAppMessagingConstants.EVENT_NAME_EMPTY_EXCEPTION
        }
    }

    @Test
    fun `should have correct event name`() {
        LocalEventRepository.instance().clearEvents()
        val event = CustomEvent("TEST")
        event.addAttribute("doubleAttr", 1.0)
        event.addAttribute("stringAttr", "1.0")
        LocalEventRepository.instance().addEvent(event)
        LocalEventRepository.instance().getEvents()[0].getEventName() shouldEqual "test"
    }

    @Test
    fun `should be called once`() {
        val mockRepo = Mockito.mock(LocalEventRepository::class.java)
        val mockSched = Mockito.mock(EventMessageReconciliationScheduler::class.java)

        val mockEvent = Mockito.mock(Event::class.java)

        EventsManager.onEventReceived(mockEvent, localEventRepo = mockRepo, eventScheduler = mockSched)

        Mockito.verify(mockRepo, Mockito.times(1)).addEvent(mockEvent)
    }

    @Test
    fun `should contain correct number of event when adding all unique`() {
        LocalEventRepository.instance().clearEvents()
        LocalEventRepository.instance().addEvent(AppStartEvent())
        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().addEvent(PurchaseSuccessfulEvent())
        LocalEventRepository.instance().addEvent(CustomEvent("test"))
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)
    }

    @Test
    fun `should contain correct number of event when adding multiple non-persistent types`() {
        LocalEventRepository.instance().clearEvents()
        LocalEventRepository.instance().addEvent(AppStartEvent())
        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().addEvent(PurchaseSuccessfulEvent())
        LocalEventRepository.instance().addEvent(PurchaseSuccessfulEvent())
        LocalEventRepository.instance().addEvent(CustomEvent("test"))
        LocalEventRepository.instance().addEvent(CustomEvent("test2"))
        LocalEventRepository.instance().getEvents().shouldHaveSize(7)
    }

    @Test
    fun `should contain correct number of event when adding multiple persistent types`() {
        LocalEventRepository.instance().clearEvents()
        LocalEventRepository.instance().addEvent(AppStartEvent())
        LocalEventRepository.instance().addEvent(AppStartEvent())
        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().addEvent(PurchaseSuccessfulEvent())
        LocalEventRepository.instance().addEvent(CustomEvent("test"))
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)
    }
}
