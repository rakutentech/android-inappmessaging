package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event

/**
 * EventRepository for logEvent repos to implement.
 */
internal interface EventRepository {
    /**
     * This method adds logEvent to the logEvent repository.
     */
    fun addEvent(event: Event)

    /**
     * This method returns a list of all the Event objects in the logEvent repository.
     */
    fun getEvents(): List<Event>
}
