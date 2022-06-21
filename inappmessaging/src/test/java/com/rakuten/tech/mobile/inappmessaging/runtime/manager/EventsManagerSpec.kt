package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.EventTrackerHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.EventMatchingUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.ExecutionException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Test class for EventsManager.
 */
@RunWith(RobolectricTestRunner::class)
class EventsManagerSpec : BaseTest() {

    private val message = ValidTestMessage()
    private val mockEvent = Mockito.mock(Event::class.java)
    private val mockEventBroadcaster = Mockito.mock(EventTrackerHelper::class.java)
    private val configResponseData = Mockito.mock(ConfigResponseData::class.java)
    private val mockAccount = Mockito.mock(AccountRepository::class.java)
    private val eventRecon = Mockito.mock(EventMessageReconciliationScheduler::class.java)

    @Before
    override fun setup() {
        super.setup()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        `when`(mockEvent.getEventName()).thenReturn(EVENT_NAME)
        `when`(mockEvent.getRatEventMap()).thenReturn(map)
        CampaignRepository.instance().clearMessages()
    }

    @Test
    fun `should check config is enabled`() {
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        `when`(configResponseData.rollOutPercentage).thenReturn(0)
        EventsManager.onEventReceived(mockEvent)
        Mockito.verify(configResponseData).rollOutPercentage
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun `should not reconcile when config is disabled`() {
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id"
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        `when`(configResponseData.rollOutPercentage).thenReturn(0)
        `when`(mockEvent.getEventType()).thenReturn(EventType.LOGIN_SUCCESSFUL.typeId)
        EventsManager.onEventReceived(mockEvent)
        WorkManager.getInstance(context).getWorkInfosByTag(MESSAGES_EVENTS_WORKER_NAME).get().shouldHaveSize(0)
    }

    @Test
    fun `should not clear messages when user info is not updated`() {
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id"
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        `when`(configResponseData.rollOutPercentage).thenReturn(100)
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        `when`(mockAccount.updateUserInfo()).thenReturn(false)

        addTestData()

        EventsManager.onEventReceived(
            event = PurchaseSuccessfulEvent(), eventScheduler = eventRecon
        )

        verifyTestData(1)
    }

    private fun addTestData() {
        // Add messages
        val messageList = ArrayList<Message>()
        messageList.add(message)
        CampaignRepository.instance().syncWith(messageList, 0)
    }

    private fun verifyTestData(expected: Int) {
        CampaignRepository.instance().messages.shouldHaveSize(expected)

        if (expected > 0) {
            EventMatchingUtil.instance().matchedEvents(CampaignRepository.instance().messages.values.first()).shouldNotBeNull()
        }

        Mockito.verify(eventRecon, Mockito.times(expected)).startEventMessageReconciliationWorker()
    }

    companion object {
        private const val MESSAGES_EVENTS_WORKER_NAME = "iam_messages_events_worker"
        private const val EVENT_NAME = "event1"
        private val context = ApplicationProvider.getApplicationContext<Context>()
        private val map: Map<String, Any> = HashMap()
    }
}
