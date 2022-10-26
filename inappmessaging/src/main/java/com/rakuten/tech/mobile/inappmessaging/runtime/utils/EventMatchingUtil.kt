package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.matchingEventName

internal abstract class EventMatchingUtil {
    internal val persistentEvents = mutableSetOf<Event>()
    internal val matchedEvents = mutableMapOf<String, MutableList<Event>>()
    internal val triggeredPersistentCampaigns = mutableSetOf<String>()

    /**
     * Stores logged event.
     * Event won't be stored if it is not categorized as persistent event
     * or there are no campaigns with matching triggers.
     */
    abstract fun matchAndStore(event: Event)

    abstract fun matchedEvents(campaign: Message): List<Event>

    abstract fun containsAllMatchedEvents(campaign: Message): Boolean

    /**
     * Finds and removes one record for each event in set for provided campaign.
     * Operation succeeds only if there is at least one record of each event.
     * Function can be used with persistent events - they won't be removed.
     */
    abstract fun removeSetOfMatchedEvents(eventsToRemove: Set<Event>, campaign: Message): Boolean

    abstract fun clearNonPersistentEvents()

    companion object {
        private const val TAG = "IAM_EventMatchingUtil"
        private var instance: EventMatchingUtil = EventMatchingUtilImpl(CampaignRepository.instance())

        fun instance(): EventMatchingUtil = instance
    }

    /**
     * Stores logged events that match a campaign's trigger.
     */
    internal class EventMatchingUtilImpl(private val campaignRepo: CampaignRepository) : EventMatchingUtil() {

        @SuppressWarnings("LabeledExpression")
        override fun matchAndStore(event: Event) {
            if (event.isPersistentType()) {
                persistentEvents.add(event)
                return
            }

            campaignRepo.messages.values.forEach { campaign ->
                val campaignTriggers = campaign.getTriggers()
                if (campaignTriggers.isNullOrEmpty()) { return@forEach }
                if (!isEventMatchingOneOfTriggers(event, campaignTriggers)) { return@forEach }

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

        @SuppressWarnings("ComplexMethod", "ReturnCount")
        override fun removeSetOfMatchedEvents(eventsToRemove: Set<Event>, campaign: Message): Boolean {
            val campaignEvents = matchedEvents[campaign.getCampaignId()] ?: mutableListOf()
            val totalMatchedEvents = campaignEvents.count() + persistentEvents.count()

            if (!(totalMatchedEvents > 0 && totalMatchedEvents >= eventsToRemove.size)) {
                InAppLogger(TAG).debug("Couldn't find set of events")
                return false
            }

            val isCampaignPersistentEventsOnly = campaignEvents.isEmpty()
            if (isCampaignPersistentEventsOnly && triggeredPersistentCampaigns.contains(campaign.getCampaignId())) {
                return false
            }

            if (removeEvents(eventsToRemove, campaignEvents)) return false

            if (isCampaignPersistentEventsOnly) {
                triggeredPersistentCampaigns.add(campaign.getCampaignId())
            } else {
                matchedEvents[campaign.getCampaignId()] = campaignEvents
            }
            return true
        }

        private fun removeEvents(eventsToRemove: Set<Event>, campaignEvents: MutableList<Event>): Boolean {
            for (eventToRemove in eventsToRemove) {
                if (eventToRemove.isPersistentType() && persistentEvents.contains(eventToRemove)) {
                    continue
                }
                val index = campaignEvents.indexOf(eventToRemove)
                if (index == -1) {
                    InAppLogger(TAG).debug("Couldn't find requested set of events")
                    return true
                }
                campaignEvents.removeAt(index)
            }
            return false
        }

        override fun clearNonPersistentEvents() {
            matchedEvents.clear()
            triggeredPersistentCampaigns.clear()
        }

        private fun isEventMatchingOneOfTriggers(event: Event, triggers: List<Trigger>) =
            triggers.any { event.getEventName() == it.matchingEventName }

        private fun isTriggerMatchingOneOfEvents(trigger: Trigger, events: List<Event>) =
            events.any { it.getEventName() == trigger.matchingEventName }
    }
}
