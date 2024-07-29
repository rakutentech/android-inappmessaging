package com.rakuten.tech.mobile.inappmessaging.runtime

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.*
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TooltipHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.EventMatchingUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.amshove.kluent.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for InAppMessaging.
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@SuppressWarnings("LargeClass")
@Ignore("base class")
open class InAppMessagingSpec : BaseTest() {
    internal val activity = Mockito.mock(Activity::class.java)
    internal val configResponseData = Mockito.mock(ConfigResponseData::class.java)
    internal val displayManager = Mockito.mock(DisplayManager::class.java)
    internal val eventsManager = Mockito.mock(EventsManager::class.java)
    internal val viewGroup = Mockito.mock(ViewGroup::class.java)
    internal val parentViewGroup = Mockito.mock(ViewGroup::class.java)
    internal val mockContext = Mockito.mock(Context::class.java)

    private val function: (ex: Exception) -> Unit = {}
    internal val mockCallback = Mockito.mock(function.javaClass)
    internal val captor = argumentCaptor<InAppMessagingException>()

    @Before
    override fun setup() {
        super.setup()
        EventMatchingUtil.instance().clearNonPersistentEvents()
        `when`(mockContext.applicationContext).thenReturn(null)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        ConfigResponseRepository.resetInstance()
    }

    internal fun initializeInstance(shouldEnableCaching: Boolean = false) {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(ctx)
        Settings.Secure.putString(ctx.contentResolver, Settings.Secure.ANDROID_ID, "test_device_id")
        `when`(configResponseData.rollOutPercentage).thenReturn(100)
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), shouldEnableCaching)
    }

    internal fun initializeMockInstance(
        rollout: Int = 100,
        displayManager: DisplayManager = this.displayManager,
        eventsManager: EventsManager = EventsManager,
        eventMatchingUtil: EventMatchingUtil = EventMatchingUtil.instance(),
        accountRepo: AccountRepository = AccountRepository.instance(),
        campaignRepo: CampaignRepository = CampaignRepository.instance(),
        configRepo: ConfigResponseRepository = ConfigResponseRepository.instance(),
        sessionManager: SessionManager = SessionManager,
        readinessManager: MessageReadinessManager = MessageReadinessManager.instance(),
        primerManager: PushPrimerTrackerManager = PushPrimerTrackerManager,
    ): InAppMessaging {
        `when`(configResponseData.rollOutPercentage).thenReturn(rollout)
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        return InApp(
            isDebugLogging = false,
            displayManager = displayManager,
            eventsManager = eventsManager,
            eventMatchingUtil = eventMatchingUtil,
            accountRepo = accountRepo,
            campaignRepo = campaignRepo,
            configRepo = configRepo,
            sessionManager = sessionManager,
            messageReadinessManager = readinessManager,
            primerManager = primerManager,
        )
    }
}

class InAppMessagingBasicSpec : InAppMessagingSpec() {
    @Test
    fun `should unregister activity not crash when no activity is registered`() {
        InAppMessaging.instance().unregisterMessageDisplayActivity()
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
        HostAppInfoRepository.instance().getRegisteredActivity() shouldBeEqualTo activity
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        HostAppInfoRepository.instance().getRegisteredActivity().shouldBeNull()
    }

    @SuppressWarnings("SwallowedException")
    @Test
    fun `should not crash close message for initialized instance`() {
        initializeInstance()

        try {
            InAppMessaging.instance().closeMessage()
            InAppMessaging.instance().closeMessage(true)
        } catch (e: Exception) { Assert.fail(EXCEPTION_MSG) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should not crash close tooltip for initialized instance`() = runTest {
        initializeInstance()

        InAppMessaging.instance().closeTooltip("ui-element")
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
        Mockito.verify(displayManager, never()).removeMessage(any(), any(), any(), any())
    }

    @Test
    fun `should enable caching`() {
        initializeInstance(true)

        InAppMessaging.instance().isLocalCachingEnabled().shouldBeTrue()
    }

    @Test
    fun `should not clear persistent campaigns list when changing user`() {
        EventMatchingUtil.instance().matchedEvents.clear()
        EventMatchingUtil.instance().matchedEvents["app-start-campaign"] = mutableListOf(AppStartEvent())
        EventMatchingUtil.instance().matchedEvents["dummy-campaign"] =
            mutableListOf(AppStartEvent(), LoginSuccessfulEvent())

        EventMatchingUtil.instance().triggeredPersistentCampaigns.clear()
        EventMatchingUtil.instance().triggeredPersistentCampaigns.add("app-start-campaign")

        // Simulate change user
        SessionManager.onSessionUpdate()

        EventMatchingUtil.instance().matchedEvents.shouldBeEmpty() // cleared
        EventMatchingUtil.instance().triggeredPersistentCampaigns.shouldHaveSize(1) // not cleared
    }

    companion object {
        private const val EXCEPTION_MSG = "should not throw exception"
    }
}

class InAppMessagingConfigureSpec : InAppMessagingSpec() {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `should return true when initialization have no issues`() {
        InAppMessaging.configure(ApplicationProvider.getApplicationContext()).shouldBeTrue()
    }

    @Test
    fun `should return true when initialization have no issues with callback`() {
        InAppMessaging.errorCallback = {
            Assert.fail()
        }
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(ctx)
        InAppMessaging.configure(ctx).shouldBeTrue()
        InAppMessaging.errorCallback = null
    }

    @Test
    fun `should return false when initialization failed`() {
        InAppMessaging.configure(mockContext).shouldBeFalse()
    }

    @Test
    fun `should return false when initialization failed with callback`() {
        InAppMessaging.errorCallback = mockCallback
        InAppMessaging.configure(mockContext).shouldBeFalse()

        Mockito.verify(mockCallback).invoke(any())
        InAppMessaging.errorCallback = null
    }

    @Test
    fun `should use subscription key from AndroidManifest by default`() {
        InAppMessaging.configure(context)
        HostAppInfoRepository.instance().getSubscriptionKey() shouldBeEqualTo
            InApp.AppManifestConfig(context).subscriptionKey()
    }

    @Test
    fun `should use config Url from AndroidManifest by default`() {
        InAppMessaging.configure(context)
        HostAppInfoRepository.instance().getConfigUrl() shouldBeEqualTo InApp.AppManifestConfig(context).configUrl()
    }

    @Test
    fun `should use subscription key from AndroidManifest when configured to null`() {
        InAppMessaging.configure(context, subscriptionKey = null)
        HostAppInfoRepository.instance().getSubscriptionKey() shouldBeEqualTo
            InApp.AppManifestConfig(context).subscriptionKey()
    }

    @Test
    fun `should use config Url from AndroidManifest when configured to null`() {
        InAppMessaging.configure(context, configUrl = null)
        HostAppInfoRepository.instance().getConfigUrl() shouldBeEqualTo InApp.AppManifestConfig(context).configUrl()
    }

    @Test
    fun `should use subscription key from AndroidManifest when configured to empty after trim`() {
        InAppMessaging.configure(context, subscriptionKey = "  ")
        HostAppInfoRepository.instance().getSubscriptionKey() shouldBeEqualTo
            InApp.AppManifestConfig(context).subscriptionKey()
    }

    @Test
    fun `should use config Url from AndroidManifest when configured to empty after trim`() {
        InAppMessaging.configure(context, configUrl = " ")
        HostAppInfoRepository.instance().getConfigUrl() shouldBeEqualTo InApp.AppManifestConfig(context).configUrl()
    }

    @Test
    fun `should use the updated subscription key when re-configured`() {
        InAppMessaging.configure(context)
        HostAppInfoRepository.instance().getSubscriptionKey() shouldBeEqualTo
            InApp.AppManifestConfig(context).subscriptionKey()

        val newSubsKey = "abcd-efgh-ijkl"
        InAppMessaging.configure(context, subscriptionKey = newSubsKey)
        HostAppInfoRepository.instance().getSubscriptionKey() shouldBeEqualTo newSubsKey
    }

    @Test
    fun `should use the updated config Url when re-configured`() {
        InAppMessaging.configure(context)
        HostAppInfoRepository.instance().getConfigUrl() shouldBeEqualTo InApp.AppManifestConfig(context).configUrl()

        val newConfigUrl = "https://test-config"
        InAppMessaging.configure(context, configUrl = newConfigUrl)
        HostAppInfoRepository.instance().getConfigUrl() shouldBeEqualTo newConfigUrl
    }

    @Test
    fun `should use trimmed subscription key`() {
        val newSubsKey = "    abcd-efgh-ijkl        "
        InAppMessaging.configure(context, subscriptionKey = newSubsKey)
        HostAppInfoRepository.instance().getSubscriptionKey() shouldBeEqualTo newSubsKey.trim()
    }

    @Test
    fun `should use trimmed config url`() {
        val newConfigUrl = "   https://test-config "
        InAppMessaging.configure(context, configUrl = newConfigUrl)
        HostAppInfoRepository.instance().getConfigUrl() shouldBeEqualTo newConfigUrl.trim()
    }

    @Test
    fun `should disable tooltip feature by default when not set`() {
        InAppMessaging.configure(context)
        HostAppInfoRepository.instance().isTooltipFeatureEnabled().shouldBeFalse()
    }

    @Test
    fun `should disable tooltip feature by default when set to null`() {
        InAppMessaging.configure(context, enableTooltipFeature = null)
        HostAppInfoRepository.instance().isTooltipFeatureEnabled().shouldBeFalse()
    }

    @Test
    fun `should disable tooltip feature`() {
        InAppMessaging.configure(context, enableTooltipFeature = false)
        HostAppInfoRepository.instance().isTooltipFeatureEnabled().shouldBeFalse()
    }

    @Test
    fun `should enable tooltip feature`() {
        InAppMessaging.configure(context, enableTooltipFeature = true)
        HostAppInfoRepository.instance().isTooltipFeatureEnabled().shouldBeTrue()
    }

    @Test
    fun `should set updated tooltip setting when re-configured`() {
        InAppMessaging.configure(context, enableTooltipFeature = false)
        HostAppInfoRepository.instance().isTooltipFeatureEnabled().shouldBeFalse()

        InAppMessaging.configure(context, enableTooltipFeature = true)
        HostAppInfoRepository.instance().isTooltipFeatureEnabled().shouldBeTrue()
    }
}

class InAppMessagingLogEventSpec : InAppMessagingSpec() {
    private val mockConfigRepo = Mockito.mock(ConfigResponseRepository::class.java)
    private val mockEventUtil = Mockito.mock(EventMatchingUtil::class.java)
    private val mockAcctRepo = Mockito.mock(AccountRepository::class.java)
    private val mockCampaignRepo = Mockito.mock(CampaignRepository::class.java)
    private val mockSessionManager = Mockito.mock(SessionManager::class.java)
    private val mockEventsManager = Mockito.mock(EventsManager::class.java)

    private val instance = initializeMockInstance(
        rollout = 100,
        configRepo = mockConfigRepo,
        eventMatchingUtil = mockEventUtil,
        accountRepo = mockAcctRepo,
        campaignRepo = mockCampaignRepo,
        sessionManager = mockSessionManager,
        eventsManager = mockEventsManager,
    )

    @Test
    fun `logEvent should not process event yet when config is not enabled`() {
        `when`(mockConfigRepo.isConfigEnabled()).thenReturn(false)

        val event = PurchaseSuccessfulEvent()
        instance.logEvent(event)

        verify(mockEventUtil).addToEventBuffer(event)
    }

    @Test
    fun `logEvent should not process event yet when user changed`() {
        `when`(mockConfigRepo.isConfigEnabled()).thenReturn(true)
        `when`(mockAcctRepo.updateUserInfo()).thenReturn(true)

        val event = PurchaseSuccessfulEvent()
        instance.logEvent(event)

        verify(mockEventUtil).addToEventBuffer(event)
        verify(mockSessionManager).onSessionUpdate()
    }

    @Test
    fun `logEvent should not process event yet when campaigns are not synced`() {
        `when`(mockConfigRepo.isConfigEnabled()).thenReturn(true)
        `when`(mockAcctRepo.updateUserInfo()).thenReturn(false)
        `when`(mockCampaignRepo.lastSyncMillis).thenReturn(null)

        val event = PurchaseSuccessfulEvent()
        instance.logEvent(event)

        verify(mockEventUtil).addToEventBuffer(event)
    }

    @Test
    fun `logEvent should process event`() {
        `when`(mockConfigRepo.isConfigEnabled()).thenReturn(true)
        `when`(mockAcctRepo.updateUserInfo()).thenReturn(false)
        `when`(mockCampaignRepo.lastSyncMillis).thenReturn(0)
        `when`(mockEventUtil.eventBuffer).thenReturn(arrayListOf())

        val event = PurchaseSuccessfulEvent()
        instance.logEvent(event)

        verify(mockEventUtil, never()).addToEventBuffer(event)
        verify(mockEventsManager).onEventReceived(event)
    }

    @Test
    fun `logEvent should not crash due to forced exception`() {
        `when`(mockConfigRepo.isConfigEnabled()).thenThrow(NullPointerException())

        instance.logEvent(PurchaseSuccessfulEvent())
    }

    @Test
    fun `logEvent should trigger callback due to forced exception`() {
        `when`(mockConfigRepo.isConfigEnabled()).thenThrow(NullPointerException())
        InAppMessaging.errorCallback = mockCallback

        instance.logEvent(PurchaseSuccessfulEvent())

        Mockito.verify(mockCallback).invoke(captor.capture())
        captor.firstValue shouldBeInstanceOf InAppMessagingException::class.java
    }
}

class InAppMessagingExceptionSpec : InAppMessagingSpec() {

    private val mockActivity = Mockito.mock(Activity::class.java)
    private val dispMgr = Mockito.mock(DisplayManager::class.java)
    private val mockAcctRepo = Mockito.mock(AccountRepository::class.java)
    private val instance = initializeMockInstance(100, dispMgr, accountRepo = mockAcctRepo)

    @Before
    override fun setup() {
        super.setup()
        InAppMessaging.errorCallback = null
        `when`(dispMgr.displayMessage()).thenThrow(NullPointerException())
        `when`(dispMgr.removeMessage(anyOrNull(), anyOrNull(), any(), anyOrNull())).thenThrow(NullPointerException())
        `when`(eventsManager.onEventReceived(any(), any(), any())).thenThrow(NullPointerException())
    }

    @After
    override fun tearDown() {
        super.tearDown()
        InAppMessaging.errorCallback = null
    }

    @Test
    fun `should not crash when register activity failed due to forced exception`() {
        instance.registerMessageDisplayActivity(mockActivity)
    }

    @Test
    fun `should trigger callback when register activity failed due to forced exception`() {
        InAppMessaging.errorCallback = mockCallback
        instance.registerMessageDisplayActivity(mockActivity)

        Mockito.verify(mockCallback).invoke(captor.capture())
        captor.firstValue shouldBeInstanceOf InAppMessagingException::class.java
    }

    @Test
    fun `should not crash when unregister activity failed due to forced exception`() {
        instance.registerMessageDisplayActivity(mockActivity)
        instance.unregisterMessageDisplayActivity()
    }

    @Test
    fun `should trigger callback when unregister activity failed due to forced exception`() {
        InAppMessaging.errorCallback = mockCallback
        instance.unregisterMessageDisplayActivity()

        Mockito.verify(mockCallback).invoke(captor.capture())
        captor.firstValue shouldBeInstanceOf InAppMessagingException::class.java
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should not crash when closing message due to forced exception`() = runTest {
        InAppMessaging.errorCallback = mockCallback

        instance.closeMessage(false)

        Mockito.verify(mockCallback).invoke(captor.capture())
        captor.firstValue shouldBeInstanceOf InAppMessagingException::class.java
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should not crash when closing tooltip due to forced exception`() = runTest {
        InAppMessaging.errorCallback = mockCallback
        CampaignRepository.instance().syncWith(
            listOf(
                TooltipHelper.createMessage(
                    target = "ui-element",
                    position = "top-center",
                ),
            ),
            0,
        )

        instance.closeTooltip("ui-element")

        Mockito.verify(mockCallback).invoke(captor.capture())
        captor.firstValue shouldBeInstanceOf InAppMessagingException::class.java
    }
}

class InAppMessagingUnInitSpec : InAppMessagingSpec() {
    @Test
    fun `should unregister activity not crash when no activity is registered for uninitialized instance`() {
        InAppMessaging.setNotConfiguredInstance()
        InAppMessaging.instance().unregisterMessageDisplayActivity()
    }

    @Test
    fun `should not display message if config is true for uninitialized instance`() {
        InAppMessaging.setNotConfiguredInstance()
        `when`(configResponseData.rollOutPercentage).thenReturn(100)
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        Mockito.verify(displayManager, never()).displayMessage()
    }

    @Test
    fun `should not crash when using uninitialized instance`() {
        InAppMessaging.setNotConfiguredInstance()
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        InAppMessaging.instance().logEvent(AppStartEvent())
        InAppMessaging.instance().closeMessage()
        InAppMessaging.instance().closeTooltip("ui-element")
        InAppMessaging.instance().isLocalCachingEnabled().shouldBeFalse()
        InAppMessaging.instance().trackPushPrimer(arrayOf(""), intArrayOf(1))
        InAppMessaging.instance().onPushPrimer.shouldBeNull()
    }
}

class InAppMessagingRemoveSpec : InAppMessagingSpec() {
    @Test
    fun `should remove message but not clear repo when activity is unregistered`() {
        val message = TestDataHelper.createDummyMessage(campaignId = "1")
        setupDisplayedView(message)
        initializeInstance()

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        Mockito.verify(parentViewGroup).removeView(viewGroup)
        CampaignRepository.instance().messages.shouldHaveSize(2)
    }

    @Test
    fun `should not crash when unregister activity without displayed message`() {
        val message = TestDataHelper.createDummyMessage(campaignId = "1")
        setupDisplayedView(message)
        initializeInstance()

        `when`(activity.findViewById<ViewGroup>(R.id.in_app_message_base_view)).thenReturn(null)

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        Mockito.verify(parentViewGroup, never()).removeView(viewGroup)
        CampaignRepository.instance().messages.shouldHaveSize(2)
    }

    @Test
    fun `should remove message from host activity and not clear queue`() {
        val message = TestDataHelper.createDummyMessage(campaignId = "1")
        setupDisplayedView(message)
        initializeInstance()

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        (InAppMessaging.instance() as InApp).removeMessage(false)
        verifyMaxImpression()
    }

    @Test
    fun `should remove message from host activity and clear queue`() {
        val message = TestDataHelper.createDummyMessage(campaignId = "1")
        setupDisplayedView(message)
        initializeInstance()

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        (InAppMessaging.instance() as InApp).removeMessage(true)
        verifyMaxImpression()
    }

    @Test
    fun `should remove tooltip when UIElement is valid`() {
        val message = TooltipHelper.createMessage(
            target = "ui-element",
            position = "top-center",
        )
        setupDisplayedView(message, true)
        val mockMgr = Mockito.mock(MessageReadinessManager::class.java)
        val instance = initializeMockInstance(100, readinessManager = mockMgr)

        instance.registerMessageDisplayActivity(activity)
        (instance as InApp).removeMessage("ui-element")
        Mockito.verify(displayManager).removeMessage(anyOrNull(), any(), any(), anyOrNull())
        Mockito.verify(mockMgr).removeMessageFromQueue(message.campaignId)
        // atLeastOnce due to registerMessageDisplayActivity
        Mockito.verify(displayManager, atLeastOnce()).displayMessage()
    }

    @Test
    fun `should not process tooltip removal when UIElement is invalid`() {
        val message = TooltipHelper.createMessage(
            target = "invalid-element",
            position = "top-center",
        )
        setupDisplayedView(message, true)
        val mockMgr = Mockito.mock(MessageReadinessManager::class.java)
        val instance = initializeMockInstance(100, readinessManager = mockMgr)

        instance.registerMessageDisplayActivity(activity)
        (instance as InApp).removeMessage("ui-element")
        Mockito.verify(displayManager, never()).removeMessage(anyOrNull(), any(), any(), anyOrNull())
        Mockito.verify(mockMgr, never()).removeMessageFromQueue(message.campaignId)
        // atLeastOnce due to registerMessageDisplayActivity
        Mockito.verify(displayManager, atLeastOnce()).displayMessage()
    }

    @Test
    fun `should call display manager when removing campaign but not clear queue`() {
        val message = TestDataHelper.createDummyMessage(campaignId = "1")
        setupDisplayedView(message)
        val mockMgr = Mockito.mock(MessageReadinessManager::class.java)
        val instance = initializeMockInstance(100, readinessManager = mockMgr)

        `when`(displayManager.removeMessage(anyOrNull(), any(), any(), anyOrNull())).thenReturn("1")

        (instance as InApp).removeMessage(false)
        CampaignRepository.instance().messages.values.forEach {
            // Impressions left should not be reduced
            it.impressionsLeft shouldBeEqualTo it.maxImpressions
        }
        Mockito.verify(mockMgr).removeMessageFromQueue(message.campaignId)
        Mockito.verify(displayManager).displayMessage()
    }

    @Test
    fun `should not call display manager when removing campaign but not clear queue`() {
        val mockMgr = Mockito.mock(MessageReadinessManager::class.java)
        val instance = initializeMockInstance(100, readinessManager = mockMgr)

        `when`(displayManager.removeMessage(anyOrNull(), any(), any(), anyOrNull())).thenReturn(null)

        (instance as InApp).removeMessage(false)
        Mockito.verify(mockMgr, never()).removeMessageFromQueue(any())
        Mockito.verify(displayManager, never()).displayMessage()
    }

    private fun setupDisplayedView(message: Message, isTooltip: Boolean = false) {
        val message2 = TestDataHelper.createDummyMessage()
        CampaignRepository.instance().syncWith(listOf(message, message2), 0)
        if (isTooltip) {
            `when`(activity.findViewById<ViewGroup>(R.id.in_app_message_tooltip_view)).thenReturn(viewGroup)
        } else {
            `when`(activity.findViewById<ViewGroup>(R.id.in_app_message_base_view)).thenReturn(viewGroup)
        }
        `when`(viewGroup.parent).thenReturn(parentViewGroup)
        `when`(viewGroup.tag).thenReturn("1")
    }

    private fun verifyMaxImpression() {
        Mockito.verify(parentViewGroup).removeView(viewGroup)
        CampaignRepository.instance().messages.values.forEach {
            // Impressions left should not be reduced
            it.impressionsLeft shouldBeEqualTo it.maxImpressions
        }
    }
}

@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class InAppMessagingPrimerTrackerSpec : InAppMessagingSpec() {
    private val mockMgr = Mockito.mock(PushPrimerTrackerManager::class.java)

    @Test
    fun `should call primer manager with granted result`() {
        val inApp = initializeMockInstance(primerManager = mockMgr)

        inApp.trackPushPrimer(
            arrayOf(Manifest.permission.POST_NOTIFICATIONS), intArrayOf(PackageManager.PERMISSION_GRANTED),
        )

        verify(mockMgr).sendPrimerEvent(eq(1))
    }

    @Test
    fun `should call primer manager with denied result`() {
        val inApp = initializeMockInstance(primerManager = mockMgr)

        inApp.trackPushPrimer(
            arrayOf(Manifest.permission.POST_NOTIFICATIONS), intArrayOf(PackageManager.PERMISSION_DENIED),
        )

        verify(mockMgr).sendPrimerEvent(eq(0))
    }

    @Test
    fun `should not call primer manager other permission`() {
        val inApp = initializeMockInstance(primerManager = mockMgr)

        inApp.trackPushPrimer(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), intArrayOf(PackageManager.PERMISSION_DENIED),
        )

        verify(mockMgr, never()).sendPrimerEvent(any())
    }

    @Test
    fun `should not call primer manager other permission and result higher size`() {
        val inApp = initializeMockInstance(primerManager = mockMgr)

        inApp.trackPushPrimer(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            intArrayOf(PackageManager.PERMISSION_DENIED, PackageManager.PERMISSION_GRANTED),
        )

        verify(mockMgr, never()).sendPrimerEvent(any())
    }

    @Test
    fun `should not call primer manager other permission and permission higher size`() {
        val inApp = initializeMockInstance(primerManager = mockMgr)

        inApp.trackPushPrimer(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            intArrayOf(PackageManager.PERMISSION_DENIED),
        )

        verify(mockMgr, never()).sendPrimerEvent(any())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `should not call primer manager if lower than tiramisu`() {
        val inApp = initializeMockInstance(primerManager = mockMgr)

        inApp.trackPushPrimer(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            intArrayOf(PackageManager.PERMISSION_DENIED, PackageManager.PERMISSION_GRANTED),
        )

        verify(mockMgr, never()).sendPrimerEvent(any())
    }
}
