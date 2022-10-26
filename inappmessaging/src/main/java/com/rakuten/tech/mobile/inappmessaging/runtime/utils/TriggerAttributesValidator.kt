package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.OperatorType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ValueType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Attribute
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.TriggerAttribute
import java.util.Locale

internal object TriggerAttributesValidator {

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
    fun isTriggerSatisfied(trigger: Trigger, event: Event): Boolean {
        // Validate argument event is for the argument trigger and
        // if different custom events share the same eventType.
        if (trigger.eventType != event.getEventType() || (
            trigger.eventType == EventType.CUSTOM.typeId &&
                trigger.eventName.lowercase(Locale.getDefault()) != event.getEventName()
            )
        ) return false

        for (triggerAttribute in trigger.triggerAttributes) {
            // Get attribute from event by triggerAttribute's name.
            // Attribute's name should be in lower case.
            val eventAttribute =
                event.getAttributeMap()[triggerAttribute.name.lowercase(Locale.getDefault())] ?: return false
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
        if (triggerAttribute.name.lowercase(Locale.getDefault()) != eventAttribute.name) {
            return false
        }

        // Validate attribute's value type equals to event's attribute value type.
        val valueTypeId: Int = triggerAttribute.type
        return if (valueTypeId != eventAttribute.valueType) {
            // If trigger's attribute value type is different from event's, value can't be reconciled.
            false
        } else isValueReconciled(
            valueId = valueTypeId,
            eventValue = eventAttribute.value,
            operatorId = triggerAttribute.operator,
            triggerValue = triggerAttribute.value
        )
    }

    /**
     * The method checks if the value from event attribute can satisfy trigger's attribute values according to
     * trigger attribute's operator type.
     */
    @SuppressWarnings("LongMethod", "ComplexMethod", "ReturnCount")
    private fun isValueReconciled(valueId: Int, eventValue: String?, operatorId: Int, triggerValue: String?): Boolean {
        // Validate value strings. They shouldn't be null, but empty string is OK.
        if (eventValue == null || triggerValue == null) return false

        // Validate attribute's operator type and value type.
        val operatorType = OperatorType.getById(operatorId) ?: return false
        val valueType = ValueType.getById(valueId) ?: return false
        if (operatorType == OperatorType.INVALID || valueType == ValueType.INVALID) return false

        return when (valueType) {
            ValueType.STRING -> ValueMatchingUtil.isOperatorConditionSatisfied(eventValue, operatorType, triggerValue)
            ValueType.DOUBLE ->
                ValueMatchingUtil.isOperatorConditionSatisfied(
                    eventValue.toDoubleOrNull(), operatorType, triggerValue.toDoubleOrNull()
                )
            ValueType.BOOLEAN ->
                ValueMatchingUtil.isOperatorConditionSatisfied(
                    eventValue.toBoolean(), operatorType, triggerValue.toBoolean()
                )
            ValueType.INTEGER ->
                ValueMatchingUtil.isOperatorConditionSatisfied(
                    eventValue.toIntOrNull(), operatorType, triggerValue.toIntOrNull()
                )
            ValueType.TIME_IN_MILLI ->
                ValueMatchingUtil.isOperatorConditionSatisfied(
                    eventValue = eventValue.toLongOrNull(),
                    operatorType = operatorType,
                    triggerValue = triggerValue.toLongOrNull(),
                    isTime = true
                )
            ValueType.INVALID -> false
        }
    }
}
