package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.never
import com.rakuten.tech.mobile.inappmessaging.runtime.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.Impression
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mockStatic
import java.util.Date

/**
 * Test class for ImpressionManager.
 */
class ImpressionManagerSpec {

    private val analyticsManager = mockStatic(AnalyticsManager::class.java)

    @Before
    fun setup() {
        impressionList = ImpressionManager.createImpressionList(VALID_IMPRESSION_TYPES)
    }

    @After
    fun teardown() {
        analyticsManager.close()
    }

    @Test
    fun `should create impression list with correct attributes`() {
        impressionList!![0].timestamp shouldBeGreaterThan 0L
        impressionList!![0].type shouldBeEqualTo ImpressionType.ACTION_ONE.typeId
    }

    @Test
    fun `should return empty when create impression list with wrong arg`() {
        ImpressionManager.createImpressionList(IMPRESSION_TYPES).isEmpty()
        ImpressionManager.createImpressionList(INVALID_TYPES).isEmpty()
    }

    @Test
    fun `should not track empty impression list`() {
        ImpressionManager.scheduleReportImpression(emptyList(), "1234", false)

        analyticsManager.verify({ AnalyticsManager.sendEvent(any(), any(), any()) }, never())
    }

    @Test
    fun `should track valid impression event`() {
        ImpressionManager.impressionMap["1234"] = Impression(ImpressionType.IMPRESSION, Date().time)
        ImpressionManager.scheduleReportImpression(impressionList!!, "1234", false)

        val eTypeCaptor = argumentCaptor<AnalyticsEvent>()
        val idCaptor = argumentCaptor<String>()
        val dataCaptor = argumentCaptor<MutableMap<String, Any>>()

        analyticsManager.verify {
            AnalyticsManager.sendEvent(eTypeCaptor.capture(), idCaptor.capture(), dataCaptor.capture())
        }
        eTypeCaptor.firstValue shouldBeEqualTo AnalyticsEvent.IMPRESSION
        idCaptor.firstValue shouldBeEqualTo "1234"
        dataCaptor.firstValue.keys.shouldContain(AnalyticsKey.IMPRESSIONS.key)

        ImpressionManager.impressionMap.clear()
    }

    companion object {
        private val VALID_IMPRESSION_TYPES: MutableList<ImpressionType> =
            mutableListOf(ImpressionType.ACTION_ONE, ImpressionType.OPT_OUT)
        private val IMPRESSION_TYPES: MutableList<ImpressionType> =
            mutableListOf(ImpressionType.ACTION_ONE, ImpressionType.IMPRESSION)
        private val INVALID_TYPES: MutableList<ImpressionType> =
            mutableListOf(ImpressionType.ACTION_ONE, ImpressionType.INVALID)
        private var impressionList: List<Impression>? = null
    }
}
