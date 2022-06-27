package com.rakuten.tech.mobile.inappmessaging.runtime

import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger

internal object EventTrackerHelper {

    private const val TAG = "EventTrackerHelper"

    /**
     * This method sends event data to Analytics module for processing.
     * This method will only send the analytics event when the real Event class exists at
     * runtime.
     * The Analytics SDK should be depended upon by the App, so in that case Analytics events will
     * get sent by this function, but otherwise they will be ignored.
     * @param eventName The given Event's name to be tracked.
     * @param data the given Event parameters to be tracked.
     * @return true if the analytics event has been sent, false otherwise.
     */
    fun sendEvent(eventName: String, data: Map<String, *>?): Boolean {
        if (!TextUtils.isEmpty(eventName)) {
            val serializableData: HashMap<String, *> = when (data) {
                null -> hashMapOf<String, Any>()
                is HashMap<String, *> -> data
                else -> HashMap(data)
            }

            if (hasClass("com.rakuten.tech.mobile.analytics.Event")) {
                com.rakuten.tech.mobile.analytics.Event(eventName, serializableData).track()
                return true
            }
        }

        return false
    }

    @VisibleForTesting
    internal fun hasClass(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            InAppLogger(TAG).info(e.message)
            false
        }
    }
}
