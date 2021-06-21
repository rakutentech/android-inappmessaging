package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkManagerUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.ConfigWorker
import java.util.concurrent.TimeUnit

/**
 * Scheduling workers to do their work in the background to communicate with IAM config service.
 */
internal interface ConfigScheduler {

    fun startConfig(delay: Long = 0)

    companion object {
        private const val CONFIG_WORKER_NAME = "iam_config_worker"
        private var instance: ConfigScheduler = ConfigSchedulerImpl()
        internal var currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY

        fun instance() = instance
    }

    private class ConfigSchedulerImpl : ConfigScheduler {
        override fun startConfig(delay: Long) {
            WorkManager.getInstance(InAppMessaging.instance().getHostAppContext()!!)
                    .beginUniqueWork(
                            CONFIG_WORKER_NAME,
                            ExistingWorkPolicy.REPLACE,
                            getConfigWorkRequest(delay)!!)
                    .enqueue()
        }

        /**
         * This method syncs with config service in the background with all necessary input data
         * bundled into Data and passed into Worker.
         */
        private fun getConfigWorkRequest(delay: Long): OneTimeWorkRequest? =
                // Creating a config work request.
                OneTimeWorkRequest.Builder(ConfigWorker::class.java)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setConstraints(WorkManagerUtil.getNetworkConnectedConstraint())
                        .build()
    }
}
