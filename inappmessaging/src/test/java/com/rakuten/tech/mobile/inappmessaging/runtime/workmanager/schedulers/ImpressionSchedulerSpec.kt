package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.InApp
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.ImpressionManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for ImpressionScheduler.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ImpressionSchedulerSpec : BaseTest() {

    private val mockWorkManager = Mockito.mock(WorkManager::class.java)

    @Before
    override fun setup() {
        super.setup()
        When calling mockWorkManager.enqueue(any(WorkRequest::class)) itThrows IllegalStateException("test")
    }

    @Test
    fun `should not throw exception with valid impression request`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        setupImpressionScheduler()
        WorkManager.getInstance(ApplicationProvider.getApplicationContext())
                .getWorkInfosByTag(IMPRESSION_WORKER_NAME).get()[0].shouldNotBeNull()
    }

    @Test
    fun `should not throw exception with uninitialized workmanager and callback`() {
        val function: (ex: Exception) -> Unit = {}
        val mockCallback = Mockito.mock(function.javaClass)

        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        InApp.errorCallback = mockCallback
        setupImpressionScheduler(mockWorkManager)

        Mockito.verify(mockCallback).invoke(any(InAppMessagingException::class))
    }

    @Test
    fun `should not throw exception with uninitialized workmanager`() {
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        setupImpressionScheduler(mockWorkManager)
    }

    @Test
    fun `should not throw exception with valid workmanager but null context`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        setupImpressionScheduler()
        WorkManager.getInstance(ApplicationProvider.getApplicationContext())
                .getWorkInfosByTag(IMPRESSION_WORKER_NAME).get().shouldBeEmpty()
    }

    private fun setupImpressionScheduler(mockManager: WorkManager? = null) {
        val impressionTypes = mutableListOf(ImpressionType.CLICK_CONTENT)
        // Assemble ImpressionRequest object.
        val impressionRequest = ImpressionRequest(
                "id",
                true,
                BuildConfig.VERSION_NAME,
                HostAppInfoRepository.instance().getVersion(),
                RuntimeUtil.getUserIdentifiers(),
                ImpressionManager().createImpressionList(impressionTypes))
        ImpressionScheduler().startImpressionWorker(impressionRequest, mockManager)
    }

    companion object {
        private const val IMPRESSION_WORKER_NAME = "iam_impression_work"
    }
}
