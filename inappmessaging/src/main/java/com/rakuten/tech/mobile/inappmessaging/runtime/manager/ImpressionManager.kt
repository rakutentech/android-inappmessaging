package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.rat.RatImpression
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.Impression
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.ImpressionScheduler
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.collections.set

/**
 * ImpressionManager dispatches works accordingly. For instance, reporting impressions to IAM
 * backend, or RAT.
 */
internal object ImpressionManager {
    private const val TAG = "IAM_ImpressionManager"
    internal val impressionMap by lazy { ConcurrentHashMap<String, Impression>() }

    /**
     * Reporting impression list to IAM backend, and sending to analytics. This method is invoked on
     * main thread.
     */
    fun scheduleReportImpression(impressionList: List<Impression>, campaignId: String, isTestMessage: Boolean) {
        if (impressionList.isEmpty()) return

        // send user action impression
        sendImpressionEvent(campaignId, impressionList)

        val impListRequest = impressionList.toMutableList()
        impressionMap[campaignId]?.let { mapData ->
            impListRequest.add(mapData)
        }

        // Assemble ImpressionRequest object.
        val impressionRequest = ImpressionRequest(
            campaignId = campaignId,
            isTest = isTestMessage,
            appVersion = HostAppInfoRepository.instance().getVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            userIdentifiers = RuntimeUtil.getUserIdentifiers(),
            impressions = impListRequest,
            rmcSdkVersion = HostAppInfoRepository.instance().getRmcSdkVersion(),
        )

        // Schedule work to report impressions back to IAM backend.
        ImpressionScheduler().startImpressionWorker(impressionRequest)
    }

    internal fun sendImpressionEvent(
        campaignId: String,
        impressionList: List<Impression>,
        impressionTypeOnly: Boolean = false,
    ) {
        if (impressionList.isEmpty()) return

        if (impressionTypeOnly) {
            impressionMap[campaignId] = impressionList[0] // if impression type only, it is assumed that only one entry
        }

        val params = hashMapOf<String, Any>()
        params[AnalyticsKey.IMPRESSIONS.key] = createRatImpressionList(impressionList)
        AnalyticsManager.sendEvent(AnalyticsEvent.IMPRESSION, campaignId, params)
    }

    /**
     * This method creates a list of impressions based on input arguments.
     * Throws IllegalArgumentException if impressionTypes is IMPRESSION or INVALID.
     */
    fun createImpressionList(impressionTypes: List<ImpressionType>): List<Impression> {
        // Create Impression objects list.

        val impressionList = ArrayList<Impression>()

        val currentTimeInMillis = Date().time

        // Add impressions included in the method argument.
        for (impressionType in impressionTypes) {
            // If impressionType is Impression or Invalid, there's something wrong, do not proceed.
            if (ImpressionType.IMPRESSION == impressionType || ImpressionType.INVALID == impressionType) {
                return ArrayList()
            }

            impressionList.add(Impression(impressionType, currentTimeInMillis))
            InAppLogger(TAG).debug("impression %s, time: %d", impressionType.name, currentTimeInMillis)
        }
        return impressionList
    }

    /**
     * This method returns a list which contains all impressions.
     */
    private fun createRatImpressionList(impressionList: List<Impression>): List<RatImpression> {
        // Adding all impressions to a list of RatImpression objects.
        val ratImpressionList = ArrayList<RatImpression>()
        for (impression in impressionList) {
            val impressionType = ImpressionType.getById(impression.type)
            if (impressionType != null) {
                ratImpressionList.add(RatImpression(impressionType.typeId, impression.timestamp))
            }
        }

        return ratImpressionList
    }
}
