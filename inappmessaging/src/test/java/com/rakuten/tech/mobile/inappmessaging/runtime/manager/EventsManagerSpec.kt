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
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalEventRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
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

    private val mockEvent = Mockito.mock(Event::class.java)
    @Mock
    private val mockEventBroadcaster = Mockito.mock(LegacyEventBroadcasterHelper::class.java)
    private val configResponseData = Mockito.mock(ConfigResponseData::class.java)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        When calling mockEvent.getEventName() itReturns EVENT_NAME
        When calling mockEvent.getRatEventMap() itReturns map
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
        When calling configResponseData.enabled itReturns true
        When calling mockEvent.getEventType() itReturns EventType.LOGIN_SUCCESSFUL.typeId
        EventsManager.onEventReceived(mockEvent)
        WorkManager.getInstance(context).getWorkInfosByTag(MESSAGES_EVENTS_WORKER_NAME).get().shouldHaveSize(1)
    }

    companion object {
        private const val MESSAGES_EVENTS_WORKER_NAME = "iam_messages_events_worker"
        private const val EVENT_NAME = "event1"
        private val context = ApplicationProvider.getApplicationContext<Context>()
        private val map: Map<String, Any> = HashMap()
    }
}
