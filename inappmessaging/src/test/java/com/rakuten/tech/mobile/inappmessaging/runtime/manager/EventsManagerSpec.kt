package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.LegacyEventBroadcasterHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.ExecutionException

/**
 * Test class for EventsManager.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class EventsManagerSpec : BaseTest() {

    private val message = ValidTestMessage()
    @Mock
    private val mockEvent = Mockito.mock(Event::class.java)
    private val mockEventBroadcaster = Mockito.mock(LegacyEventBroadcasterHelper::class.java)
    private val configResponseData = Mockito.mock(ConfigResponseData::class.java)
    private val mockAccount = Mockito.mock(AccountRepository::class.java)
    private val eventRecon = Mockito.mock(EventMessageReconciliationScheduler::class.java)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        When calling mockEvent.getEventName() itReturns EVENT_NAME
        When calling mockEvent.getRatEventMap() itReturns map
        LocalDisplayedMessageRepository.instance().clearMessages()
        LocalEventRepository.instance().clearEvents()
        System.out.println(LocalEventRepository.instance().getEvents().size)
        ReadyForDisplayMessageRepository.instance().clearMessages()
        LocalOptedOutMessageRepository.instance().clearMessages()
        PingResponseMessageRepository.instance().clearMessages()
    }

    @Test
    fun `should receive event`() {
        LocalEventRepository.instance().clearEvents()
        EventsManager.onEventReceived(mockEvent)
        LocalEventRepository.instance().getEvents()[0].getEventName() shouldEqual EVENT_NAME
    }

    @Test
    fun `should invoke broadcast receiver`() {
        EventsManager.onEventReceived(mockEvent, mockEventBroadcaster::sendEvent)
        Mockito.verify(mockEventBroadcaster).sendEvent(
                InAppMessagingConstants.RAT_EVENT_KEY_EVENTS,
                mockEvent.getRatEventMap())
    }

    @Test
    fun `should check config is enabled`() {
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        When calling configResponseData.enabled itReturns false
        EventsManager.onEventReceived(mockEvent)
        Mockito.verify(configResponseData).enabled
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun `should not reconcile when config is disabled`() {
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test", "",
                isDebugLogging = false, isForTesting = true)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        When calling configResponseData.enabled itReturns false
        When calling mockEvent.getEventType() itReturns EventType.LOGIN_SUCCESSFUL.typeId
        EventsManager.onEventReceived(mockEvent)
        WorkManager.getInstance(context).getWorkInfosByTag(MESSAGES_EVENTS_WORKER_NAME).get().shouldHaveSize(0)
    }

    @Test
    fun `should clear all messages when user info is updated`() {
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test", "",
                isDebugLogging = false, isForTesting = true)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        addTestData()

        When calling configResponseData.enabled itReturns false
        When calling mockAccount.updateUserInfo() itReturns true

        EventsManager.onEventReceived(event = PurchaseSuccessfulEvent(), eventScheduler = eventRecon,
                accountRepo = mockAccount)

        verifyTestData(0)
    }

    @Test
    fun `should not clear messages when user info is not updated`() {
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test", "",
                isDebugLogging = false, isForTesting = true)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        When calling configResponseData.enabled itReturns true
        When calling mockAccount.updateUserInfo() itReturns false

        addTestData()

        EventsManager.onEventReceived(event = PurchaseSuccessfulEvent(), eventScheduler = eventRecon,
                accountRepo = mockAccount)

        verifyTestData(1)
    }

    private fun addTestData() {
        // Add messages
        val messageList = ArrayList<Message>()
        messageList.add(message)
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        LocalDisplayedMessageRepository.instance().addMessage(message)
        LocalOptedOutMessageRepository.instance().addMessage(message)
    }

    private fun verifyTestData(expected: Int) {
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(expected)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy()shouldHaveSize(expected)
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldEqual expected
        if (expected > 0) {
            LocalOptedOutMessageRepository.instance().hasMessage(message.getCampaignId()).shouldBeTrue()
        } else {
            LocalOptedOutMessageRepository.instance().hasMessage(message.getCampaignId()).shouldBeFalse()
        }
        LocalEventRepository.instance().getEvents().shouldHaveSize(1)

        Mockito.verify(eventRecon, Mockito.times(expected)).startEventMessageReconciliationWorker()
    }

    companion object {
        private const val MESSAGES_EVENTS_WORKER_NAME = "iam_messages_events_worker"
        private const val EVENT_NAME = "event1"
        private val context = ApplicationProvider.getApplicationContext<Context>()
        private val map: Map<String, Any> = HashMap()
    }
}
