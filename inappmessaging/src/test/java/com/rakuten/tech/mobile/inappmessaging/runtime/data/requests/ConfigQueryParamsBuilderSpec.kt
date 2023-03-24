package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class ConfigQueryParamsBuilderSpec {

    @Test
    fun `should map query params from data class`() {
        val testDataClass = ConfigQueryParamsBuilder(
            appId = "com.package.test",
            locale = "jp",
            appVersion = "0.0.1",
            sdkVersion = "1.6.0-SNAPSHOT",
        )
        testDataClass.apply {
            queryParams["platform"] shouldBeEqualTo InAppMessagingConstants.ANDROID_PLATFORM_ENUM
            queryParams["appId"] shouldBeEqualTo "com.package.test"
            queryParams["sdkVersion"] shouldBeEqualTo "1.6.0-SNAPSHOT"
            queryParams["appVersion"] shouldBeEqualTo "0.0.1"
            queryParams["locale"] shouldBeEqualTo "jp"
        }
    }

    @Test
    fun `should map platform in query params`() {
        val testDataClass = ConfigQueryParamsBuilder()
        testDataClass.queryParams["platform"] shouldBeEqualTo InAppMessagingConstants.ANDROID_PLATFORM_ENUM
    }
}
