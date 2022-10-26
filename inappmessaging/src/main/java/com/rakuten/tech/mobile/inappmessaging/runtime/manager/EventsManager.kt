package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
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
    fun onEventReceived(
        event: Event,
        eventMatchingUtil: EventMatchingUtil = EventMatchingUtil.instance(),
        eventScheduler: EventMessageReconciliationScheduler = EventMessageReconciliationScheduler.instance()
    ) {
        if (ConfigResponseRepository.instance().isConfigEnabled()) {
            eventMatchingUtil.matchAndStore(event)
            eventScheduler.startReconciliationWorker()
        }
    }
}
