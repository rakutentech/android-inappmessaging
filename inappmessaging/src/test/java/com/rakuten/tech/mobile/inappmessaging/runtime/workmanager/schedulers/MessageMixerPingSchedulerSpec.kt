package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import androidx.test.core.app.ApplicationProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.times
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
import org.mockito.Mockito
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
        When calling mockWorkManager.enqueueUniqueWork(
                any(), any(), any(OneTimeWorkRequest::class)) itThrows IllegalStateException("test")
    }

    @Test
    fun `should not throw exception`() {
        When calling configResponseData.rollOutPercentage itReturns 0
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        MessageMixerPingScheduler.instance().pingMessageMixerService(10L)
    }

    @Test
    fun `should reset delay when max is encountered`() {
        InAppMessaging.init(ApplicationProvider.getApplicationContext()).shouldBeTrue()
        When calling configResponseData.rollOutPercentage itReturns 100
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        MessageMixerPingScheduler.currDelay *= 2
        MessageMixerPingScheduler.instance().pingMessageMixerService(Long.MAX_VALUE - System.currentTimeMillis() + 2)
        MessageMixerPingScheduler.currDelay shouldBeEqualTo RetryDelayUtil.INITIAL_BACKOFF_DELAY
    }

    @Test
    fun `should not throw exception with callback`() {
        val function: (ex: Exception) -> Unit = {}
        val mockCallback = Mockito.mock(function.javaClass)

        When calling configResponseData.rollOutPercentage itReturns 100
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), mockCallback)
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        MessageMixerPingScheduler.instance().pingMessageMixerService(10L, mockWorkManager)

        Mockito.verify(mockCallback, atLeastOnce()).invoke(any(InAppMessagingException::class))
    }
}
