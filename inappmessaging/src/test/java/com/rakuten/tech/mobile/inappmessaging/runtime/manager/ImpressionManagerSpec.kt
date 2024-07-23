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
import org.junit.runner.RunWith
import org.mockito.Mockito.mockStatic
import org.robolectric.RobolectricTestRunner
import java.util.Date

/**
 * Test class for ImpressionManager.
 */
@RunWith(RobolectricTestRunner::class)
class ImpressionManagerSpec : BaseTest() {

    private val analyticsManager = mockStatic(AnalyticsManager::class.java)

    @Before
    override fun setup() {
        super.setup()
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

        val captor = argumentCaptor<MutableMap<String, Any>>()

        analyticsManager.verify { AnalyticsManager.sendEvent(any(), any(), captor.capture()) }
        captor.firstValue.keys.shouldContain(AnalyticsKey.IMPRESSIONS.key)

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
