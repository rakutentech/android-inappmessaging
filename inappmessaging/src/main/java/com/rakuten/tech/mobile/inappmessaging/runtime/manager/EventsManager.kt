package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.rakuten.tech.mobile.inappmessaging.runtime.LegacyEventBroadcasterHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalEventRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler

/**
 * EventsManager accepts local events.
 */
internal object EventsManager {
    /**
     * This method adds logEvent on the events list.
     * Then starts session update and event worker to process that logEvent.
     */
    fun onEventReceived(
        event: Event,
        sendEvent: (String, Map<String, *>?) -> Unit = LegacyEventBroadcasterHelper::sendEvent,
        localEventRepo: LocalEventRepository = LocalEventRepository.instance(),
        eventScheduler: EventMessageReconciliationScheduler = EventMessageReconciliationScheduler.instance(),
        accountRepo: AccountRepository = AccountRepository.instance()
    ) {
        // Caching events locally.
        val isAdded = localEventRepo.addEvent(event)
        if (isAdded) {
            if (accountRepo.updateUserInfo()) {
                // Update session when there are updates in user info
                // event reconciliation worker is already part of session update
                SessionManager.onSessionUpdate()
            } else if (ConfigResponseRepository.instance().isConfigEnabled()) {
                eventScheduler.startEventMessageReconciliationWorker()
            }
        }

        // Broadcasting host app logged event to Analytics SDK.
        sendEvent(InAppMessagingConstants.RAT_EVENT_KEY_EVENTS, event.getRatEventMap())
    }
}
