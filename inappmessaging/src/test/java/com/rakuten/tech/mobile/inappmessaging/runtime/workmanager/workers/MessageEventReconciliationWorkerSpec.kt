package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.PingResponseMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.MessageEventReconciliationUtil
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test for MessageEventReconciliationWorker class.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class MessageEventReconciliationWorkerSpec : BaseTest() {

    private val workerParameters = Mockito.mock(WorkerParameters::class.java)
    private var worker: MessageEventReconciliationWorker? = null
    private val mockPingResponseRepo = Mockito.mock(PingResponseMessageRepository::class.java)

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
        val message = ValidTestMessage()
        val notTestMessage = ValidTestMessage(isTest = false)
        worker = MessageEventReconciliationWorker(
            ApplicationProvider.getApplicationContext(), workerParameters,
            mockPingResponseRepo, MessageEventReconciliationUtil.instance()
        )
        `when`(mockPingResponseRepo.getAllMessagesCopy()).thenReturn(listOf(message, notTestMessage))
        worker?.doWork() shouldBeEqualTo ListenableWorker.Result.success()
    }
}
