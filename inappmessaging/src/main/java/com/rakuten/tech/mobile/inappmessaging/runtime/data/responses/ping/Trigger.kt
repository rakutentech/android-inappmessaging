package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import java.util.Locale

/**
 * Class for parsing Trigger, which is a response from MessageMixer.
 */
internal data class Trigger(
    val type: Int,
    val eventType: Int,
    val eventName: String,
    @SerializedName("attributes") val triggerAttributes: MutableList<TriggerAttribute>,
) {
    val matchingEventName: String?
        get() {
            val eventType = EventType.getById(eventType)
            if (eventType == null || eventType == EventType.INVALID) {
                return null
            }
            return if (eventType == EventType.CUSTOM) {
                // Custom event's name should go by the eventName variable in trigger.
                // Explicitly user lowercase to handle case-sensitive name on ping response
                eventName.lowercase(Locale.getDefault())
            } else {
                // Global event's name should go by its enum name, and it should be in lower case.
                eventType.name.lowercase(Locale.getDefault())
            }
        }
}
internal data class TriggerAttribute(
    val name: String,
    val value: String,
    val type: Int,
    val operator: Int,
)
