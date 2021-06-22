package com.rakuten.tech.mobile.inappmessaging.runtime

import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.never
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.LoginSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import org.amshove.kluent.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import kotlin.test.fail

/**
 * Test class for InAppMessaging.
 */
@RunWith(RobolectricTestRunner::class)
@SuppressWarnings("LargeClass")
class InAppMessagingSpec : BaseTest() {
    private val activity = Mockito.mock(Activity::class.java)
    private val configResponseData = Mockito.mock(ConfigResponseData::class.java)
    private val displayManager = Mockito.mock(DisplayManager::class.java)
    private val eventsManager = Mockito.mock(EventsManager::class.java)
    private val viewGroup = Mockito.mock(ViewGroup::class.java)
    private val parentViewGroup = Mockito.mock(ViewGroup::class.java)
    private val mockContext = Mockito.mock(Context::class.java)

    @Before
    fun setup() {
        LocalEventRepository.instance().clearEvents()
        When calling mockContext.applicationContext itReturns null
    }

    @After
    fun tearDown() {
        ConfigResponseRepository.resetInstance()
    }

    @Test
    fun `should unregister activity not crash when no activity is registered`() {
        InAppMessaging.instance().unregisterMessageDisplayActivity()
    }

    @Test
    fun `should unregister activity not crash when no activity is registered for uninitialized instance`() {
        InAppMessaging.setUninitializedInstance()
        InAppMessaging.instance().unregisterMessageDisplayActivity()
    }

    @Test
    fun `should not display message if config is true for uninitialized instance`() {
        InAppMessaging.setUninitializedInstance()
        When calling configResponseData.rollOutPercentage itReturns 100
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        Mockito.verify(displayManager, never()).displayMessage()
    }

    @Test
    fun `should not crash when using uninitialized instance`() {
        InAppMessaging.setUninitializedInstance()
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        InAppMessaging.instance().logEvent(AppStartEvent())
        InAppMessaging.instance().closeMessage()
        InAppMessaging.instance().isLocalCachingEnabled().shouldBeFalse()
        InAppMessaging.instance().getSharedPref().shouldBeNull()
        InAppMessaging.instance().saveTempData()
    }

    @Test
    fun `should return null activity using uninitialized instance`() {
        InAppMessaging.setUninitializedInstance()
        InAppMessaging.instance().getRegisteredActivity().shouldBeNull()
    }

    @Test
    fun `should return null context using uninitialized instance`() {
        InAppMessaging.setUninitializedInstance()
        InAppMessaging.instance().getHostAppContext() shouldBeEqualTo null
    }

    @Test
    // For code coverage. will be deleted when updateSession() is removed
    fun `should not crash update session when using uninitialized instance`() {
        InAppMessaging.setUninitializedInstance()
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        InAppMessaging.instance().updateSession()
    }

    @Test
    fun `should display message using initialized instance`() {
        val inApp = initializeMockInstance(100)
        inApp.registerMessageDisplayActivity(activity)
        Mockito.verify(displayManager).displayMessage()
    }

    @Test
    fun `should clear registered activity for initialized instance`() {
        initializeInstance()

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        InAppMessaging.instance().getRegisteredActivity() shouldBeEqualTo activity
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        InAppMessaging.instance().getRegisteredActivity().shouldBeNull()
    }

    @Test
    @SuppressWarnings("SwallowedException")
    fun `should not crash logging event for initialized instance`() {
        initializeInstance()

        try {
            InAppMessaging.instance().logEvent(AppStartEvent())
        } catch (e: Exception) {
            fail("should not throw exception")
        }
    }

    @Test
    @SuppressWarnings("SwallowedException")
    // For code coverage. will be deleted when updateSession() is removed
    fun `should not crash update session for initialized instance`() {
        initializeInstance()

        try {
            InAppMessaging.instance().updateSession()
        } catch (e: Exception) {
            fail("should not throw exception")
        }
    }

    @Test
    @SuppressWarnings("SwallowedException")
    fun `should not crash close message for initialized instance`() {
        initializeInstance()

        try {
            InAppMessaging.instance().closeMessage()
            InAppMessaging.instance().closeMessage(true)
        } catch (e: Exception) {
            fail("should not throw exception")
        }
    }

    @Test
    fun `should remove message from host activity and not clear queue`() {
        val message = ValidTestMessage("1")
        setupDisplayedView(message)
        initializeInstance()

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        (InAppMessaging.instance() as InApp).removeMessage(false)
        Mockito.verify(parentViewGroup).removeView(viewGroup)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(1)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            if (msg.getCampaignId() == "1") {
                msg.getNumberOfTimesClosed() shouldBeEqualTo 1
            } else {
                msg.getNumberOfTimesClosed() shouldBeEqualTo 0
            }
        }
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 0
    }

    @Test
    fun `should remove message from host activity and clear queue`() {
        val message = ValidTestMessage("1")
        setupDisplayedView(message)
        initializeInstance()

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        (InAppMessaging.instance() as InApp).removeMessage(true)
        Mockito.verify(parentViewGroup).removeView(viewGroup)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldBeEmpty()
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 1
        }
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 0
    }

    @Test
    fun `should not increment when no message is displayed`() {
        val message = ValidTestMessage("1")
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(listOf(message))
        PingResponseMessageRepository.instance().replaceAllMessages(listOf(message))
        initializeInstance()

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        (InAppMessaging.instance() as InApp).removeMessage(false)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(1)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
        }
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 0
    }

    @Test
    fun `should clear and increment when no message is displayed but flag true`() {
        val message = ValidTestMessage("1")
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(listOf(message))
        PingResponseMessageRepository.instance().replaceAllMessages(listOf(message))
        initializeInstance()

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        (InAppMessaging.instance() as InApp).removeMessage(true)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldBeEmpty()
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 1
        }
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 0
    }

    @Test
    fun `should remove message but not clear repo when activity is unregistered`() {
        val message = ValidTestMessage("1")
        setupDisplayedView(message)
        initializeInstance()

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        Mockito.verify(parentViewGroup).removeView(viewGroup)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 0
    }

    @Test
    fun `should not crash when unregister activity without displayed message`() {
        val message = ValidTestMessage("1")
        setupDisplayedView(message)
        initializeInstance()

        When calling activity.findViewById<ViewGroup>(R.id.in_app_message_base_view) itReturns null

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        Mockito.verify(parentViewGroup, never()).removeView(viewGroup)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 0
    }

    @Test
    fun `should not call display message if config is false`() {
        val instance = initializeMockInstance(0)

        instance.registerMessageDisplayActivity(activity)
        Mockito.verify(displayManager, never()).displayMessage()
    }

    @Test
    fun `should not call remove message if config is false`() {
        val instance = initializeMockInstance(0)

        instance.unregisterMessageDisplayActivity()
        Mockito.verify(displayManager, never()).removeMessage(any())
    }

    @Test
    fun `should not log event if config is false`() {
        val instance = initializeMockInstance(0)

        instance.logEvent(AppStartEvent())
        instance.logEvent(AppStartEvent())
        instance.logEvent(PurchaseSuccessfulEvent())
        instance.logEvent(LoginSuccessfulEvent())
        Mockito.verify(eventsManager, never()).onEventReceived(any(), any(), any(), any(), any())
        LocalEventRepository.instance().getEvents().shouldHaveSize(0)
        (instance as InApp).tempEventList.shouldHaveSize(4)
    }

    @Test
    fun `should move temp data to repo`() {
        val instance = initializeMockInstance(0)

        instance.logEvent(AppStartEvent())
        instance.logEvent(AppStartEvent())
        instance.logEvent(PurchaseSuccessfulEvent())
        instance.logEvent(LoginSuccessfulEvent())
        Mockito.verify(eventsManager, never()).onEventReceived(any(), any(), any(), any(), any())
        LocalEventRepository.instance().getEvents().shouldHaveSize(0)
        (instance as InApp).tempEventList.shouldHaveSize(4)

        instance.saveTempData()
        LocalEventRepository.instance().getEvents().shouldHaveSize(3) // app start is only logged once
        instance.tempEventList.shouldBeEmpty()
    }

    @Test
    fun `should log event if config is true`() {
        val instance = initializeMockInstance(100)

        instance.logEvent(AppStartEvent())
        Mockito.verify(eventsManager).onEventReceived(any(), any(), any(), any(), any())
    }

    @Test
    fun `should enable caching`() {
        initializeInstance(true)

        InAppMessaging.instance().isLocalCachingEnabled().shouldBeTrue()
    }

    @Test
    fun `should return true when initialization have no issues`() {
        InAppMessaging.init(ApplicationProvider.getApplicationContext()).shouldBeTrue()
    }

    @Test
    fun `should return true when initialization have no issues with callback`() {
        InAppMessaging.init(ApplicationProvider.getApplicationContext()) {
            Assert.fail()
        }.shouldBeTrue()
    }

    @Test
    fun `should return false when initialization failed`() {
        InAppMessaging.init(mockContext).shouldBeFalse()
    }

    @Test
    fun `should return false when initialization failed with callback`() {
        val function: (ex: Exception) -> Unit = {}
        val mockCallback = Mockito.mock(function.javaClass)
        InAppMessaging.init(mockContext, mockCallback).shouldBeFalse()

        Mockito.verify(mockCallback).invoke(any())
    }

    private fun setupDisplayedView(message: Message) {
        val message2 = ValidTestMessage()
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(listOf(message, message2))
        PingResponseMessageRepository.instance().replaceAllMessages(listOf(message, message2))
        LocalDisplayedMessageRepository.instance().clearMessages()
        When calling activity.findViewById<ViewGroup>(R.id.in_app_message_base_view) itReturns viewGroup
        When calling viewGroup.parent itReturns parentViewGroup
        When calling viewGroup.tag itReturns "1"
    }

    private fun initializeInstance(shouldEnableCaching: Boolean = false) {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        When calling configResponseData.rollOutPercentage itReturns 100
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true, shouldEnableCaching)
    }

    private fun initializeMockInstance(rollout: Int): InAppMessaging {
        When calling configResponseData.rollOutPercentage itReturns rollout
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        return InApp(ApplicationProvider.getApplicationContext(), false, displayManager,
                eventsManager = eventsManager)
    }
}
