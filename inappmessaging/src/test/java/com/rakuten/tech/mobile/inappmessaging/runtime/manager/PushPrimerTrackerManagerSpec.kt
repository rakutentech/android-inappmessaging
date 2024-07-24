package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.nhaarman.mockitokotlin2.argumentCaptor
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mockStatic

class PushPrimerTrackerManagerSpec {

    private val analyticsManager = mockStatic(AnalyticsManager::class.java)

    @Before
    fun setup() {
        PushPrimerTrackerManager.campaignId = "test"
    }

    @After
    fun teardown() {
        analyticsManager.close()
    }

    @Test
    fun `should send event with granted permission`() {
        verifyTracker(1)
    }

    @Test
    fun `should send event with denied permission`() {
        verifyTracker(0)
    }

    @Test
    fun `should send event with granted permission and empty campaign`() {
        PushPrimerTrackerManager.campaignId = ""
        verifyTracker(1, "")
    }

    @Test
    fun `should send event with denied permission and empty campaign`() {
        PushPrimerTrackerManager.campaignId = ""
        verifyTracker(0, "")
    }

    private fun verifyTracker(result: Int, campaignId: String = "test") {
        PushPrimerTrackerManager.sendPrimerEvent(result)

        val eTypeCaptor = argumentCaptor<AnalyticsEvent>()
        val idCaptor = argumentCaptor<String>()
        val dataCaptor = argumentCaptor<MutableMap<String, Any>>()

        analyticsManager.verify {
            AnalyticsManager.sendEvent(eTypeCaptor.capture(), idCaptor.capture(), dataCaptor.capture())
        }

        eTypeCaptor.firstValue shouldBeEqualTo AnalyticsEvent.PUSH_PRIMER
        idCaptor.firstValue shouldBeEqualTo campaignId
        dataCaptor.firstValue[AnalyticsKey.PUSH_PERMISSION.key] shouldBeEqualTo result

        PushPrimerTrackerManager.campaignId.shouldBeEmpty()
    }
}
