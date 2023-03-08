package com.rakuten.tech.mobile.inappmessaging.runtime

import org.amshove.kluent.shouldBeEmpty
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserInfoProviderSpec {
    class TestDefaultProvider : UserInfoProvider
    val provider: UserInfoProvider = TestDefaultProvider()

    @Test
    fun `default access token should be empty`() {
        provider.provideAccessToken()?.shouldBeEmpty()
    }

    @Test
    fun `default userId should be empty`() {
        provider.provideUserId()?.shouldBeEmpty()
    }

    @Test
    fun `default IdTracking should be empty`() {
        provider.provideIdTrackingIdentifier()?.shouldBeEmpty()
    }
}
