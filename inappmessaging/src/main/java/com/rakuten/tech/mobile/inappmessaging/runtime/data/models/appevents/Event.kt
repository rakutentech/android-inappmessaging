package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Attribute
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * Interface of local events.
 */
@RestrictTo(LIBRARY)
interface Event {

    /**
     * This method returns event name.
     */
    @Nullable
    fun getEventName(): String?

    /**
     * This method returns event type.
     */
    @NotNull
    fun getEventType(): Int

    /**
     * This method returns event timestamp.
     */
    @NotNull
    fun getTimestamp(): Long

    /**
     * This method returns event is persistent type (can be used by campaigns multiple times).
     * If persistent type, event will not be removed in LocalEventRepository when used by a campaign.
     */
    @NotNull
    fun isPersistentType(): Boolean

    /**
     * This method returns RAT event map.
     */
    @RestrictTo(LIBRARY)
    @NotNull
    fun getRatEventMap(): Map<@NotNull String, @NotNull Any>

    /**
     * This method returns event attribute map.
     */
    @RestrictTo(LIBRARY)
    @NotNull
    fun getAttributeMap(): Map<@NotNull String, @Nullable Attribute?>

    /**
     * This method returns true if the event was logged when user information was updated.
     */
    fun isUserUpdated(): Boolean

    /**
     * Set to true if the event was logged when user information was updated.
     */
    fun setUserUpdated(isUpdated: Boolean)
}
