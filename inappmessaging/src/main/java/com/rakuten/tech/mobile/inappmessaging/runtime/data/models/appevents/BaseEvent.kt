package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Attribute
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.AnalyticsKey
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.jetbrains.annotations.NotNull
import java.util.Locale
import java.util.Calendar
import java.util.Collections

/**
 * BaseEvent will be the base class of all local events objects, including CustomEvent. During
 * initialization, constructor parameters will be checked for validity if there are reasons to
 * believe that parameter could be invalid.
 *
 * <p>Constructor will automatically store eventType, eventName, and timestamp at the moment of
 * object creation.
 *
 * <p>Event name can't be empty or null, else IllegalArgumentException will be thrown. Also,
 * logEvent name will be stored in upper case form.
 */
@RestrictTo(LIBRARY)
@SuppressWarnings("UnnecessaryAbstractClass")
abstract class BaseEvent(
    @NotNull private val eventType: EventType,
    @NotNull private var eventName: String,
    @NotNull private val isPersistent: Boolean,
) : Event {
    private val timestamp: Long

    init {
        require(this.eventName.isNotEmpty()) { InAppMessagingConstants.EVENT_NAME_EMPTY_EXCEPTION }
        require(this.eventName.length <= MAX_EVENT_NAME_CHARACTER_LENGTH) {
            InAppMessagingConstants.EVENT_NAME_TOO_LONG_EXCEPTION
        }

        // Use lower case eventName only.
        this.eventName = this.eventName.lowercase(Locale.getDefault())
        this.timestamp = Calendar.getInstance().timeInMillis
    }

    /**
     * This method returns the event name.
     */
    override fun getEventName(): String = eventName

    /**
     * This method returns the event type id.
     */
    @NotNull
    override fun getEventType(): Int = eventType.typeId

    /**
     * This method returns the timestamp.
     */
    @NotNull
    override fun getTimestamp(): Long = timestamp

    /**
     * This method returns event is persistent type (can be used by campaigns multiple times).
     * If persistent type, event will not be removed in LocalEventRepository when used by a campaign.
     */
    @NotNull
    override fun isPersistentType(): Boolean = isPersistent

    /**
     * A map will be created containing event's base attributes like event name, and timestamp.
     */
    @RestrictTo(LIBRARY)
    @NotNull
    override fun getRatEventMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[AnalyticsKey.EVENT_NAME.key] = eventName
        map[AnalyticsKey.TIMESTAMP.key] = timestamp
        return Collections.unmodifiableMap(map)
    }

    /**
     * This method is intended to be used by child classes which doesn't override this method.
     * Returns an empty map.
     */
    @RestrictTo(LIBRARY)
    @NotNull
    override fun getAttributeMap(): Map<String, Attribute?> = HashMap()

    companion object {
        private const val MAX_EVENT_NAME_CHARACTER_LENGTH = 255
    }
}
