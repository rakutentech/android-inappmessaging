package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.EventTrackerHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.RmcHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository

internal enum class AnalyticsEvent(val iamName: String?, val rmcName: String?) {
    IMPRESSION("_rem_iam_impressions", "_rem_rmc_iam_impressions"),
    PUSH_PRIMER(null, "_rem_rmc_iam_pushprimer"),
}

internal enum class AnalyticsKey(val key: String) {
    EVENT_NAME("eventName"),
    CUSTOM_ATTRIBUTES("customAttributes"),
    CAMPAIGN_ID("campaign_id"),
    SUBS_ID("subscription_id"),
    DEVICE_ID("device_id"),
    TIMESTAMP("timestamp"),
    CUSTOM_PARAM("cp"),
    ACCOUNT("acc"),
    APP_ID("aid"),
    IMPRESSIONS("impressions"),
    PUSH_PERMISSION("push_permission"),
}

internal object AnalyticsManager {

    /**
     * Sends the event [analyticsEvent]. For RMC, the event is sent to both the host app account and SDK account.
     * This method attaches common metadata to the event such as Device Id etc., so only add custom information that is
     * specific to the event on [data].
     */
    @SuppressWarnings("LongMethod")
    @JvmStatic
    fun sendEvent(analyticsEvent: AnalyticsEvent, campaignId: String, data: MutableMap<String, Any>) {
        // Common metadata
        data[AnalyticsKey.CAMPAIGN_ID.key] = campaignId
        data[AnalyticsKey.SUBS_ID.key] = HostAppInfoRepository.instance().getSubscriptionKey()
        data[AnalyticsKey.DEVICE_ID.key] = HostAppInfoRepository.instance().getDeviceId()

        val params = hashMapOf<String, Any>()
        params[AnalyticsKey.CUSTOM_PARAM.key] = data

        if (RmcHelper.isRmcIntegrated()) {
            if (analyticsEvent.rmcName == null) {
                return
            }

            val paramsCopy = HashMap<String, Any>(params)
            paramsCopy[AnalyticsKey.ACCOUNT.key] = BuildConfig.IAM_RAT_ACC
            paramsCopy[AnalyticsKey.APP_ID.key] = BuildConfig.IAM_RAT_AID

            EventTrackerHelper.sendEvent(analyticsEvent.rmcName, params)
            EventTrackerHelper.sendEvent(analyticsEvent.rmcName, paramsCopy)
        } else {
            if (analyticsEvent.iamName == null) {
                return
            }

            EventTrackerHelper.sendEvent(analyticsEvent.iamName, params)
        }
    }
}
