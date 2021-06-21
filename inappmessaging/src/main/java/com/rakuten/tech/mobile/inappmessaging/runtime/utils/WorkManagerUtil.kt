package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import androidx.work.Constraints
import androidx.work.NetworkType

/**
 * Utility class for WorkManager related classes.
 */
internal object WorkManagerUtil {
    /**
     * This method returns work constraints for impression worker.
     */
    fun getNetworkConnectedConstraint(): Constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
}
