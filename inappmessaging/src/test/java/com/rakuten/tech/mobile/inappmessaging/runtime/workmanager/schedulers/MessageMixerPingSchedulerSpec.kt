package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Test
import org.mockito.Mockito

/**
 * Test for class: MessageMixerPingScheduler.
 */
class MessageMixerPingSchedulerSpec : BaseTest() {
    private val configResponseData = Mockito.mock(ConfigResponseData::class.java)

    @Test
    fun `should not throw exception`() {
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        When calling configResponseData.rollOutPercentage itReturns 0
        MessageMixerPingScheduler.instance().pingMessageMixerService(10L)
    }
}
