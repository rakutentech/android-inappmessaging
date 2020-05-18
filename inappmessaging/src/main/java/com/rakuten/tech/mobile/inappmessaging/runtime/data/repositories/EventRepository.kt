package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event

/**
 * EventRepository for logEvent repos to implement.
 */
internal interface EventRepository {
    /**
     * This method adds logEvent to the logEvent repository.
     *
     * Returns true if event was added, false if event is a persistent type and already exists in the local list.
     */
    fun addEvent(event: Event): Boolean

    /**
     * This method returns a list of all the Event objects in the logEvent repository.
     */
    fun getEvents(): List<Event>
}
