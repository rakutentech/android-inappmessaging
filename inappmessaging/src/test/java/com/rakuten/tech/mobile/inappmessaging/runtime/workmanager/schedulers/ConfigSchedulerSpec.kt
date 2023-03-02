package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

/**
 * Testing class of ConfigScheduler.
 */
@RunWith(RobolectricTestRunner::class)
class ConfigSchedulerSpec : BaseTest() {

    private val mockWorkManager = Mockito.mock(WorkManager::class.java)

    @Before
    override fun setup() {
        super.setup()
        `when`(
            mockWorkManager.beginUniqueWork(
                any(), any(),
                ArgumentMatchers.any(OneTimeWorkRequest::class.java),
            ),
        ).thenThrow(IllegalStateException("test"))
    }

    @Test
    fun `should not throw exception`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id",
        )
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
        InAppMessaging.errorCallback = mockCallback

        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        ConfigScheduler.instance().startConfig(0, mockWorkManager)

        val captor = argumentCaptor<InAppMessagingException>()
        Mockito.verify(mockCallback).invoke(captor.capture())
        captor.firstValue shouldBeInstanceOf InAppMessagingException::class.java
    }

    @Test
    fun `should be called once`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id",
        )
        val mockConfigScheduler = Mockito.mock(ConfigScheduler::class.java)

        InAppMessaging.initialize(
            ApplicationProvider.getApplicationContext(),
            configScheduler = mockConfigScheduler,
        )

        Mockito.verify(mockConfigScheduler).startConfig()
    }

    @Test
    fun `should reset delay when max is encountered`() {
        InAppMessaging.configure(ApplicationProvider.getApplicationContext()).shouldBeTrue()
        ConfigScheduler.currDelay *= 2
        ConfigScheduler.instance().startConfig(Long.MAX_VALUE - System.currentTimeMillis() + 2)
        ConfigScheduler.currDelay shouldBeEqualTo RetryDelayUtil.INITIAL_BACKOFF_DELAY
    }
}
