package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger

internal abstract class MessageEventReconciliationUtil(
    internal val campaignRepo: CampaignRepository,
    internal val eventMatchingUtil: EventMatchingUtil,
) {

    /**
     * Validates whether a campaign is ready to be displayed by cross-referencing [CampaignRepository.messages]
     * and the list of [EventMatchingUtil.matchedEvents].
     */
    abstract fun validate(validatedCampaignHandler: (campaign: Message, events: Set<Event>) -> Unit)

    companion object {
        private var instance: MessageEventReconciliationUtil = MessageEventReconciliationUtilImpl(
            CampaignRepository.instance(),
            EventMatchingUtil.instance(),
        )

        private const val TAG = "IAM_MsgEventReconcileUtil"

        fun instance(): MessageEventReconciliationUtil = instance
    }

    /**
     * Utility class helping MessageEventReconciliationWorker, handling the logic for checking if a campaign is ready
     * to be displayed.
     */
    private class MessageEventReconciliationUtilImpl(
        campaignRepo: CampaignRepository,
        eventMatchingUtil: EventMatchingUtil,
    ) : MessageEventReconciliationUtil(campaignRepo, eventMatchingUtil) {

        @SuppressWarnings("ComplexMethod", "ComplexCondition")
        override fun validate(validatedCampaignHandler: (campaign: Message, events: Set<Event>) -> Unit) {
            for (campaign in campaignRepo.messages.values) {
                if (campaign.impressionsLeft == 0 ||
                    (!campaign.isTest && (campaign.isOptedOut == true || campaign.isOutdated))
                ) { continue }

                val triggers = campaign.triggers
                if (triggers.isNullOrEmpty()) {
                    InAppLogger(TAG).debug("Campaign (${campaign.campaignId}) has no triggers.")
                    continue
                }

                if (!eventMatchingUtil.containsAllMatchedEvents(campaign)) { continue }

                val triggeredEvents = triggerEvents(triggers, eventMatchingUtil.matchedEvents(campaign)) ?: continue

                validatedCampaignHandler(campaign, triggeredEvents)
            }
        }

        /**
         * Finds set of events that match all triggers.
         */
        @SuppressWarnings("ReturnCount")
        private fun triggerEvents(triggers: List<Trigger>, loggedEvents: List<Event>): Set<Event>? {
            if (loggedEvents.isEmpty()) {
                return null
            }

            val triggeredEvents = mutableSetOf<Event>()
            for (trigger in triggers) {
                val event = loggedEvents.firstOrNull {
                    it.getEventName() == trigger.matchingEventName &&
                        TriggerAttributesValidator.isTriggerSatisfied(trigger, it) // Check attributes
                } ?: // No event found for this trigger
                    return null
                triggeredEvents.add(event)
            }
            return triggeredEvents
        }
    }
}
