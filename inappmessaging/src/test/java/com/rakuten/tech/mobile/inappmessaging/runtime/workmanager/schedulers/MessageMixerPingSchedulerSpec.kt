package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import androidx.test.core.app.ApplicationProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

/**
 * Test for class: MessageMixerPingScheduler.
 */
@RunWith(RobolectricTestRunner::class)
class MessageMixerPingSchedulerSpec : BaseTest() {
    private val configResponseData = Mockito.mock(ConfigResponseData::class.java)
    private val mockWorkManager = Mockito.mock(WorkManager::class.java)

    @Before
    override fun setup() {
        super.setup()
        `when`(
            mockWorkManager.enqueueUniqueWork(
                any(), any(),
                ArgumentMatchers.any(OneTimeWorkRequest::class.java),
            ),
        ).thenThrow(IllegalStateException("test"))
    }

    @Test
    fun `should not throw exception`() {
        `when`(configResponseData.rollOutPercentage).thenReturn(0)
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        MessageMixerPingScheduler.instance().pingMessageMixerService(10L)
    }

    @Test
    fun `should reset delay when max is encountered`() {
        InAppMessaging.configure(ApplicationProvider.getApplicationContext()).shouldBeTrue()
        `when`(configResponseData.rollOutPercentage).thenReturn(100)
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        MessageMixerPingScheduler.currDelay *= 2
        MessageMixerPingScheduler.instance().pingMessageMixerService(Long.MAX_VALUE - System.currentTimeMillis() + 2)
        MessageMixerPingScheduler.currDelay shouldBeEqualTo RetryDelayUtil.INITIAL_BACKOFF_DELAY
    }

    @Test
    fun `should not throw exception with callback`() {
        val function: (ex: Exception) -> Unit = {}
        val mockCallback = Mockito.mock(function.javaClass)

        `when`(configResponseData.rollOutPercentage).thenReturn(100)
        InAppMessaging.errorCallback = mockCallback
        InAppMessaging.configure(ApplicationProvider.getApplicationContext())
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        MessageMixerPingScheduler.instance().pingMessageMixerService(10L, mockWorkManager)

        val captor = argumentCaptor<InAppMessagingException>()
        Mockito.verify(mockCallback, atLeastOnce()).invoke(captor.capture())
        captor.firstValue shouldBeInstanceOf InAppMessagingException::class.java
        InAppMessaging.errorCallback = null
    }
}
