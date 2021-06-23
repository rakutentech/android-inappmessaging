package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.*
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

/**
 * Test class for LocalEventRepository.
 */
@RunWith(RobolectricTestRunner::class)
class LocalEventRepositorySpec : BaseTest() {

    @Before
    fun setup() {
        LocalEventRepository.instance().clearEvents()
    }

    @Test
    fun `should throw exception when event has empty event name`() {
        try {
            LocalEventRepository.instance().addEvent(CustomEvent(""))
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            e.localizedMessage shouldBeEqualTo InAppMessagingConstants.EVENT_NAME_EMPTY_EXCEPTION
        }
    }

    @Test
    fun `should have correct event name`() {
        val event = CustomEvent("TEST")
        event.addAttribute("doubleAttr", 1.0)
        event.addAttribute("stringAttr", "1.0")
        LocalEventRepository.instance().addEvent(event)
        LocalEventRepository.instance().getEvents()[0].getEventName() shouldBeEqualTo "test"
    }

    @Test
    fun `should be called once`() {
        val mockRepo = Mockito.mock(LocalEventRepository::class.java)
        val mockSched = Mockito.mock(EventMessageReconciliationScheduler::class.java)

        val mockEvent = Mockito.mock(Event::class.java)

        EventsManager.onEventReceived(mockEvent, localEventRepo = mockRepo, eventScheduler = mockSched)

        Mockito.verify(mockRepo).addEvent(mockEvent)
    }

    @Test
    fun `should contain correct number of event when adding all unique`() {
        LocalEventRepository.instance().addEvent(AppStartEvent())
        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().addEvent(PurchaseSuccessfulEvent())
        LocalEventRepository.instance().addEvent(CustomEvent("test"))
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)
    }

    @Test
    fun `should contain correct number of event when adding multiple non-persistent types`() {
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
        initializeLocalEvent()
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)
    }

    @Test
    fun `should return zero after clearing with no persistent type`() {
        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().addEvent(PurchaseSuccessfulEvent())
        LocalEventRepository.instance().addEvent(CustomEvent("test"))
        LocalEventRepository.instance().getEvents().shouldHaveSize(3)

        LocalEventRepository.instance().clearNonPersistentEvents()
        LocalEventRepository.instance().getEvents().shouldHaveSize(0)
    }

    @Test
    fun `should return zero after clearing with one persistent type`() {
        LocalEventRepository.instance().addEvent(AppStartEvent())
        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().addEvent(PurchaseSuccessfulEvent())
        LocalEventRepository.instance().addEvent(CustomEvent("test"))
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)

        LocalEventRepository.instance().clearNonPersistentEvents()
        LocalEventRepository.instance().getEvents().shouldHaveSize(1)
    }

    @Test
    fun `should return valid value after clearing then adding`() {
        initializeLocalEvent()
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)

        LocalEventRepository.instance().clearNonPersistentEvents()
        LocalEventRepository.instance().getEvents().shouldHaveSize(1)

        LocalEventRepository.instance().addEvent(AppStartEvent())
        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().addEvent(PurchaseSuccessfulEvent())
        LocalEventRepository.instance().addEvent(CustomEvent("test"))
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)
    }

    @Test
    fun `should not throw exception when clearing with empty events`() {
        LocalEventRepository.instance().clearNonPersistentEvents()
        LocalEventRepository.instance().getEvents().shouldHaveSize(0)
    }

    @Test
    fun `should save and restore values for different users`() {
        setupAndTestMultipleUser()
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)
    }

    @Test
    fun `should not crash and clear previous when forced cast exception`() {
        setupAndTestMultipleUser()
        val editor = InAppMessaging.instance().getSharedPref()?.edit()
        editor?.putInt(LocalEventRepository.LOCAL_EVENT_KEY, 1)?.apply()

        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().getEvents().shouldHaveSize(2) // including persistent type
    }

    @Test
    fun `should not crash and clear previous when parsing invalid json`() {
        setupAndTestMultipleUser()
        val editor = InAppMessaging.instance().getSharedPref()?.edit()
        editor?.putString(LocalEventRepository.LOCAL_EVENT_KEY, "[{eventType:\"invalid\"}]")?.apply()

        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().getEvents().shouldHaveSize(1) // clear all due to invalid data
    }

    private fun setupAndTestMultipleUser() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)

        initializeLocalEvent()
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)

        infoProvider.rakutenId = "user2"
        AccountRepository.instance().updateUserInfo()
        LocalEventRepository.instance().getEvents().shouldHaveSize(1) // persistent type is retained

        // revert to initial user info
        infoProvider.rakutenId = TestUserInfoProvider.TEST_RAKUTEN_ID
        AccountRepository.instance().updateUserInfo()
    }

    private fun initializeInstance(infoProvider: UserInfoProvider) {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(),
                isForTesting = true, isCacheHandling = true)
        InAppMessaging.instance().registerPreference(infoProvider)
    }

    private fun initializeLocalEvent() {
        LocalEventRepository.instance().addEvent(AppStartEvent())
        LocalEventRepository.instance().addEvent(AppStartEvent())
        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().addEvent(PurchaseSuccessfulEvent())
        LocalEventRepository.instance().addEvent(CustomEvent("test"))
    }
}
