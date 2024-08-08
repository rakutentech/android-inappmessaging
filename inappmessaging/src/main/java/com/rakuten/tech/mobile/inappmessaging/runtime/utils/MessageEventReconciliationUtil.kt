package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger

internal class MessageEventReconciliationUtil(
    private val campaignRepo: CampaignRepository,
    private val eventMatchingUtil: EventMatchingUtil,
) {

    /**
     * Validates whether a campaign is ready to be displayed by cross-referencing [CampaignRepository.messages]
     * and the list of [EventMatchingUtil.matchedEvents].
     */
    @SuppressWarnings("ComplexCondition")
    fun validate(validatedCampaignHandler: (campaign: Message, events: Set<Event>) -> Unit) {
        for (campaign in campaignRepo.messages.values) {
            if (campaign.impressionsLeft == 0 ||
                (!campaign.isTest && (campaign.isOptedOut == true || campaign.isOutdated))
            ) { continue }

            val triggers = campaign.triggers
            if (triggers.isNullOrEmpty()) {
                InAppLogger(TAG).debug("campaign (${campaign.campaignId}) has no triggers.")
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

    companion object {
        private const val TAG = "IAM_MsgEventReconcileUtil"
    }
}
