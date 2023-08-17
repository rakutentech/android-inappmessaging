package com.rakuten.tech.mobile.inappmessaging.runtime

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InAppMessagingRmcSpec {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockRmcHelper = mockStatic(RmcHelper::class.java)
    private val iamSpy = spy(InAppMessaging)

    @After
    fun tearDown() {
        mockRmcHelper.close()
    }

    @Test
    fun `should ignore configure if call (with runtime params) is made from app using RMC SDK`() {
        mockRmcHelper.`when`<Any> { RmcHelper.isRmcIntegrated(context) }.thenReturn(true)

        iamSpy.configure(
            context,
            TEST_SUBS_KEY,
            TEST_CONFIG_URL,
        )

        verifyInitializedCalled(0)
    }

    @Test
    fun `should ignore configure if call (with default params) is made from app using RMC SDK`() {
        mockRmcHelper.`when`<Any> { RmcHelper.isRmcIntegrated(context) }.thenReturn(true)

        iamSpy.configure(context)

        verifyInitializedCalled(0)
    }

    @Test
    fun `should process configure if call is made from RMC SDK`() {
        mockRmcHelper.`when`<Any> { RmcHelper.isRmcIntegrated(context) }.thenReturn(true)

        iamSpy.configure(
            context,
            "$TEST_SUBS_KEY${RmcHelper.RMC_SUFFIX}", // simulate call from RMC by adding suffix
            TEST_CONFIG_URL,
        )

        verifyInitializedCalled(1)
    }

    @Test
    fun `should process configure if call is made from app not using RMC SDK`() {
        mockRmcHelper.`when`<Any> { RmcHelper.isRmcIntegrated(context) }.thenReturn(false)

        iamSpy.configure(
            context,
            TEST_SUBS_KEY,
            TEST_CONFIG_URL,
        )

        verifyInitializedCalled(1)
    }

    private fun verifyInitializedCalled(invocations: Int) {
        verify(iamSpy, times(invocations)).initialize(
            context,
            true,
            TEST_SUBS_KEY, // verify any suffix is removed
            TEST_CONFIG_URL,
            false,
        )
    }

    companion object {
        private const val TEST_SUBS_KEY = "test-subs-key"
        private const val TEST_CONFIG_URL = "test-config-url"
    }
}
