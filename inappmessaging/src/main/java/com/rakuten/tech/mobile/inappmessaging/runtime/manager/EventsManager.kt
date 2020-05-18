package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.rakuten.tech.mobile.inappmessaging.runtime.LegacyEventBroadcasterHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalEventRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.EventMessageReconciliationScheduler

/**
 * EventsManager accepts local events.
 */
internal object EventsManager {
    /**
     * This method adds logEvent on the events list. Then start EventWorker to process that logEvent.
     */
    fun onEventReceived(
        event: Event,
        sendEvent: (String, Map<String, *>?) -> Unit = LegacyEventBroadcasterHelper::sendEvent,
        localEventRepo: LocalEventRepository = LocalEventRepository.instance(),
        eventScheduler: EventMessageReconciliationScheduler = EventMessageReconciliationScheduler.instance()
    ) {
        // Caching events locally.
        val isAdded = localEventRepo.addEvent(event)
        // If event is a LoginSuccessfulEvent, then notify SessionManager.
        if (event.getEventType() == EventType.LOGIN_SUCCESSFUL.typeId) {
            SessionManager.onSessionUpdate()
        }
        // Broadcasting host app logged event to Analytics SDK.
        sendEvent(InAppMessagingConstants.RAT_EVENT_KEY_EVENTS, event.getRatEventMap())
        // Start reconciliation process if config service is enabled.
        if (ConfigResponseRepository.instance().isConfigEnabled() && isAdded) {
            eventScheduler.startEventMessageReconciliationWorker()
        }
    }
}
