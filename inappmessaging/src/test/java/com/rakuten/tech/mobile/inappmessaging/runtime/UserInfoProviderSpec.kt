package com.rakuten.tech.mobile.inappmessaging.runtime

import org.amshove.kluent.shouldBeEmpty
import org.junit.Test

class UserInfoProviderSpec {
    // Default interface
    private class TestDefaultProvider : UserInfoProvider

    @Test
    fun `default access token should be empty`() {
        TestDefaultProvider().provideAccessToken()?.shouldBeEmpty()
    }

    @Test
    fun `default userId should be empty`() {
        TestDefaultProvider().provideUserId()?.shouldBeEmpty()
    }

    @Test
    fun `default IdTracking should be empty`() {
        TestDefaultProvider().provideIdTrackingIdentifier()?.shouldBeEmpty()
    }
}
