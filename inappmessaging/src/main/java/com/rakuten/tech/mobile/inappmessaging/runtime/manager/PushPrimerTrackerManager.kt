package com.rakuten.tech.mobile.inappmessaging.runtime.manager

internal object PushPrimerTrackerManager {
    internal var campaignId = ""

    internal fun sendPrimerEvent(permission: Int) {
        val params: MutableMap<String, Any> = HashMap()
        params[AnalyticsKey.PUSH_PERMISSION.key] = permission

        AnalyticsManager.sendEvent(AnalyticsEvent.PUSH_PRIMER, campaignId, params)

        campaignId = "" // reset value
    }
}
