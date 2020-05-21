package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ValueType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Attribute
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.rat.RatAttribute
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import kotlin.collections.HashMap
import java.util.Date
import java.util.Collections

/**
 * Logging custom event for client to use.
 * Note: Please don't use the same attribute's name more than once
 * because the new attribute will replace the old one.
 */
class CustomEvent(eventName: String) : BaseEvent(EventType.CUSTOM, eventName, false) {
    private val attributesMap: MutableMap<String, Attribute> = HashMap()

    /**
     * This method adds custom attribute of integer type.
     */
    @NotNull
    fun addAttribute(@NotNull key: String, @NotNull value: Int): CustomEvent {
        attributesMap[key] = Attribute(key, value.toString(), ValueType.INTEGER)
        return this
    }

    /**
     * This method adds custom attribute of double type.
     */
    @NotNull
    fun addAttribute(@NotNull key: String, @NotNull value: Double): CustomEvent {
        attributesMap[key] = Attribute(key, value.toString(), ValueType.DOUBLE)
        return this
    }

    /**
     * This method adds custom attribute of string type.
     */
    @NotNull
    fun addAttribute(@NotNull key: String, @NotNull value: String): CustomEvent {
        attributesMap[key] = Attribute(key, value, ValueType.STRING)
        return this
    }

    /**
     * This method adds custom attribute of boolean type.
     */
    @NotNull
    fun addAttribute(@NotNull key: String, @NotNull value: Boolean): CustomEvent {
        attributesMap[key] = Attribute(key, value.toString(), ValueType.BOOLEAN)
        return this
    }

    /**
     * This method adds custom attribute of date type.
     */
    @NotNull
    fun addAttribute(@NotNull key: String, @NotNull value: Date): CustomEvent {
        attributesMap[key] = Attribute(key, value.time.toString(), ValueType.TIME_IN_MILLI)
        return this
    }

    /**
     * This method returns an unmodifiable map which contains all custom attributes.
     */
    @RestrictTo(LIBRARY)
    @NotNull
    override fun getAttributeMap(): Map<@NotNull String, @Nullable Attribute?> =
            Collections.unmodifiableMap(attributesMap)

    /**
     * This method returns an unmodifiable map which contains all event's attributes.
     */
    @RestrictTo(LIBRARY)
    @NotNull
    override fun getRatEventMap(): Map<@NotNull String, @NotNull Any> {
        // Making a list of all custom attributes.
        val ratAttributeList = ArrayList<RatAttribute>()
        for (attribute in attributesMap.values) {
            ratAttributeList.add(RatAttribute(attribute.name, attribute.value))
        }

        // Inherit basic attributes, and add custom attributes.
        val map = HashMap(super.getRatEventMap())
        map[InAppMessagingConstants.RAT_EVENT_KEY_EVENT_CUSTOM_ATTRIBUTE] = ratAttributeList

        return Collections.unmodifiableMap(map)
    }
}
