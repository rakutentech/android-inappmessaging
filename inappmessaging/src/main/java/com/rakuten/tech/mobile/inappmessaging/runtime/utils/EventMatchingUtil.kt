package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.matchingEventName

internal interface EventMatchingUtilType {

    /**
     * Stores logged event.
     * Event won't be stored if it is not categorized as persistent event
     * or there are no campaigns with matching triggers.
     */
    fun matchAndStore(event: Event)

    fun matchedEvents(campaign: Message): List<Event>

    fun containsAllMatchedEvents(campaign: Message): Boolean

    /**
     * Finds and removes one record for each event in set for provided campaign.
     * Operation succeeds only if there is at least one record of each event.
     * Function can be used with persistent events - they won't be removed.
     */
    fun removeSetOfMatchedEvents(eventsToRemove: Set<Event>, campaign: Message): Boolean

    fun clearNonPersistentEvents()
}

/**
 * Stores logged events that match a campaign's trigger.
 */
@SuppressWarnings("UnnecessaryAbstractClass")
internal abstract class EventMatchingUtil : EventMatchingUtilType {

    companion object {
        private const val TAG = "IAM_EventMatchingUtil"
        private var instance: EventMatchingUtil = EventMatchingUtilImpl(CampaignRepository.instance())

        fun instance(): EventMatchingUtil = instance
    }

    internal class EventMatchingUtilImpl(private val campaignRepo: CampaignRepository) : EventMatchingUtil() {
        private val persistentEvents = mutableSetOf<Event>()
        private val matchedEvents = mutableMapOf<String, MutableList<Event>>()
        private val triggeredPersistentEventOnlyCampaigns = mutableSetOf<String>()

        @SuppressWarnings("LabeledExpression", "LongMethod")
        override fun matchAndStore(event: Event) {
            if (event.isPersistentType()) {
                persistentEvents.add(event)
                return
            }

            campaignRepo.messages.values.forEach { campaign ->
                val campaignTriggers = campaign.getTriggers()
                if (campaignTriggers.isNullOrEmpty()) {
                    return@forEach
                }
                if (!isEventMatchingOneOfTriggers(event, campaignTriggers)) {
                    return@forEach
                }

                val events = matchedEvents[campaign.getCampaignId()] ?: mutableListOf()
                events.add(event)
                matchedEvents[campaign.getCampaignId()] = events
                InAppLogger(TAG).debug(
                    "Campaign (${campaign.getCampaignId()}) matched events:" +
                        matchedEvents(campaign).map { it.getEventName() }
                )
            }
        }

        override fun matchedEvents(campaign: Message) =
            matchedEvents[campaign.getCampaignId()].orEmpty() + persistentEvents

        override fun containsAllMatchedEvents(campaign: Message): Boolean {
            val triggers = campaign.getTriggers()
            if (triggers.isNullOrEmpty()) {
                return false
            }
            val events = matchedEvents[campaign.getCampaignId()].orEmpty() + persistentEvents
            return triggers.all { isTriggerMatchingOneOfEvents(it, events) }
        }

        @SuppressWarnings("ComplexMethod", "LongMethod", "ReturnCount")
        override fun removeSetOfMatchedEvents(eventsToRemove: Set<Event>, campaign: Message): Boolean {
            val campaignEvents = matchedEvents[campaign.getCampaignId()] ?: mutableListOf()
            val totalMatchedEvents = campaignEvents.count() + persistentEvents.count()

            if (!(totalMatchedEvents > 0 && totalMatchedEvents >= eventsToRemove.size)) {
                InAppLogger(TAG).debug("Couldn't find set of events")
                return false
            }

            val isCampaignPersistentEventsOnly = campaignEvents.isEmpty()
            if (isCampaignPersistentEventsOnly &&
                triggeredPersistentEventOnlyCampaigns.contains(campaign.getCampaignId())
            ) {
                InAppLogger(TAG).debug("Provided set of events already used")
                return false
            }

            for (eventToRemove in eventsToRemove) {
                if (eventToRemove.isPersistentType() && persistentEvents.contains(eventToRemove)) {
                    continue
                }
                val index = campaignEvents.indexOf(eventToRemove)
                if (index == -1) {
                    InAppLogger(TAG).debug("Couldn't find requested set of events")
                    return false
                }
                campaignEvents.removeAt(index)
            }

            if (isCampaignPersistentEventsOnly) {
                triggeredPersistentEventOnlyCampaigns.add(campaign.getCampaignId())
            } else {
                matchedEvents[campaign.getCampaignId()] = campaignEvents
            }
            return true
        }

        override fun clearNonPersistentEvents() {
            matchedEvents.clear()
            triggeredPersistentEventOnlyCampaigns.clear()
        }

        private fun isEventMatchingOneOfTriggers(event: Event, triggers: List<Trigger>) =
            triggers.any { event.getEventName() == it.matchingEventName }

        private fun isTriggerMatchingOneOfEvents(trigger: Trigger, events: List<Event>) =
            events.any { it.getEventName() == trigger.matchingEventName }
    }
}
