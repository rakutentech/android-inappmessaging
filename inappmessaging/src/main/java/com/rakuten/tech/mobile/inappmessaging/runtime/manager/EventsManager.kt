package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.EventMatchingUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler

/**
 * EventsManager accepts local events.
 */
internal object EventsManager {

    private var isUpdated = false

    /**
     * This method adds logEvent on the events list.
     * Then starts session update and event worker to process that logEvent.
     */
    @SuppressWarnings("LongMethod")
    fun onEventReceived(
        event: Event,
        eventMatchingUtil: EventMatchingUtil = EventMatchingUtil.instance(),
        eventScheduler: EventMessageReconciliationScheduler = EventMessageReconciliationScheduler.instance(),
        accountRepo: AccountRepository = AccountRepository.instance()
    ) {
        val isUserUpdated = accountRepo.updateUserInfo()
        val isAdded = eventMatchingUtil.matchAndStore(event)
        if (isAdded) {
            if (isUserUpdated || isUpdated) {
                isUpdated = false
                // Update session when there are updates in user info
                // event reconciliation worker is already part of session update
                SessionManager.onSessionUpdate(event)
            } else if (ConfigResponseRepository.instance().isConfigEnabled()) {
                eventScheduler.startEventMessageReconciliationWorker()
            }
        } else if (InAppMessaging.instance().isLocalCachingEnabled()) {
            // retain "user updated status" for next non-persistent to trigger ping request
            isUpdated = isUserUpdated
        }
    }
}
