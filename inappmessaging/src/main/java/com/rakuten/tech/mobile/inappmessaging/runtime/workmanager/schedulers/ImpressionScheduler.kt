package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppError
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppErrorLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.BackendApi
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkManagerUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.ImpressionWorker
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.ImpressionWorker.Companion.IMPRESSION_REQUEST_KEY

/**
 * Report Impression Scheduler, which reporting campaign impressions to IAM backend.
 */
internal class ImpressionScheduler {

    /**
     * This method starts to report a campaign impression to IAM backend.
     */
    fun startImpressionWorker(impressionRequest: ImpressionRequest, workManager: WorkManager? = null) {
        // Enqueue unique work request in the background.
        try {
            val context = HostAppInfoRepository.instance().getContext()
            context?.let { ctx ->
                val manager = workManager ?: WorkManager.getInstance(ctx)
                manager.enqueue(createWorkRequest(impressionRequest))
            }
        } catch (ie: IllegalStateException) {
            // this should not occur since work manager is initialized during SDK initialization
            "In-App Messaging impression request failed".let {
                InAppErrorLogger.logError(
                    "ImpressionScheduler",
                    InAppError(
                        ex = InAppMessagingException(it, ie),
                        ev = Event.OperationFailed(BackendApi.IMPRESSION.alias),
                    ),
                )
            }
        }
    }

    /**
     * This method creates an impression work request.
     */
    private fun createWorkRequest(impressionRequest: ImpressionRequest): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(ImpressionWorker::class.java)
            .setInputData(getInputData(impressionRequest))
            .addTag(IMPRESSION_WORKER_NAME)
            .setConstraints(WorkManagerUtil.getNetworkConnectedConstraint())
            .build()

    /**
     * This method retrieves a data object which contains the necessary data required by ReportImpressionWorker.
     */
    private fun getInputData(impressionRequest: ImpressionRequest): Data {
        // Convert ImpressionRequest object into a Json String before setting it as input data.
        val impressionRequestJsonString = Gson().toJson(impressionRequest)
        // Create input data objects.
        return Data.Builder()
            .putString(IMPRESSION_REQUEST_KEY, impressionRequestJsonString)
            .build()
    }

    companion object {
        private const val IMPRESSION_WORKER_NAME = "iam_impression_work"
    }
}
