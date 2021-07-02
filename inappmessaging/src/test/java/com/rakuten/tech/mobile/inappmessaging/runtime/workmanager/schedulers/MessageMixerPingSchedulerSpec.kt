package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import androidx.test.core.app.ApplicationProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import org.amshove.kluent.*
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
        MessageMixerPingScheduler.currDelay*=2
        MessageMixerPingScheduler.instance().pingMessageMixerService(Long.MAX_VALUE - System.currentTimeMillis() + 2)
        MessageMixerPingScheduler.currDelay shouldBeEqualTo RetryDelayUtil.INITIAL_BACKOFF_DELAY
    }
}
