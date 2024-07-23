package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.rakuten.tech.mobile.inappmessaging.runtime.EventTrackerHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.RmcHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository

/**
 * @param name1 Standalone event name
 * @param name2 RMC event name
 */
internal enum class AnalyticsEvent(val name1: String?, val name2: String?) {
    IMPRESSION("_rem_iam_impressions", "_rem_rmc_iam_impressions"),
    PUSH_PRIMER(null, "_rem_rmc_iam_pushprimer")
}

internal enum class AnalyticsKey(val key: String) {
    EVENT_NAME("eventName"),
    CUSTOM_ATTRIBUTES("customAttributes"),
    CAMPAIGN_ID("campaign_id"),
    SUBS_ID("subscription_id"),
    DEVICE_ID("device_id"),
    TIMESTAMP("timestamp"),
    CUSTOM_PARAM("cp"),
    ACCOUNT_ID("acc"),
    APP_ID("aid"),
    IMPRESSIONS("impressions"),
    PUSH_PERMISSION("push_permission")
}

internal object AnalyticsManager {

    /**
     * Sends the event [analyticsEvent]. For RMC, the event is sent to both the host app and SDK accounts.
     * This method attaches common metadata to the event such as Device Id etc., so only add custom information that is
     * specific to the event on [data].
     */
    fun sendEvent(analyticsEvent: AnalyticsEvent,
                  campaignId: String,
                  data: MutableMap<String, Any>) {

        // Common metadata
        data[AnalyticsKey.CAMPAIGN_ID.key] = campaignId
        data[AnalyticsKey.SUBS_ID.key] = HostAppInfoRepository.instance().getSubscriptionKey()
        data[AnalyticsKey.DEVICE_ID.key] = HostAppInfoRepository.instance().getDeviceId()

        val params = hashMapOf<String, Any>()
        params[AnalyticsKey.CUSTOM_PARAM.key] = data

        if (RmcHelper.isRmcIntegrated()) {
            if (analyticsEvent.name2 == null)
                return

            EventTrackerHelper.sendEvent(analyticsEvent.name2, params)

            val (ratAcc, ratAid) = Pair(RmcHelper.rmcRatAcc, RmcHelper.rmcRatAid)
            if (ratAcc == null || ratAid == null)
                return

            val paramsCopy = HashMap<String, Any>(params)
            paramsCopy[AnalyticsKey.ACCOUNT_ID.key] = 888
            paramsCopy[AnalyticsKey.APP_ID.key] = 2
            EventTrackerHelper.sendEvent(analyticsEvent.name2, paramsCopy)
        } else {
            if (analyticsEvent.name1 == null)
                return

            EventTrackerHelper.sendEvent(analyticsEvent.name1, data)
        }
    }
}