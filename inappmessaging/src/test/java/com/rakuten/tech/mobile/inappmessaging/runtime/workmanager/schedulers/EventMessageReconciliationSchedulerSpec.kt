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
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalEventRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.ExecutionException

/**
 * Test class for EventMessageReconciliationScheduler.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class EventMessageReconciliationSchedulerSpec : BaseTest() {

    private val mockWorkManager = Mockito.mock(WorkManager::class.java)

    @Before
    override fun setup() {
        super.setup()
        When calling mockWorkManager.beginUniqueWork(any(), any(),
                any(OneTimeWorkRequest::class)) itThrows IllegalStateException("test")
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun `should start reconciliation worker`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        EventMessageReconciliationScheduler.instance().startEventMessageReconciliationWorker()
        WorkManager.getInstance(ApplicationProvider.getApplicationContext())
                .getWorkInfosByTag(MESSAGES_EVENTS_WORKER_NAME).get()[0].shouldNotBeNull()
    }

    @Test
    fun `should not crash when workmanager is not initialized`() {
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        EventMessageReconciliationScheduler.instance().startEventMessageReconciliationWorker(mockWorkManager)
    }

    @Test
    fun `should not crash when context is null`() {
        EventMessageReconciliationScheduler.instance().startEventMessageReconciliationWorker()
    }

    @Test
    fun `should not crash when workmanager is not initialized with callback`() {
        val function: (ex: Exception) -> Unit = {}
        val mockCallback = Mockito.mock(function.javaClass)
        InApp.errorCallback = mockCallback

        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        EventMessageReconciliationScheduler.instance().startEventMessageReconciliationWorker(mockWorkManager)

        Mockito.verify(mockCallback).invoke(any(InAppMessagingException::class))
    }

    @Test
    fun `should be called once`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        val context = ApplicationProvider.getApplicationContext<Context>()
        Settings.Secure.putString(context.contentResolver, Settings.Secure.ANDROID_ID, "test_device_id")
        InAppMessaging.initialize(context, true)
        val mockSched = Mockito.mock(EventMessageReconciliationScheduler::class.java)

        val configResponseData = Mockito.mock(ConfigResponseData::class.java)
        val mockAccount = Mockito.mock(AccountRepository::class.java)

        When calling configResponseData.rollOutPercentage itReturns 100
        When calling mockAccount.updateUserInfo() itReturns false

        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        LocalEventRepository.instance().clearEvents()
        EventsManager.onEventReceived(PurchaseSuccessfulEvent(), eventScheduler = mockSched, accountRepo = mockAccount)

        Mockito.verify(mockSched).startEventMessageReconciliationWorker()
    }

    companion object {
        private const val MESSAGES_EVENTS_WORKER_NAME = "iam_messages_events_worker"
    }
}
