package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.EventMatchingUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler

/**
 * EventsManager accepts local events.
 */
internal object EventsManager {

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
        eventMatchingUtil.matchAndStore(event)
        if (isUserUpdated) {
            // Update session when there are updates in user info
            // event reconciliation worker is already part of session update
            SessionManager.onSessionUpdate(event)
        } else if (ConfigResponseRepository.instance().isConfigEnabled()) {
            eventScheduler.startEventMessageReconciliationWorker()
        }
    }
}
