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
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.LoginSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.*
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.EventMatchingUtil
import kotlinx.coroutines.Dispatchers
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

    private val testDispatcher = UnconfinedTestDispatcher() // for use on tests that call a coroutinescope

    @Before
    override fun setup() {
        super.setup()
        EventMatchingUtil.instance().clearNonPersistentEvents()
        `when`(mockContext.applicationContext).thenReturn(null)

        Dispatchers.setMain(testDispatcher)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        ConfigResponseRepository.resetInstance()

        Dispatchers.resetMain()
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
        accountRepo: AccountRepository = AccountRepository.instance(),
        sessionManager: SessionManager = SessionManager,
        readinessManager: MessageReadinessManager = MessageReadinessManager.instance(),
        primerManager: PushPrimerTrackerManager = PushPrimerTrackerManager
    ): InAppMessaging {
        `when`(configResponseData.rollOutPercentage).thenReturn(rollout)
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        return InApp(
            context = ApplicationProvider.getApplicationContext(),
            isDebugLogging = false,
            displayManager = displayManager,
            eventsManager = eventsManager,
            accountRepo = accountRepo,
            sessionManager = sessionManager,
            messageReadinessManager = readinessManager,
            primerManager = primerManager
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
        InAppMessaging.instance().getRegisteredActivity() shouldBeEqualTo activity
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        InAppMessaging.instance().getRegisteredActivity().shouldBeNull()
    }

    @SuppressWarnings("SwallowedException")
    @Test
    fun `should not crash logging event for initialized instance`() {
        initializeInstance()

        try {
            InAppMessaging.instance().logEvent(AppStartEvent())
        } catch (e: Exception) { Assert.fail(EXCEPTION_MSG) }
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
    fun `should not log event if config is false`() {
        val instance = initializeMockInstance(0)

        instance.logEvent(AppStartEvent())
        instance.logEvent(AppStartEvent())
        instance.logEvent(PurchaseSuccessfulEvent())
        instance.logEvent(LoginSuccessfulEvent())
        Mockito.verify(eventsManager, never()).onEventReceived(any(), any(), any())
    }

    @Test
    fun `should move temp data to repo`() {
        val instance = initializeMockInstance(0)
        instance.registerPreference(TestUserInfoProvider())

        instance.logEvent(AppStartEvent())
        instance.logEvent(AppStartEvent())
        instance.logEvent(PurchaseSuccessfulEvent())
        instance.logEvent(LoginSuccessfulEvent())
        Mockito.verify(eventsManager, never()).onEventReceived(any(), any(), any())
        (instance as InApp).tempEventList.shouldHaveSize(4)

        instance.flushEventList()
        instance.tempEventList.shouldBeEmpty()
    }

    @Test
    fun `should log event if config is true`() {
        val instance = initializeMockInstance(100)
        instance.registerPreference(TestUserInfoProvider())

        instance.logEvent(AppStartEvent())
        Mockito.verify(eventsManager).onEventReceived(any(), any(), any())
    }

    @Test
    fun `should enable caching`() {
        initializeInstance(true)

        InAppMessaging.instance().isLocalCachingEnabled().shouldBeTrue()
    }

    @Test
    fun `should call onSessionUpdate on user change`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(ctx)
        Settings.Secure.putString(ctx.contentResolver, Settings.Secure.ANDROID_ID, "test_device_id")

        val accMock = Mockito.mock(AccountRepository::class.java)
        val sessMock = Mockito.mock(SessionManager::class.java)
        val instance = initializeMockInstance(100, accountRepo = accMock, sessionManager = sessMock)
        val infoProvider = TestUserInfoProvider() // test_user_id
        instance.registerPreference(infoProvider)

        // Simulate change user
        infoProvider.userId = "test_user_id_2"
        `when`(accMock.updateUserInfo()).thenReturn(true)
        (instance as InApp).userDidChange()

        // Should call onSessionUpdate
        verify(sessMock).onSessionUpdate()
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
        HostAppInfoRepository.instance().isTooltipEnabled().shouldBeFalse()
    }

    @Test
    fun `should disable tooltip feature by default when set to null`() {
        InAppMessaging.configure(context, enableTooltip = null)
        HostAppInfoRepository.instance().isTooltipEnabled().shouldBeFalse()
    }

    @Test
    fun `should disable tooltip feature`() {
        InAppMessaging.configure(context, enableTooltip = false)
        HostAppInfoRepository.instance().isTooltipEnabled().shouldBeFalse()
    }

    @Test
    fun `should enable tooltip feature`() {
        InAppMessaging.configure(context, enableTooltip = true)
        HostAppInfoRepository.instance().isTooltipEnabled().shouldBeTrue()
    }

    @Test
    fun `should set updated tooltip setting when re-configured`() {
        InAppMessaging.configure(context, enableTooltip = false)
        HostAppInfoRepository.instance().isTooltipEnabled().shouldBeFalse()

        InAppMessaging.configure(context, enableTooltip = true)
        HostAppInfoRepository.instance().isTooltipEnabled().shouldBeTrue()
    }
}

class InAppMessagingExceptionSpec : InAppMessagingSpec() {

    private val mockActivity = Mockito.mock(Activity::class.java)
    private val dispMgr = Mockito.mock(DisplayManager::class.java)
    private val instance = initializeMockInstance(100, dispMgr)

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
    fun `should not crash when register preference failed due to forced exception`() {
        val mockProvider = Mockito.mock(UserInfoProvider::class.java)
        `when`(mockProvider.provideUserId()).thenThrow(NullPointerException())

        instance.registerPreference(mockProvider)
    }

    @Test
    fun `should trigger callback when register preference failed due to forced exception`() {
        InAppMessaging.errorCallback = mockCallback
        val mockProvider = Mockito.mock(UserInfoProvider::class.java)
        `when`(mockProvider.provideUserId()).thenThrow(NullPointerException())

        instance.registerPreference(mockProvider)

        Mockito.verify(mockCallback).invoke(captor.capture())
        captor.firstValue shouldBeInstanceOf InAppMessagingException::class.java
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

    @Test
    fun `should not crash when log event failed due to forced exception`() {
        instance.logEvent(AppStartEvent())
    }

    @Test
    fun `should trigger callback when log event failed due to forced exception`() {
        InAppMessaging.errorCallback = mockCallback
        instance.logEvent(AppStartEvent())

        Mockito.verify(mockCallback).invoke(captor.capture())
        captor.firstValue shouldBeInstanceOf InAppMessagingException::class.java
    }

    @Test
    fun `should not crash when save temp data failed due to forced exception`() {
        instance.flushEventList()
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
                ValidTestMessage(
                    campaignId = "1",
                    type = InAppMessageType.TOOLTIP.typeId,
                    tooltip = Tooltip("ui-element", "top-center", "testurl")
                )
            ),
            0
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
        InAppMessaging.instance().flushEventList()
        InAppMessaging.instance().onPushPrimer.shouldBeNull()
    }

    @Test
    fun `should return null activity using uninitialized instance`() {
        InAppMessaging.setNotConfiguredInstance()
        InAppMessaging.instance().getRegisteredActivity().shouldBeNull()
    }

    @Test
    fun `should return null context using uninitialized instance`() {
        InAppMessaging.setNotConfiguredInstance()
        InAppMessaging.instance().getHostAppContext() shouldBeEqualTo null
    }
}

class InAppMessagingRemoveSpec : InAppMessagingSpec() {
    @Test
    fun `should remove message but not clear repo when activity is unregistered`() {
        val message = ValidTestMessage("1")
        setupDisplayedView(message)
        initializeInstance()

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        Mockito.verify(parentViewGroup).removeView(viewGroup)
        CampaignRepository.instance().messages.shouldHaveSize(2)
    }

    @Test
    fun `should not crash when unregister activity without displayed message`() {
        val message = ValidTestMessage("1")
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
        val message = ValidTestMessage("1")
        setupDisplayedView(message)
        initializeInstance()

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        (InAppMessaging.instance() as InApp).removeMessage(false)
        verifyMaxImpression()
    }

    @Test
    fun `should remove message from host activity and clear queue`() {
        val message = ValidTestMessage("1")
        setupDisplayedView(message)
        initializeInstance()

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        (InAppMessaging.instance() as InApp).removeMessage(true)
        verifyMaxImpression()
    }

    @Test
    fun `should remove tooltip when UIElement is valid`() {
        val message = ValidTestMessage(
            campaignId = "1",
            type = InAppMessageType.TOOLTIP.typeId,
            tooltip = Tooltip("ui-element", "top-center", "testurl")
        )
        setupDisplayedView(message, true)
        val mockMgr = Mockito.mock(MessageReadinessManager::class.java)
        val instance = initializeMockInstance(100, readinessManager = mockMgr)

        instance.registerMessageDisplayActivity(activity)
        (instance as InApp).removeMessage("ui-element")
        Mockito.verify(displayManager).removeMessage(anyOrNull(), any(), any(), anyOrNull())
        Mockito.verify(mockMgr).removeMessageFromQueue(message.getCampaignId())
        // atLeastOnce due to registerMessageDisplayActivity
        Mockito.verify(displayManager, atLeastOnce()).displayMessage()
    }

    @Test
    fun `should not process tooltip removal when UIElement is invalid`() {
        val message = ValidTestMessage(
            campaignId = "1",
            type = InAppMessageType.TOOLTIP.typeId,
            tooltip = Tooltip("invalid-element", "top-center", "testurl")
        )
        setupDisplayedView(message, true)
        val mockMgr = Mockito.mock(MessageReadinessManager::class.java)
        val instance = initializeMockInstance(100, readinessManager = mockMgr)

        instance.registerMessageDisplayActivity(activity)
        (instance as InApp).removeMessage("ui-element")
        Mockito.verify(displayManager, never()).removeMessage(anyOrNull(), any(), any(), anyOrNull())
        Mockito.verify(mockMgr, never()).removeMessageFromQueue(message.getCampaignId())
        // atLeastOnce due to registerMessageDisplayActivity
        Mockito.verify(displayManager, atLeastOnce()).displayMessage()
    }

    @Test
    fun `should call display manager when removing campaign but not clear queue`() {
        val message = ValidTestMessage("1")
        setupDisplayedView(message)
        val mockMgr = Mockito.mock(MessageReadinessManager::class.java)
        val instance = initializeMockInstance(100, readinessManager = mockMgr)

        `when`(displayManager.removeMessage(anyOrNull(), any(), any(), anyOrNull())).thenReturn("1")

        (instance as InApp).removeMessage(false)
        CampaignRepository.instance().messages.values.forEach {
            // Impressions left should not be reduced
            it.impressionsLeft shouldBeEqualTo it.getMaxImpressions()
        }
        Mockito.verify(mockMgr).removeMessageFromQueue(message.getCampaignId())
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
        val message2 = ValidTestMessage()
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
            it.impressionsLeft shouldBeEqualTo it.getMaxImpressions()
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
            arrayOf(Manifest.permission.POST_NOTIFICATIONS), intArrayOf(PackageManager.PERMISSION_GRANTED)
        )

        verify(mockMgr).sendPrimerEvent(eq(1), any())
    }

    @Test
    fun `should call primer manager with denied result`() {
        val inApp = initializeMockInstance(primerManager = mockMgr)

        inApp.trackPushPrimer(
            arrayOf(Manifest.permission.POST_NOTIFICATIONS), intArrayOf(PackageManager.PERMISSION_DENIED)
        )

        verify(mockMgr).sendPrimerEvent(eq(0), any())
    }

    @Test
    fun `should not call primer manager other permission`() {
        val inApp = initializeMockInstance(primerManager = mockMgr)

        inApp.trackPushPrimer(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), intArrayOf(PackageManager.PERMISSION_DENIED)
        )

        verify(mockMgr, never()).sendPrimerEvent(any(), any())
    }

    @Test
    fun `should not call primer manager other permission and result higher size`() {
        val inApp = initializeMockInstance(primerManager = mockMgr)

        inApp.trackPushPrimer(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            intArrayOf(PackageManager.PERMISSION_DENIED, PackageManager.PERMISSION_GRANTED)
        )

        verify(mockMgr, never()).sendPrimerEvent(any(), any())
    }

    @Test
    fun `should not call primer manager other permission and permission higher size`() {
        val inApp = initializeMockInstance(primerManager = mockMgr)

        inApp.trackPushPrimer(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            intArrayOf(PackageManager.PERMISSION_DENIED)
        )

        verify(mockMgr, never()).sendPrimerEvent(any(), any())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `should not call primer manager if lower than tiramisu`() {
        val inApp = initializeMockInstance(primerManager = mockMgr)

        inApp.trackPushPrimer(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            intArrayOf(PackageManager.PERMISSION_DENIED, PackageManager.PERMISSION_GRANTED)
        )

        verify(mockMgr, never()).sendPrimerEvent(any(), any())
    }
}
