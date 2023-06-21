package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.rakuten.tech.mobile.inappmessaging.runtime.EventTrackerHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants

internal object PushPrimerTrackerManager {
    internal var campaignId = ""

    internal fun sendPrimerEvent(
        permission: Int,
        sendEvent: (String, data: Map<String, *>?) -> Boolean = EventTrackerHelper::sendEvent,
    ) {
        val params: MutableMap<String, Any?> = HashMap()
        params[InAppMessagingConstants.RAT_EVENT_CAMP_ID] = campaignId
        params[InAppMessagingConstants.RAT_EVENT_SUBS_ID] = HostAppInfoRepository.instance().getSubscriptionKey()
        params[InAppMessagingConstants.RAT_EVENT_KEY_PERMISSION] = permission
        params[InAppMessagingConstants.RAT_EVENT_KEY_DEVICE_ID] = HostAppInfoRepository.instance().getDeviceId()

        sendEvent(InAppMessagingConstants.RAT_EVENT_KEY_PRIMER, params)

        campaignId = "" // reset value
    }
}
