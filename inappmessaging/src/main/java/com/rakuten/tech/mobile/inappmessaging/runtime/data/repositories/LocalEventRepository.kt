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
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.json.JSONArray
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
     * This method removes all stored events for testing.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun clearEvents()

    /**
     * This method removes all stored non-persistent events.
     * This is done during session update due to user info update.
     */
    fun clearNonPersistentEvents()

    companion object {
        private const val TAG = "IAM_LocalEventRepo"
        private var instance: LocalEventRepository = LocalEventRepositoryImpl()
        private const val LOCAL_EVENT_KEY = "local_event_list"

        fun instance() = instance
    }

    private class LocalEventRepositoryImpl : LocalEventRepository {
        private val events = ArrayList<Event>()
        private var user = ""

        init {
            checkAndResetList(true)
        }

        /**
         * This method adds logEvent into the events map.
         * If logEvent name is empty, an IllegalArgumentException will be thrown.
         */
        @Throws(IllegalArgumentException::class)
        override fun addEvent(event: Event): Boolean {
            require(!event.getEventName().isNullOrEmpty()) { InAppMessagingConstants.ARGUMENT_IS_EMPTY_EXCEPTION }

            synchronized(events) {
                checkAndResetList()
                // If persistent type, event should only be stored once.
                if (shouldIgnore(event)) return false

                events.add(event)
                Timber.tag(TAG).d(event.getEventName())
                event.getAttributeMap().forEach { (key, value) ->
                    Timber.tag(TAG).d("Key: %s", key)
                    Timber.tag(TAG).d(
                            "Value name: %s, Value Type: %d, Value data: %s", value?.name, value?.valueType,
                            value?.value)
                }
                saveUpdatedList()
            }
            return true
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

        private fun shouldIgnore(event: Event): Boolean {
            if (event.isPersistentType()) {
                for (currEvent in events) {
                    if (event.getEventType() == currEvent.getEventType() &&
                            event.getEventName() == currEvent.getEventName()) return true
                }
            }

            return false
        }

        private fun checkAndResetList(onLaunch: Boolean = false) {
            // check if caching is enabled and if there are changes in user info
            if (InAppMessaging.instance().isLocalCachingEnabled() &&
                    (onLaunch || user != AccountRepository.instance().userInfoHash)) {
                user = AccountRepository.instance().userInfoHash
                // reset message list from cached using updated user info
                val listString = InAppMessaging.instance().getEncryptedSharedPref()?.getString(LOCAL_EVENT_KEY, "")
                        ?: ""
                if (listString.isNotEmpty()) {
                    events.clear()
                    deserializeLocalEvents(listString)
                } else if (events.isNotEmpty()) {
                    // retain persistent event for user with no stored data
                    events.removeAll { ev -> !ev.isPersistentType() }
                }
            }
        }

        private fun deserializeLocalEvents(listString: String) {
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
                if (event != null) {
                    events.add(event)
                }
            }
        }

        private fun saveUpdatedList() {
            // check if caching is enabled to update persistent data
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                // reset message list from cached using updated user info
                InAppMessaging.instance().getEncryptedSharedPref()?.edit()?.putString(LOCAL_EVENT_KEY,
                        Gson().toJson(events))?.apply()
            }
        }
    }
}
