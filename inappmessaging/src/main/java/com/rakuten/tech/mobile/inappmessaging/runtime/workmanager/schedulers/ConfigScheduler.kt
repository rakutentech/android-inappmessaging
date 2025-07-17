package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppError
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppErrorLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.SdkApi
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.WorkManagerUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.ConfigWorker
import java.util.concurrent.TimeUnit

/**
 * Scheduling workers to do their work in the background to communicate with IAM config service.
 */
@SuppressWarnings(
    "kotlin:S6517",
)
internal interface ConfigScheduler {

    fun startConfig(delay: Long = 0, workManager: WorkManager? = null)

    @SuppressWarnings("kotlin:S6515")
    companion object {
        private const val CONFIG_WORKER_NAME = "iam_config_worker"
        private var instance: ConfigScheduler = ConfigSchedulerImpl()
        internal var currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY

        fun instance() = instance
    }

    private class ConfigSchedulerImpl : ConfigScheduler {
        override fun startConfig(delay: Long, workManager: WorkManager?) {
            try {
                HostAppInfoRepository.instance().getContext()?.let { ctx ->
                    val manager = workManager ?: WorkManager.getInstance(ctx)
                    manager.beginUniqueWork(CONFIG_WORKER_NAME, ExistingWorkPolicy.REPLACE, getConfigWorkRequest(delay))
                        .enqueue()
                }
            } catch (ie: IllegalStateException) {
                // this should not occur since work manager is initialized during SDK initialization
                "In-App Messaging config request failed".let {
                    InAppErrorLogger.logError(
                        "ConfigScheduler",
                        InAppError(
                            it, InAppMessagingException(it, ie),
                            Event.OperationFailed(SdkApi.CONFIG.name),
                        ),
                    )
                }
            }
        }

        /**
         * This method syncs with config service in the background with all necessary input data
         * bundled into Data and passed into Worker.
         */
        private fun getConfigWorkRequest(delay: Long): OneTimeWorkRequest {
            // this is just to handle possible overflow but should never occur
            val newDelay = if (Long.MAX_VALUE - System.currentTimeMillis()
                <= TimeUnit.MILLISECONDS.toMillis(delay)
            ) {
                // reset current delay
                currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
                RetryDelayUtil.INITIAL_BACKOFF_DELAY
            } else {
                delay
            }
            // Creating a config work request.
            return OneTimeWorkRequest.Builder(ConfigWorker::class.java)
                .setInitialDelay(newDelay, TimeUnit.MILLISECONDS)
                .setConstraints(WorkManagerUtil.getNetworkConnectedConstraint())
                .build()
        }
    }
}
