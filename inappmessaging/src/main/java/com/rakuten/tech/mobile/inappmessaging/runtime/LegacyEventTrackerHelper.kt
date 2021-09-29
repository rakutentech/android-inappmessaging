package com.rakuten.tech.mobile.inappmessaging.runtime

import android.text.TextUtils

/**
 * <p>Utility that allow to send events to analytics module for event tracking. If the Analytics module version
 * 4.7.0 or newer is installed (i.e. included as compile or implementation dependency of the app)
 * the event will be processed. If there is no compatible analytics module is present nothing
 * happens. To request the Analytics module to track an event use {@link #sendEvent(String,
 * Map)}. Events with names that have a `_` (underscore) as prefix will be processed by the RAT
 * tracker & parameters will be sent as `cp` data (as of 4.7.0).
 */
internal object LegacyEventTrackerHelper {

    /**
     * This method sends event data to Analytics module for processing.
     * This method will only send the analytics event when the real Event class exists at
     * runtime.
     * The Analytics SDK should be depended upon by the App, so in that case Analytics events will
     * get sent by this function, but otherwise they will be ignored.
    */
    fun sendEvent(eventName: String, data: Map<String, *>?) {

        if (TextUtils.isEmpty(eventName)) {
            return
        }

        val serializableData: HashMap<String, *> = when (data) {
            null -> hashMapOf<String, Any>()
            is HashMap<*, *> -> data as HashMap<String, *>
            else -> HashMap(data)
        }

        if (hasClass("com.rakuten.tech.mobile.analytics.Event")) {
            com.rakuten.tech.mobile.analytics.Event("rat.$eventName", serializableData).track()
        }
    }

    private fun hasClass(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}