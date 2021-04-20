package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkManagerUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.ConfigWorker
import java.util.concurrent.TimeUnit

/**
 * Scheduling workers to do their work in the background to communicate with IAM config service.
 */
@SuppressWarnings("PMD.UseUtilityClass")
internal interface ConfigScheduler {

    fun startConfig()

    companion object {
        private const val CONFIG_WORKER_NAME = "iam_config_worker"
        private var instance: ConfigScheduler = ConfigSchedulerImpl()

        fun instance() = instance
    }

    private class ConfigSchedulerImpl : ConfigScheduler {
        override fun startConfig() {
            WorkManager.getInstance(InAppMessaging.instance().getHostAppContext()!!)
                    .beginUniqueWork(
                            CONFIG_WORKER_NAME,
                            ExistingWorkPolicy.REPLACE,
                            getConfigWorkRequest()!!)
                    .enqueue()
        }

        /**
         * This method syncs with config service in the background with all necessary input data
         * bundled into Data and passed into Worker.
         */
        private fun getConfigWorkRequest(): OneTimeWorkRequest? =
                // Creating a config work request.
                OneTimeWorkRequest.Builder(ConfigWorker::class.java)
                        .setConstraints(WorkManagerUtil.getNetworkConnectedConstraint())
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL,
                                InAppMessagingConstants.INITIAL_BACKOFF_DELAY, TimeUnit.SECONDS)
                        .build()
    }
}
