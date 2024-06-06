package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.EventMatchingUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler

/**
 * SessionManager, it is the manager of session tracking. It will discard old message data, and
 * prepare new message data for the new user or session.
 */
internal object SessionManager {
    /**
     * Upon login successful or logout, old messages will be discarded, then prepare new messages for the new
     * user.
     */
    fun onSessionUpdate() {
        InAppLogger("IAM_SessionManager").debug("onSessionUpdate")

        if (!InAppMessaging.instance().isLocalCachingEnabled()) {
            // Clear locally stored campaigns from ping response
            CampaignRepository.instance().clearMessages()
        }

        // Clear matched events
        EventMatchingUtil.instance().clearNonPersistentEvents()

        // Clear campaigns which are ready for display
        MessageReadinessManager.instance().clearMessages()

        // Clear any stale user cache structure if applicable
        AccountRepository.instance().clearUserOldCacheStructure()

        // reset current delay to initial
        // future update: possibly add checking if last ping is within a certain threshold before executing the request
        MessageMixerPingScheduler.currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
        MessageMixerPingScheduler.instance().pingMessageMixerService(0)
    }
}
