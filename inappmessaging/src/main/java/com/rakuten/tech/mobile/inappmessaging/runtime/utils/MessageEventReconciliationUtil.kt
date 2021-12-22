package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.OperatorType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ValueType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Attribute
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalDisplayedMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalEventRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.PingResponseMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.TriggerAttribute
import timber.log.Timber
import java.util.Collections
import java.util.Locale
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

/**
 * Utility class helping MessageEventReconciliationWorker for reconciliation.
 */
@SuppressWarnings("LargeClass", "LabeledExpression")
internal interface MessageEventReconciliationUtil {

    /**
     * This method adds all test messages from [messageList] to ready message list without checking for triggers.
     * Test messages are by default ready to be displayed.
     */
    fun extractTestMessages(messageList: List<Message>): List<Message>

    /**
     * This method reconciles a list of messages with local events, return a list of reconciled ready messages.
     * Test messages will not be added to the returned list. No repeating messages will be added.
     */
    fun reconcileMessagesAndEvents(messages: List<Message>): MutableList<Message>

    companion object {
        private const val TAG = "MsgEventReconcileUtil"
        private var instance: MessageEventReconciliationUtil = MessageEventReconciliationUtilImpl()

        fun instance(): MessageEventReconciliationUtil = instance
    }

    private class MessageEventReconciliationUtilImpl : MessageEventReconciliationUtil {

        override fun extractTestMessages(messageList: List<Message>): List<Message> {
            val testMessages = ArrayList<Message>()
            // Add all test messages first.
            for (message in messageList) {
                if (message.isTest()) {
                    testMessages.add(message)
                }
            }
            return testMessages
        }

        override fun reconcileMessagesAndEvents(messages: List<Message>): MutableList<Message> {
            // Make an empty list of message, later add reconciled messages to it.
            val reconciledMessages = ArrayList<Message>()
            // Make a map of events for easy matching.
            val localEvents = aggregateLocalEvents()
            for (message in messages) {
                if (message.isTest()) {
                    // Skip test messages.
                    continue
                } else if (isMessageReconciled(message, localEvents)) {
                    // Check if message is reconciled.
                    // Add this message only once regardless of its max impressions.
                    reconciledMessages.add(message)
                }
            }
            return Collections.unmodifiableList(reconciledMessages)
        }

        /**
         * This method checks if the argument message could be reconciled with the argument local events.
         * The message has to be reconciled for enough times in order to be displayed.
         * To calculate how many times reconciliations are needed, see method getNumTimesToSatisfyTriggersForDisplay().
         * Once the getNumTimesToSatisfyTriggersForDisplay is reached, the method will return true
         * because there's no need to continue checking events any further.
         *
         * Note: once the event has been reconciled with the message, it will be removed from the
         * argument eventMap. Because each event can only be used once against a message.
         */
        @SuppressWarnings("ReturnCount")
        private fun isMessageReconciled(message: Message, eventMap: Map<String, MutableList<Event>>): Boolean {
            // Getting the number of reconciliations are needed in order to reconcile this message.
            val requiredSetsToSatisfy = getNumTimesToSatisfyTriggersForDisplay(message)

            // If requiredSetsToSatisfy <= 0, it's impossible to reconcile this
            // message. Because this message had been displayed equal or more than its max impressions.
            if (requiredSetsToSatisfy <= 0) {
                return false
            }

            val triggerList = message.getTriggers() ?: listOf()
            for (trigger in triggerList) {
                checkTrigger(trigger, eventMap, triggerList.size, message.getCampaignId(), requiredSetsToSatisfy)?.let {
                    return it
                }
            }
            // At this point, all triggers had been reconciled
            // ${requiredSetsOfSatisfiedTriggersToDisplayMessage} times.
            return true
        }

        @SuppressWarnings("LongMethod", "ReturnCount")
        private fun checkTrigger(
            trigger: Trigger,
            eventMap: Map<String, MutableList<Event>>,
            size: Int,
            id: String,
            required: Int
        ): Boolean? {
            // Make a copy of only relevant events to the argument trigger.
            val relevantEventsCopy = copyEventsForTrigger(trigger, eventMap)
            // If there are no relevant events, this trigger can't ever be satisfied.
            if (relevantEventsCopy.isNullOrEmpty()) return false

            // Reset numTriggersSatisfied in each outer loop. Because all triggers must be reach
            // requiredSetsOfSatisfiedTriggersToDisplayMessage times.
            var numTriggersSatisfied = 0

            // Reconcile each trigger with all relevant events.
            for (event in relevantEventsCopy) {
                if (isTriggerReconciled(trigger, event)) {
                    // Add this event to eventsToBeRemoved list because it can't be used again
                    // to satisfy any more triggers.
                    if (event.isPersistentType() && (size > 1 || PingResponseMessageRepository
                            .instance().shouldDisplayAppLaunchCampaign(id))
                    ) {
                        // If campaign depends on other events other than a persistent type (i.e. App Launch)
                        // or should at least be displayed once,
                        // no need to check the required number for satisfied triggers
                        return null
                    }

                    // Add numTriggersSatisfied by 1, and check the number against
                    // requiredSetsOfSatisfiedTriggersToDisplayMessage.
                    ++numTriggersSatisfied

                    if (numTriggersSatisfied >= required) {
                        // Break the inner loop after numTriggersSatisfied has reached to
                        // requiredSetsOfSatisfiedTriggersToDisplayMessage.
                        break
                    }
                }
            }

            // InAppMessaging's matching logic only support `AND` logic, meaning all triggers must be
            // satisfied by unique events. Therefore, if any trigger was not satisfied by unique events,
            // or the number of reconciliation needed is not reached, then this whole trigger list is not
            // satisfied.
            if (numTriggersSatisfied < required) return false

            return null
        }

        /**
         * This method checks if the argument trigger is reconciled with the argument event.
         *
         *
         * Note: all attributes in the trigger must be matched by the local event's attributes. But,
         * not all local event's attributes have to match with trigger's attributes. Meaning, local event
         * can have attributes that are not part of trigger's attributes. Also, all trigger's attributes
         * just need to be satisfied once by event's attributes.
         */
        @SuppressWarnings("ReturnCount")
        private fun isTriggerReconciled(trigger: Trigger, event: Event): Boolean {
            // Validate argument event is for the argument trigger and
            // if different custom events share the same eventType.
            if (trigger.eventType != event.getEventType() || (trigger.eventType == EventType.CUSTOM.typeId &&
                            trigger.eventName.toLowerCase(Locale.getDefault()) != event.getEventName())) return false

            for (triggerAttribute in trigger.triggerAttributes) {
                // Get attribute from event by triggerAttribute's name.
                // Attribute's name should be in lower case.
                val eventAttribute =
                    event.getAttributeMap()[triggerAttribute.name.toLowerCase(Locale.getDefault())] ?: return false
                // If no such attribute in the event, this trigger's attribute can't be satisfied.
                // Compare trigger attribute with the found event attribute. If attribute value can be
                // satisfied by event's attribute, continue with the next attribute.
                if (!isAttributeReconciled(triggerAttribute, eventAttribute)) {
                    // If any attribute value doesn't satisfy trigger attribute's condition, then this trigger
                    // can't be satisfied by this event.
                    return false
                }
            }
            // At this point, all attributes' conditions are satisfied by the argument event. Including
            // trigger without any attributes.
            return true
        }

        /**
         * This method checks if the argument TriggerAttribute object is reconciled with the argument Attribute from
         * event. This is done by comparing TriggerAttribute's value and operator against local event
         * Attribute's value.
         */
        private fun isAttributeReconciled(triggerAttribute: TriggerAttribute, eventAttribute: Attribute): Boolean {
            if (triggerAttribute.name.toLowerCase(Locale.getDefault()) != eventAttribute.name) {
                return false
            }

            // Validate attribute's value type equals to event's attribute value type.
            val valueTypeId: Int = triggerAttribute.type
            return if (valueTypeId != eventAttribute.valueType) {
                // If trigger's attribute value type is different from event's, value can't be reconciled.
                false
            } else isValueReconciled(
                    valueTypeId,
                    eventAttribute.value,
                    triggerAttribute.operator,
                    triggerAttribute.value)
        }

        /**
         * The method checks if the value from event attribute can satisfy trigger's attribute values according to
         * trigger attribute's operator type.
         */
        @SuppressWarnings("ReturnCount", "ComplexCondition", "LongMethod")
        private fun isValueReconciled(
            valueTypeId: Int,
            eventValue: String?,
            operatorTypeId: Int,
            triggerValue: String?
        ): Boolean {
            // Validate value strings. They shouldn't be null, but empty string is OK.
            if (eventValue == null || triggerValue == null) return false

            // Validate attribute's operator type and value type.
            val operatorType = OperatorType.getById(operatorTypeId)
            val valueType = ValueType.getById(valueTypeId)
            if (operatorType == null || operatorType == OperatorType.INVALID ||
                    valueType == null || valueType == ValueType.INVALID) return false

            return when (valueType) {
                ValueType.STRING ->
                    ValueMatchingUtil.isOperatorConditionSatisfied(eventValue, operatorType, triggerValue)
                ValueType.DOUBLE ->
                    ValueMatchingUtil.isOperatorConditionSatisfied(
                            eventValue.toDoubleOrNull(), operatorType, triggerValue.toDoubleOrNull())
                ValueType.BOOLEAN ->
                    ValueMatchingUtil.isOperatorConditionSatisfied(
                            eventValue.toBoolean(), operatorType, triggerValue.toBoolean())
                ValueType.INTEGER ->
                    ValueMatchingUtil.isOperatorConditionSatisfied(
                            eventValue.toIntOrNull(), operatorType, triggerValue.toIntOrNull())
                ValueType.TIME_IN_MILLI ->
                    ValueMatchingUtil.isOperatorConditionSatisfied(
                            eventValue.toLongOrNull(), operatorType, triggerValue.toLongOrNull(), true)
                else -> false
            }
        }

        /**
         * In order to determine if this message should be displayed, we need to match its triggers with
         * local events. If there were enough events satisfied message's triggers, then this message
         * should be displayed. But, how much is "enough"? For example, if this message has been displayed
         * 0 times, and its max impression is 3. Then only 1 set of qualified local events are needed to
         * satisfy this message's triggers. If message has been displayed 1 time, then 2 sets of the
         * qualified local events are needed to satisfy this message's triggers, and so on. In summary,
         * getNumTimesToSatisfyTriggersForDisplay = displayedImpression + 1.
         */
        @SuppressWarnings("FunctionMaxLength")
        private fun getNumTimesToSatisfyTriggersForDisplay(message: Message): Int {
            val maxImpression = message.getMaxImpressions()
            val displayedImpression: Int = LocalDisplayedMessageRepository.instance()
                    .numberOfTimesDisplayed(message)
            val displayedImpressionAfterLastPing: Int = LocalDisplayedMessageRepository.instance()
                    .numberOfDisplaysAfterPing(message)

            // Only check for message has been displayed less than its max impressions.
            // The number of times the message was removed from ready for display repository is considered since local
            // event list was not cleared and the triggers should  all be satisfied again.
            return if (displayedImpression < maxImpression) {
                displayedImpressionAfterLastPing + 1 + message.getNumberOfTimesClosed()
            } else 0
        }

        /**
         * This method reconstructs a map of events by event's name for easy reconciliation.
         *
         * Note: No events shall be removed the returned map. Because same event could be used to
         * satisfy different messages' triggers.
         */
        private fun aggregateLocalEvents(): Map<String, MutableList<Event>> {
            // Consolidate all local events into a map.
            val eventMap = HashMap<String, MutableList<Event>>()
            for (event in LocalEventRepository.instance().getEvents()) {
                val eventName = event.getEventName()
                if (eventName.isNotEmpty()) {
                    var eventList = eventMap[eventName]
                    if (eventList == null || eventList.isEmpty()) {
                        eventList = ArrayList()
                        eventMap[eventName] = eventList
                    }
                    eventList.add(event)
                }
            }

            // Make map's values unmodifiable.
            for (entry in eventMap.entries) {
                entry.setValue(Collections.unmodifiableList(entry.value))
            }
            // Return an unmodifiable map.
            return Collections.unmodifiableMap(eventMap)
        }

        /**
         * This method extracts then returns only relevant events from argument eventMap.
         * The purpose of only extracting relevant events is to increase performance
         * by only reconcile these relevant events.
         */
        @SuppressWarnings("ReturnCount", "LongMethod")
        private fun copyEventsForTrigger(trigger: Trigger, eventMap: Map<String, MutableList<Event>>):
                MutableList<Event>? {
            // Reconcile by trigger's type.
            val eventType = EventType.getById(trigger.eventType)
            if (eventType == null || eventType == EventType.INVALID) {
                Timber.tag(TAG).d("null or INVALID EventType.")
                // Trigger is null or INVALID, therefore, it can't be reconciled.
                return null
            }
            // CUSTOM event and trigger reconciliation.
            val eventName = if (eventType == EventType.CUSTOM) {
                // Custom event's name should go by the eventName variable in trigger.
                // Explicitly user lowercase to handle case-sensitive name on ping response
                trigger.eventName.toLowerCase(Locale.getDefault())
            } else {
                // Global event's name should go by its enum name, and it should be in lower case.
                eventType.name.toLowerCase(Locale.getDefault())
            }

            // Only retrieve a list of events according to trigger's event type. Since this list of events
            // is only a copy from the event repository, therefore, it's OK to modify or remove elements.
            var eventsCopy: MutableList<Event>? = null
            val eventsFound = eventMap[eventName]
            if (!eventsFound.isNullOrEmpty()) {
                eventsCopy = ArrayList(eventsFound)
            }
            return eventsCopy
        }
    }
}
