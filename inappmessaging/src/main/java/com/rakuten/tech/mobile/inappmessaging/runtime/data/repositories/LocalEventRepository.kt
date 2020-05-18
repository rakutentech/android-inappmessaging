package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import timber.log.Timber
import java.util.Collections
import kotlin.collections.ArrayList
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * LocalEventRepository will store all incoming events from host app.
 */
internal interface LocalEventRepository : EventRepository {
    /**
     * This method returns a copy of list which contains all events.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun clearEvents()

    companion object {
        private const val TAG = "IAM_LocalEventRepo"
        private var instance: LocalEventRepository = LocalEventRepositoryImpl()

        fun instance() = instance
    }

    private class LocalEventRepositoryImpl : LocalEventRepository {
        private val events = ArrayList<Event>()

        /**
         * This method adds logEvent into the events map.
         * If logEvent name is empty, an IllegalArgumentException will be thrown.
         */
        @Throws(IllegalArgumentException::class)
        override fun addEvent(event: Event) {
            require(!event.getEventName().isNullOrEmpty()) { InAppMessagingConstants.ARGUMENT_IS_EMPTY_EXCEPTION }

            synchronized(events) {
                if (shouldIgnore(event)) {
                    // If persistent type, event should only be stored once.
                    return
                }
                events.add(event)
                Timber.tag(TAG).d(event.getEventName())
                event.getAttributeMap().forEach { (key, value) ->
                    Timber.tag(TAG).d("Key: %s", key)
                    Timber.tag(TAG).d(
                            "Value name: %s, Value Type: %d, Value data: %s",
                            value?.name,
                            value?.valueType,
                            value?.value)
                }
            }
        }

        /**
         * This method returns a copy of list which contains all events.
         */
        override fun getEvents(): List<Event> {
            synchronized(events) {
                return Collections.unmodifiableList(events)
            }
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        override fun clearEvents() {
            synchronized(events) {
                if (events.isNotEmpty()) {
                    events.clear()
                }
            }
        }

        private fun shouldIgnore(event: Event): Boolean {
            if (event.isPersistentType()) {
                for (currEvent in events) {
                    if (event.getEventType() == currEvent.getEventType() &&
                            event.getEventName() == currEvent.getEventName()) return true
                }
            }

            return false
        }
    }
}
