package com.rakuten.tech.mobile.inappmessaging.runtime

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract.Intents.Insert.ACTION
import android.text.TextUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.lang.ref.WeakReference

/**
 * Renamed from EventBroadcaster, only for test.
 *
 * <p>Utility that allow to send broadcasts to analytics module for event tracking. If the Analytics module version
 * 4.7.0 or newer is installed (i.e. included as compile or implementation dependency of the app)
 * the event will be processed. If there is no compatible analytics module is present nothing
 * happens. To request the Analytics module to track an event use {@link #sendEvent(Context, String,
 * Map)}. Events with names that have a `_` (underscore) as prefix will be processed by the RAT
 * tracker & parameters will be sent as `cp` data (as of 4.7.0).
 */
internal object LegacyEventBroadcasterHelper {
    private var context: WeakReference<Context>? = null

    /**
     * This method sends event data to Analytics module for processing.
     * If the object's context has not been garbage collected this will delegate the actual
     * broadcasting to [.sendEvent]. Otherwise nothing happens.
     */
    fun setContext(context: Context) {
        this.context = WeakReference(context)
    }

    /**
     * This method sends event data to Analytics module for processing.
     * If the app does not bundle a compatible, Analytics module the broadcast will still be sent,
     * but not processed.
     */
    fun sendEvent(eventName: String, data: Map<String, *>?) {
        if (this.context == null) {
            return
        }
        val ctx = this.context?.get()
        if (TextUtils.isEmpty(eventName) || ctx == null) {
            return
        }

        val serializableData: HashMap<String, *>? = when {
            data is HashMap<*, *> -> data as HashMap<String, *>?
            data != null -> HashMap(data)
            else -> null
        }

        val intent = Intent(ACTION)
        intent.putExtra("event-name", eventName)
        intent.putExtra("event-data", serializableData)

        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent)
    }
}
