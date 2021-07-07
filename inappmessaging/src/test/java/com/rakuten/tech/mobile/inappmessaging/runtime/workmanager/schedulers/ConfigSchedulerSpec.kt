package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InApp
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testing class of ConfigScheduler.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ConfigSchedulerSpec : BaseTest() {

    private val mockWorkManager = Mockito.mock(WorkManager::class.java)

    @Before
    override fun setup() {
        super.setup()
        When calling mockWorkManager.beginUniqueWork(any(), any(),
                any(OneTimeWorkRequest::class)) itThrows IllegalStateException("test")
    }

    @Test
    fun `should not throw exception`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        ConfigScheduler.instance().startConfig()
    }

    @Test
    fun `should not throw exception when workmanager is not initialized`() {
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        ConfigScheduler.instance().startConfig(0, mockWorkManager)
    }

    @Test
    fun `should not throw exception when workmanager is not initialized with callback`() {
        val function: (ex: Exception) -> Unit = {}
        val mockCallback = Mockito.mock(function.javaClass)
        InApp.errorCallback = mockCallback

        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        ConfigScheduler.instance().startConfig(0, mockWorkManager)

        Mockito.verify(mockCallback).invoke(any(InAppMessagingException::class))
    }

    @Test
    fun `should be called once`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        val mockConfigScheduler = Mockito.mock(ConfigScheduler::class.java)

        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), isForTesting = true,
                configScheduler = mockConfigScheduler)

        Mockito.verify(mockConfigScheduler).startConfig()
    }

    @Test
    fun `should reset delay when max is encountered`() {
        InAppMessaging.init(ApplicationProvider.getApplicationContext()).shouldBeTrue()
        ConfigScheduler.currDelay *= 2
        ConfigScheduler.instance().startConfig(Long.MAX_VALUE - System.currentTimeMillis() + 2)
        ConfigScheduler.currDelay shouldBeEqualTo RetryDelayUtil.INITIAL_BACKOFF_DELAY
    }
}
