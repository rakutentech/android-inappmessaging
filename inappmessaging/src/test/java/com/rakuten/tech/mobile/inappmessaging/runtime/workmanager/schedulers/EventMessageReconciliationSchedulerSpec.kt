package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalEventRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldNotBeNull
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
    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun `should start reconciliation worker`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.init(
                ApplicationProvider.getApplicationContext(),
                "test",
                "",
                isDebugLogging = false,
                isForTesting = true)
        EventMessageReconciliationScheduler.instance().startEventMessageReconciliationWorker()
        WorkManager.getInstance(ApplicationProvider.getApplicationContext())
                .getWorkInfosByTag(MESSAGES_EVENTS_WORKER_NAME).get()[0].shouldNotBeNull()
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun `should start reconciliation worker with message`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.init(
                ApplicationProvider.getApplicationContext(),
                "test",
                "",
                isDebugLogging = false,
                isForTesting = true)
        EventMessageReconciliationScheduler.instance().startEventMessageReconciliationWorker(ValidTestMessage())
        WorkManager.getInstance(ApplicationProvider.getApplicationContext())
                .getWorkInfosByTag(MESSAGES_EVENTS_WORKER_NAME).get()[0].shouldNotBeNull()
    }

    @Test
    fun `should be called once`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        val context = ApplicationProvider.getApplicationContext<Context>()
        Settings.Secure.putString(context.contentResolver, Settings.Secure.ANDROID_ID, "test_device_id")
        InAppMessaging.init(context, "test", "", isDebugLogging = false, isForTesting = true)
        val mockSched = Mockito.mock(EventMessageReconciliationScheduler::class.java)

        val configResponseData = Mockito.mock(ConfigResponseData::class.java)
        val mockAccount = Mockito.mock(AccountRepository::class.java)

        When calling configResponseData.enabled itReturns true
        When calling mockAccount.updateUserInfo() itReturns false

        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        LocalEventRepository.instance().clearEvents()
        EventsManager.onEventReceived(PurchaseSuccessfulEvent(), eventScheduler = mockSched, accountRepo = mockAccount)

        Mockito.verify(mockSched, Mockito.times(1)).startEventMessageReconciliationWorker()
    }

    companion object {
        private const val MESSAGES_EVENTS_WORKER_NAME = "iam_messages_events_worker"
    }
}
