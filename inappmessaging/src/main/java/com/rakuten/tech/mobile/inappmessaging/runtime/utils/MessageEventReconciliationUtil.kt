package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.matchingEventName
import java.util.Date

internal interface MessageEventReconciliationUtilType {

    /**
     * Validates whether a campaign is ready to be displayed by cross-referencing [CampaignRepository.messages]
     * and the list of [EventMatchingUtil.matchedEvents].
     */
    fun validate(validatedCampaignHandler: (campaign: Message, events: Set<Event>) -> Unit)
}

/**
 * Utility class helping MessageEventReconciliationWorker, handling the logic for checking if a campaign is ready
 * to be displayed.
 */
@SuppressWarnings("UnnecessaryAbstractClass")
internal abstract class MessageEventReconciliationUtil : MessageEventReconciliationUtilType {

    companion object {
        private var instance: MessageEventReconciliationUtil = MessageEventReconciliationUtilImpl(
            CampaignRepository.instance(),
            EventMatchingUtil.instance()
        )

        private const val TAG = "IAM_MsgEventReconcileUtil"

        fun instance(): MessageEventReconciliationUtil = instance
    }

    private class MessageEventReconciliationUtilImpl(
        private val campaignRepo: CampaignRepository,
        private val eventMatchingUtil: EventMatchingUtil
    ) : MessageEventReconciliationUtil() {

        @SuppressWarnings("ComplexMethod", "LongMethod", "ComplexCondition")
        override fun validate(validatedCampaignHandler: (campaign: Message, events: Set<Event>) -> Unit) {
            for (campaign in campaignRepo.messages.values) {
                if (campaign.impressionsLeft == 0 ||
                    (!campaign.isTest() && (campaign.isOptedOut == true || campaign.isOutdated))
                ) {
                    continue
                }

                val campaignTriggers = campaign.getTriggers()
                if (campaignTriggers.isNullOrEmpty()) {
                    InAppLogger(TAG).debug("Campaign (${campaign.getCampaignId()}) has no triggers.")
                    continue
                }

                if (!eventMatchingUtil.containsAllMatchedEvents(campaign)) {
                    continue
                }

                val triggeredEvents = triggerEvents(
                    campaignTriggers,
                    eventMatchingUtil.matchedEvents(campaign)
                ) ?: continue

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

private val Message.isOutdated: Boolean
    get() =
        if (hasNoEndDate()) {
            false
        } else {
            getMessagePayload().messageSettings.displaySettings.endTimeMillis < Date().time
        }
