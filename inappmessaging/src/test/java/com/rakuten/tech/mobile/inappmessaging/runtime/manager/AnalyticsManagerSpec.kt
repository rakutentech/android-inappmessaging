package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.rakuten.tech.mobile.inappmessaging.runtime.EventTrackerHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.RmcHelper
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldContainAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mockStatic

@SuppressWarnings("LongMethod")
class AnalyticsManagerSpec {

    private val eventTrackerHelper = mockStatic(EventTrackerHelper::class.java)
    private val rmcHelper = mockStatic(RmcHelper::class.java)

    @Before
    fun setup() {
        rmcHelper.`when`<Any> { RmcHelper.isRmcIntegrated() }.thenReturn(true)
    }

    @After
    fun teardown() {
        eventTrackerHelper.close()
        rmcHelper.close()
    }

    @Test
    fun `should not process if there is no corresponding event name`() {
        rmcHelper.`when`<Any> { RmcHelper.isRmcIntegrated() }.thenReturn(false)

        AnalyticsManager.sendEvent(AnalyticsEvent.PUSH_PRIMER, "1234", mutableMapOf("param1" to 1))

        eventTrackerHelper.verify({ EventTrackerHelper.sendEvent(any(), any()) }, never())
    }

    @Test
    fun `should set all required custom parameter (cp)`() {
        rmcHelper.`when`<Any> { RmcHelper.isRmcIntegrated() }.thenReturn(false)

        AnalyticsManager.sendEvent(AnalyticsEvent.IMPRESSION, "1234", mutableMapOf("param1" to 1))

        val captor = argumentCaptor<Map<String, Any>>()
        eventTrackerHelper.verify { EventTrackerHelper.sendEvent(any(), captor.capture()) }
        captor.firstValue.keys.shouldContain(AnalyticsKey.CUSTOM_PARAM.key)
        val params = (captor.firstValue[AnalyticsKey.CUSTOM_PARAM.key] as Map<String, Any>).keys
        params.shouldContainAll(
            arrayOf(
                AnalyticsKey.CAMPAIGN_ID.key,
                AnalyticsKey.SUBS_ID.key,
                AnalyticsKey.DEVICE_ID.key,
                "param1",
            ),
        )
    }

    @Test
    fun `should set all required custom parameter (cp) for RMC`() {
        AnalyticsManager.sendEvent(AnalyticsEvent.PUSH_PRIMER, "1234", mutableMapOf("param1" to 1))

        val captor = argumentCaptor<Map<String, Any>>()
        eventTrackerHelper.verify({ EventTrackerHelper.sendEvent(any(), captor.capture()) }, times(2))
        captor.firstValue.keys.shouldContain(AnalyticsKey.CUSTOM_PARAM.key)
        captor.secondValue.keys.shouldContainAll(
            arrayOf(
                AnalyticsKey.CUSTOM_PARAM.key,
                AnalyticsKey.ACCOUNT.key,
                AnalyticsKey.APP_ID.key,
            ),
        )

        val params = (captor.firstValue[AnalyticsKey.CUSTOM_PARAM.key] as Map<String, Any>).keys
        params.shouldContainAll(
            arrayOf(
                AnalyticsKey.CAMPAIGN_ID.key,
                AnalyticsKey.SUBS_ID.key,
                AnalyticsKey.DEVICE_ID.key,
                "param1",
            ),
        )
    }
}
