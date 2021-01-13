package com.rakuten.tech.mobile.inappmessaging.runtime.api

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class MessageMixerRetrofitServiceSpec : BaseTest() {
    @Test
    fun `should RAE token header spelled with base attributes`() {
        MessageMixerRetrofitService.RAE_TOKEN_HEADER shouldBeEqualTo "Authorization"
    }

    @Test
    fun `should device header spelled with base attributes`() {
        MessageMixerRetrofitService.DEVICE_ID_HEADER shouldBeEqualTo "device_id"
    }

    @Test
    fun `should subscription ID header spelled with base attributes`() {
        MessageMixerRetrofitService.SUBSCRIPTION_ID_HEADER shouldBeEqualTo "Subscription-Id"
    }
}
