package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.EventTrackerHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.RAT_EVENT_CAMP_ID
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.RAT_EVENT_KEY_PERMISSION
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.RAT_EVENT_KEY_PRIMER
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.Companion.RAT_EVENT_SUBS_ID
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PushPrimerTrackerManagerSpec : BaseTest() {
    private val eventTracker = Mockito.mock(EventTrackerHelper::class.java)
    private val captor = argumentCaptor<Map<String, Any>>()

    @Before
    override fun setup() {
        super.setup()

        PushPrimerTrackerManager.campaignId = "test"
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
        PushPrimerTrackerManager.sendPrimerEvent(result, eventTracker::sendEvent)

        verify(eventTracker).sendEvent(eq(RAT_EVENT_KEY_PRIMER), captor.capture())

        captor.firstValue[RAT_EVENT_CAMP_ID] shouldBeEqualTo campaignId
        captor.firstValue[RAT_EVENT_SUBS_ID] shouldBeEqualTo HostAppInfoRepository.instance().getSubscriptionKey()
        captor.firstValue[RAT_EVENT_KEY_PERMISSION] shouldBeEqualTo result

        PushPrimerTrackerManager.campaignId.shouldBeEmpty()
    }
}
