package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.EventMatchingUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.MessageEventReconciliationUtil
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

/**
 * Test for MessageEventReconciliationWorker class.
 */
@RunWith(RobolectricTestRunner::class)
class MessageEventReconciliationWorkerSpec : BaseTest() {

    private val workerParameters = Mockito.mock(WorkerParameters::class.java)
    private var worker: MessageEventReconciliationWorker? = null

    @Before
    override fun setup() {
        super.setup()
        `when`(workerParameters!!.inputData).thenReturn(Data.EMPTY)
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        worker = MessageEventReconciliationWorker(context, workerParameters)
    }

    @Test
    fun `should do work return success`() {
        worker?.doWork() shouldBeEqualTo ListenableWorker.Result.success()
    }

    @Test
    fun `should return success with valid messages`() {
        val triggers = listOf(Trigger(0, EventType.APP_START.typeId, "testEvent2", mutableListOf()))
        val message = TestDataHelper.createDummyMessage(campaignId = "1", isTest = true, triggers = triggers)
        val notTestMessage = TestDataHelper.createDummyMessage(campaignId = "2", triggers = triggers)
        worker = MessageEventReconciliationWorker(
            ApplicationProvider.getApplicationContext(),
            workerParameters,
            EventMatchingUtil.instance(),
            MessageEventReconciliationUtil(
                campaignRepo = CampaignRepository.instance(),
                eventMatchingUtil = EventMatchingUtil.instance(),
            ),
            MessageReadinessManager.instance(),
        )
        CampaignRepository.instance().syncWith(listOf(message, notTestMessage), 0)
        EventMatchingUtil.instance().matchAndStore(AppStartEvent())
        worker?.doWork() shouldBeEqualTo ListenableWorker.Result.success()
    }
}
