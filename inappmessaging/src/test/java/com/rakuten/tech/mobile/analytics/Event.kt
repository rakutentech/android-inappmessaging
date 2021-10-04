package com.rakuten.tech.mobile.analytics

/**
 * An event to be tracked.
 * @param name Event's name
 * @param parameters Event parameters
 */
class Event(name: String, parameters: Map<String, Any>) {

    /**
     * Method to track an event.
     */
    @SuppressWarnings("EmptyFunctionBlock")
    fun track() = Unit
}
