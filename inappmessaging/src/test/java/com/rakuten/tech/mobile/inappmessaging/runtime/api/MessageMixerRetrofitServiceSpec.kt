package com.rakuten.tech.mobile.inappmessaging.runtime.api

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.DEVICE_ID_HEADER
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.SUBSCRIPTION_ID_HEADER
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class MessageMixerRetrofitServiceSpec : BaseTest() {
    @Test
    fun `should access token header spelled with base attributes`() {
        MessageMixerRetrofitService.ACCESS_TOKEN_HEADER shouldBeEqualTo "Authorization"
    }

    @Test
    fun `should device header spelled with base attributes`() {
        DEVICE_ID_HEADER shouldBeEqualTo "device_id"
    }

    @Test
    fun `should subscription ID header spelled with base attributes`() {
        SUBSCRIPTION_ID_HEADER shouldBeEqualTo "Subscription-Id"
    }
}
