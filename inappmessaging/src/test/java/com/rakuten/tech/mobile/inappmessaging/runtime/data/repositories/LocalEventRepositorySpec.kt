package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.times
import com.rakuten.tech.mobile.inappmessaging.runtime.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Attribute
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.*
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.SharedPreferencesUtil.getPreferencesFile
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldHaveSize
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.util.Calendar

/**
 * Test class for LocalEventRepository.
 */
@RunWith(RobolectricTestRunner::class)
@SuppressWarnings("LargeClass")
open class LocalEventRepositorySpec : BaseTest() {
    private val function: (ex: Exception) -> Unit = {}
    internal val mockCallback = Mockito.mock(function.javaClass)

    @Before
    override fun setup() {
        super.setup()
        LocalEventRepository.instance().clearEvents()
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
        val mockScheduler = Mockito.mock(EventMessageReconciliationScheduler::class.java)

        val mockEvent = Mockito.mock(Event::class.java)

        EventsManager.onEventReceived(
            mockEvent,
            localEventRepo = mockRepo,
            eventScheduler = mockScheduler
        )

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
    fun `should remove all events triggered before a given time`() {
        initializeLocalEvent()
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)
        val timeMillis = LocalEventRepository.instance().getEvents()[3].getTimestamp() + 1
        LocalEventRepository.instance().clearNonPersistentEvents(timeMillis)
        LocalEventRepository.instance().getEvents().shouldHaveSize(1)
    }

    @Test
    fun `should keep all events triggered after a given time`() {
        initializeLocalEvent()
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)
        val timeMillis = LocalEventRepository.instance().getEvents()[0].getTimestamp() - 1
        LocalEventRepository.instance().clearNonPersistentEvents(timeMillis)
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)
    }

    @Test
    fun `should not throw exception when clearing with empty events for a given time`() {
        val timeMillis = Calendar.getInstance().timeInMillis
        LocalEventRepository.instance().clearNonPersistentEvents(timeMillis)
        LocalEventRepository.instance().getEvents().shouldHaveSize(0)
    }

    @Test
    fun `should not throw exception when caching updating list for a given time`() {
        InAppMessaging.setUninitializedInstance(true)
        val timeMillis = Calendar.getInstance().timeInMillis
        LocalEventRepository.instance().clearNonPersistentEvents(timeMillis)
        LocalEventRepository.instance().getEvents().shouldHaveSize(0)
    }

    @Test
    fun `should not throw exception when clearing with empty events`() {
        LocalEventRepository.instance().clearNonPersistentEvents()
        LocalEventRepository.instance().getEvents().shouldHaveSize(0)
    }

    @Test
    fun `should not crash when forced cast exception`() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)
        PreferencesUtil.putInt(
            ApplicationProvider.getApplicationContext(),
            getPreferencesFile(),
            LocalEventRepository.LOCAL_EVENT_KEY,
            1
        )
        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().getEvents().shouldHaveSize(1)
    }

    @Test
    fun `should check and reset list during event handling`() {
        setupAndTestMultipleUser()
        InAppMessaging.setUninitializedInstance(true)
        LocalEventRepository.instance().getEvents().shouldHaveSize(1)
    }

    @Test
    fun `should save and restore values for different users`() {
        setupAndTestMultipleUser()
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)
    }

    internal fun setupAndTestMultipleUser() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)

        initializeLocalEvent()
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)

        infoProvider.userId = "user2"
        AccountRepository.instance().updateUserInfo()
        LocalEventRepository.instance().getEvents().shouldHaveSize(1) // persistent type is retained

        // revert to initial user info
        infoProvider.userId = TestUserInfoProvider.TEST_USER_ID
        AccountRepository.instance().updateUserInfo()
    }

    @Test
    fun `should remove all events triggered before a given time with false user updated flag`() {
        initializeLocalEvent()
        val event = LoginSuccessfulEvent()
        event.setShouldNotClear(true)
        val timeMillis = event.getTimestamp() + 1
        LocalEventRepository.instance().addEvent(event)
        LocalEventRepository.instance().getEvents().shouldHaveSize(5)
        LocalEventRepository.instance().clearNonPersistentEvents(timeMillis)
        LocalEventRepository.instance().getEvents().shouldHaveSize(2)
    }

    @Test
    fun `should keep all events triggered after a given time with true user updated flag`() {
        val event = LoginSuccessfulEvent()
        event.setShouldNotClear(true)
        val timeMillis = event.getTimestamp() - 1
        LocalEventRepository.instance().addEvent(event)
        LocalEventRepository.instance().addEvent(event)
        LocalEventRepository.instance().addEvent(event)
        LocalEventRepository.instance().addEvent(event)
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)
        LocalEventRepository.instance().clearNonPersistentEvents(timeMillis)
        LocalEventRepository.instance().getEvents().shouldHaveSize(4)
    }

    private fun initializeInstance(infoProvider: UserInfoProvider) {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), isCacheHandling = true)
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

class LocalEventRepositoryExceptionSpec : LocalEventRepositorySpec() {
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
    fun `should not crash and clear previous when parsing invalid format`() {
        setupAndTestMultipleUser()
        PreferencesUtil.putString(
            ApplicationProvider.getApplicationContext(),
            getPreferencesFile(),
            LocalEventRepository.LOCAL_EVENT_KEY,
            "[{eve"
        )

        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().getEvents().shouldHaveSize(1) // clear all due to invalid data
    }

    @Test
    fun `should throw exception with empty campaign id`() {
        InApp.errorCallback = mockCallback
        LocalEventRepository.instance().addEvent(TestEvent("")).shouldBeFalse()

        val captor = argumentCaptor<InAppMessagingException>()
        Mockito.verify(mockCallback, times(1)).invoke(captor.capture())
        captor.firstValue shouldBeInstanceOf InAppMessagingException::class.java
    }

    @Test
    fun `should not crash and clear previous when forced cast exception`() {
        setupAndTestMultipleUser()
        PreferencesUtil.putInt(
            ApplicationProvider.getApplicationContext(),
            getPreferencesFile(),
            LocalEventRepository.LOCAL_EVENT_KEY,
            1
        )

        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().getEvents().shouldHaveSize(2) // including persistent type
    }

    @Test
    fun `should not crash and clear previous when parsing invalid type`() {
        setupAndTestMultipleUser()
        PreferencesUtil.putString(
            ApplicationProvider.getApplicationContext(),
            getPreferencesFile(),
            LocalEventRepository.LOCAL_EVENT_KEY,
            "[{eventType:\"invalid\"}]"
        )

        LocalEventRepository.instance().addEvent(LoginSuccessfulEvent())
        LocalEventRepository.instance().getEvents().shouldHaveSize(1) // clear all due to invalid data
    }
}

@SuppressWarnings("EmptyFunctionBlock")
internal class TestEvent(private val name: String) : Event {
    override fun getEventName(): String = name
    override fun getEventType() = 0
    override fun getTimestamp(): Long = 0
    override fun isPersistentType(): Boolean = false
    override fun getRatEventMap(): Map<String, Any> = mapOf()
    override fun getAttributeMap(): Map<String, Attribute?> = mapOf()
    override fun shouldNotClear() = false
    override fun setShouldNotClear(shouldNotClear: Boolean) {}
}
