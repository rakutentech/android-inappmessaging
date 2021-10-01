package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.LegacyEventTrackerHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Impression
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.rat.RatImpression
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.ImpressionScheduler
import timber.log.Timber
import java.util.Collections
import java.util.Date
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

/**
 * ImpressionManager dispatches works accordingly. For instance, reporting impressions to IAM
 * backend, or RAT.
 */
internal class ImpressionManager {

    /**
     * Reporting impression list to IAM backend, and broadcasting to RAT. This method is invoked on
     * main thread.
     */
    fun scheduleReportImpression(
        impressionList: List<Impression>,
        campaignId: String,
        isTestMessage: Boolean,
        sendEvent: (String, data: Map<String, *>?) -> Boolean = LegacyEventTrackerHelper::sendEvent
    ) {
        if (impressionList.isEmpty()) return
        // Assemble ImpressionRequest object.
        val impressionRequest = ImpressionRequest(
                campaignId = campaignId,
                isTest = isTestMessage,
                appVersion = HostAppInfoRepository.instance().getVersion(),
                sdkVersion = BuildConfig.VERSION_NAME,
                userIdentifiers = RuntimeUtil.getUserIdentifiers(),
                impressions = impressionList)
        // Broadcasting impressions to RAT.
        sendEvent(
                InAppMessagingConstants.RAT_EVENT_KEY_IMPRESSION,
                createImpressionMap(impressionRequest.impressions))
        // Schedule work to report impressions back to IAM backend.
        ImpressionScheduler().startImpressionWorker(impressionRequest)
    }

    /**
     * This method creates a list of impressions based on input arguments.
     * Throws IllegalArgumentException if impressionTypes is IMPRESSION or INVALID.
     */
    fun createImpressionList(impressionTypes: List<ImpressionType>): List<Impression> {
        // Create Impression objects list.

        val impressionList = ArrayList<Impression>()

        // Adding view impression by default.
        val currentTimeInMillis = Date().time
        impressionList.add(Impression(ImpressionType.IMPRESSION, currentTimeInMillis))

        // Add impressions included in the method argument.
        for (impressionType in impressionTypes) {

            // If impressionType is Impression or Invalid, there's something wrong, do not proceed.
            if (ImpressionType.IMPRESSION == impressionType || ImpressionType.INVALID == impressionType) {
                return ArrayList()
            }

            impressionList.add(Impression(impressionType, currentTimeInMillis))
            Timber.tag(TAG).d("impression %s, time: %d", impressionType.name, currentTimeInMillis)
        }
        return impressionList
    }

    /**
     * This method returns a map which contains all impreassions in the argument list.
     */
    private fun createImpressionMap(impressionList: List<Impression>): Map<String, List<RatImpression>> {
        // Adding all impressions to a list of RatImpression objects.
        val ratImpressionList = ArrayList<RatImpression>()
        for (impression in impressionList) {
            val impressionType = ImpressionType.getById(impression.type)
            if (impressionType != null) {
                ratImpressionList.add(RatImpression(impressionType, impression.timestamp))
            }
        }
        // Adding impression list to an unmodifiable map.
        val ratMap = HashMap<String, List<RatImpression>>()
        ratMap[InAppMessagingConstants.RAT_EVENT_KEY_IMPRESSION_VALUE] = ratImpressionList
        return Collections.unmodifiableMap(ratMap)
    }

    companion object {
        private const val TAG = "IAM_ImpressionManager"
    }
}
