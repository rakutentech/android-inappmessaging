package com.rakuten.tech.mobile.inappmessaging.runtime

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InAppMessagingRmcSpec {

    private lateinit var context: Context
    private lateinit var iamSpy: InAppMessaging.Companion
    private lateinit var mockRmcHelper: MockedStatic<RmcHelper>

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        iamSpy = spy(InAppMessaging)
        mockRmcHelper = mockStatic(RmcHelper::class.java)
    }

    @After
    fun tearDown() {
        mockRmcHelper.close()
    }

    @Test
    fun `should ignore configure if call (with runtime params) is made from app using RMC SDK`() {
        mockRmcHelper.`when`<Any> { RmcHelper.isUsingRmc() }.thenReturn(true)

        iamSpy.configure(
            context,
            "test-subs-key",
            "test-config-url",
        )

        verifyInitializedCalled(0)
    }

    @Test
    fun `should ignore configure if call (with default params) is made from app using RMC SDK`() {
        mockRmcHelper.`when`<Any> { RmcHelper.isUsingRmc() }.thenReturn(true)

        iamSpy.configure(context)

        verifyInitializedCalled(0)
    }

    @Test
    fun `should process configure if call is made from RMC SDK`() {
        mockRmcHelper.`when`<Any> { RmcHelper.isUsingRmc() }.thenReturn(true)

        // Simulate call from RMC by adding prefix
        iamSpy.configure(
            context,
            "${RmcHelper.RMC_PREFIX}test-subs-key",
            "test-config-url",
        )

        verifyInitializedCalled(1)
    }

    @Test
    fun `should process configure if call is made from app not using RMC SDK`() {
        mockRmcHelper.`when`<Any> { RmcHelper.isUsingRmc() }.thenReturn(false)

        iamSpy.configure(
            context,
            "test-subs-key",
            "test-config-url",
        )

        verifyInitializedCalled(1)
    }

    private fun verifyInitializedCalled(invocations: Int) {
        verify(iamSpy, times(invocations)).initialize(
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
        )
    }
}
