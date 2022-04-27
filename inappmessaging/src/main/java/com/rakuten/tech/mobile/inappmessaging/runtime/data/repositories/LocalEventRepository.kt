package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.CustomEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.LoginSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import org.json.JSONArray
import java.lang.ClassCastException
import java.util.Collections
import kotlin.collections.ArrayList

/**
 * LocalEventRepository will store all incoming events from host app.
 */
internal interface LocalEventRepository : EventRepository {
    /**
     * This method removes all stored events for testing.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun clearEvents()

    /**
     * This method removes all stored non-persistent events.
     * This is done during session update due to user info update.
     */
    fun clearNonPersistentEvents()

    /**
     * This method removes all stored non-persistent events triggered before the given time [timeMillis].
     *
     * @param timeMillis represents the given time in UTC milliseconds from the epoch.
     */
    fun clearNonPersistentEvents(timeMillis: Long)

    companion object {
        private const val TAG = "IAM_LocalEventRepo"
        private var instance: LocalEventRepository = LocalEventRepositoryImpl()

        @VisibleForTesting
        internal const val LOCAL_EVENT_KEY = "local_event_list"

        fun instance() = instance
    }

    private class LocalEventRepositoryImpl : LocalEventRepository {
        private val events = ArrayList<Event>()
        private var user = ""

        init {
            synchronized(events) {
                checkAndResetList(true)
            }
        }

        /**
         * This method adds logEvent into the events map.
         * If logEvent name is empty, an IllegalArgumentException will be thrown.
         */
        @SuppressWarnings("ReturnCount")
        override fun addEvent(event: Event): Boolean {
            if (event.getEventName().isEmpty()) {
                InAppMessaging.errorCallback?.let {
                    it(InAppMessagingException("In-App Messaging adding event failed due to invalid event name"))
                }
                return false
            }

            synchronized(events) {
                checkAndResetList()
                // If persistent type, event should only be stored once.
                if (shouldIgnore(event)) return false

                events.add(event)
                debugLog(event)
                saveUpdatedList()
            }
            return true
        }

        private fun debugLog(event: Event) {
            Logger(TAG).debug(event.getEventName())
            for ((key, value) in event.getAttributeMap()) {
                Logger(TAG).debug("Key: %s", key)
                Logger(TAG).debug(
                    "Value name: %s, Value Type: %d, Value data: %s", value?.name, value?.valueType,
                    value?.value
                )
            }
        }

        /**
         * This method returns a copy of list which contains all events.
         */
        override fun getEvents(): List<Event> {
            synchronized(events) {
                // check if caching is enabled and if there are changes in user info
                checkAndResetList()
                return Collections.unmodifiableList(events)
            }
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        override fun clearEvents() {
            synchronized(events) {
                if (events.isNotEmpty()) {
                    events.clear()
                    saveUpdatedList()
                }
            }
        }

        override fun clearNonPersistentEvents() {
            synchronized(events) {
                if (events.isNotEmpty()) {
                    events.removeAll { ev -> !ev.isPersistentType() }
                }
            }
        }

        /**
         * {@inheritDoc}.
         * */
        override fun clearNonPersistentEvents(timeMillis: Long) {
            synchronized(events) {
                if (events.isNotEmpty()) {
                    events.removeAll { ev ->
                        !ev.isPersistentType() && ev.getTimestamp() < timeMillis && !ev.shouldNotClear()
                    }

                    // reset should not clear flag
                    events.forEach {
                        it.setShouldNotClear(false)
                    }
                }
                saveUpdatedList()
            }
        }

        private fun shouldIgnore(event: Event): Boolean {
            if (event.isPersistentType()) {
                for (currEvent in events) {
                    if (event.getEventType() == currEvent.getEventType() &&
                        event.getEventName() == currEvent.getEventName()
                    ) return true
                }
            }

            return false
        }

        @SuppressWarnings("LongMethod")
        private fun checkAndResetList(onLaunch: Boolean = false) {
            // check if caching is enabled and if there are changes in user info
            if (InAppMessaging.instance().isLocalCachingEnabled() &&
                (onLaunch || user != AccountRepository.instance().userInfoHash)
            ) {
                user = AccountRepository.instance().userInfoHash
                // reset event list from cached using updated user info
                val listString = try {
                    InAppMessaging.instance().getHostAppContext()?.let { ctx ->
                        PreferencesUtil.getString(
                            context = ctx,
                            name = InAppMessaging.getPreferencesFile(),
                            key = LOCAL_EVENT_KEY,
                            defValue = ""
                        )
                    }.orEmpty()
                } catch (ex: ClassCastException) {
                    Logger(TAG).debug(ex.cause, "Incorrect type for $LOCAL_EVENT_KEY data")
                    ""
                }
                if (listString.isNotEmpty()) {
                    events.clear()
                    deserializeLocalEvents(listString)
                } else if (events.isNotEmpty()) {
                    // retain persistent event for user with no stored data
                    events.removeAll { ev -> !ev.isPersistentType() }
                }
            }
        }

        @SuppressWarnings("TooGenericExceptionCaught", "ComplexMethod")
        private fun deserializeLocalEvents(listString: String) {
            try {
                val jsonArray = JSONArray(listString)
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val event = when (item["eventType"].toString()) {
                        EventType.APP_START.name -> Gson().fromJson(item.toString(), AppStartEvent::class.java)
                        EventType.LOGIN_SUCCESSFUL.name ->
                            Gson().fromJson(item.toString(), LoginSuccessfulEvent::class.java)
                        EventType.PURCHASE_SUCCESSFUL.name ->
                            Gson().fromJson(item.toString(), PurchaseSuccessfulEvent::class.java)
                        EventType.CUSTOM.name -> Gson().fromJson(item.toString(), CustomEvent::class.java)
                        else -> null
                    }
                    event?.let { events.add(it) }
                }
            } catch (ex: Exception) { Logger(TAG).debug(ex.cause, "Invalid JSON format for $LOCAL_EVENT_KEY data") }
        }

        private fun saveUpdatedList() {
            // check if caching is enabled to update persistent data
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                // save updated event list
                InAppMessaging.instance().getHostAppContext()?.let {
                    PreferencesUtil.putString(
                        context = it,
                        name = InAppMessaging.getPreferencesFile(),
                        key = LOCAL_EVENT_KEY,
                        value = Gson().toJson(events)
                    )
                } ?: Logger(TAG).debug("failed saving event data")
            }
        }
    }
}
