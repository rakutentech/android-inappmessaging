package com.rakuten.tech.mobile.inappmessaging.runtime

import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.fail

/**
 * Test class for InAppMessaging.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class InAppMessagingSpec : BaseTest() {
    private val activity = Mockito.mock(Activity::class.java)
    private val configResponseData = Mockito.mock(ConfigResponseData::class.java)
    private val displayManager = Mockito.mock(DisplayManager::class.java)
    private val viewGroup = Mockito.mock(ViewGroup::class.java)
    private val parentViewGroup = Mockito.mock(ViewGroup::class.java)

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
        When calling configResponseData.enabled itReturns true
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        Mockito.verify(displayManager, Mockito.times(0)).displayMessage()

        ConfigResponseRepository.resetInstance()
    }

    @Test
    fun `should not crash when using uninitialized instance`() {
        InAppMessaging.setUninitializedInstance()
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        InAppMessaging.instance().logEvent(AppStartEvent())
        InAppMessaging.instance().closeMessage()
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
        When calling configResponseData.enabled itReturns true
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)

        val inApp = InApp(ApplicationProvider.getApplicationContext(), false, displayManager)
        inApp.registerMessageDisplayActivity(activity)
        Mockito.verify(displayManager, Mockito.times(1)).displayMessage()

        ConfigResponseRepository.resetInstance()
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
    @Suppress("SwallowedException")
    fun `should not crash logging event for initialized instance`() {
        initializeInstance()

        try {
            InAppMessaging.instance().logEvent(AppStartEvent())
        } catch (e: Exception) {
            fail("should not throw exception")
        }
    }

    @Test
    @Suppress("SwallowedException")
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
    fun `should remove message from host activity`() {
        When calling activity.findViewById<ViewGroup>(R.id.in_app_message_base_view) itReturns viewGroup
        When calling viewGroup.parent itReturns parentViewGroup

        initializeInstance()

        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        InAppMessaging.instance().closeMessage()
        Mockito.verify(parentViewGroup).removeView(viewGroup)
    }

    private fun initializeInstance() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test", "",
                isDebugLogging = true, isForTesting = true)
    }
}
