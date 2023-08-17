package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotHaveKey
import org.junit.Test

class ConfigQueryParamsBuilderSpec {

    @Test
    fun `should map query params from data class`() {
        val testDataClass = ConfigQueryParamsBuilder(
            appId = "com.package.test",
            locale = "jp",
            appVersion = "0.0.1",
            sdkVersion = "1.6.0-SNAPSHOT",
            rmcSdkVersion = "1.0.0"
        )
        testDataClass.apply {
            queryParams["platform"] shouldBeEqualTo InAppMessagingConstants.ANDROID_PLATFORM_ENUM
            queryParams["appId"] shouldBeEqualTo "com.package.test"
            queryParams["sdkVersion"] shouldBeEqualTo "1.6.0-SNAPSHOT"
            queryParams["appVersion"] shouldBeEqualTo "0.0.1"
            queryParams["locale"] shouldBeEqualTo "jp"
            queryParams["rmcSdkVersion"] shouldBeEqualTo "1.0.0"
        }
    }

    @Test
    fun `should not include rmcSdkVersion in queryParams when set to null`() {
        val testDataClass = ConfigQueryParamsBuilder(rmcSdkVersion = null)
        testDataClass.queryParams shouldNotHaveKey "rmcSdkVersion"
    }

    @Test
    fun `should map platform in query params`() {
        val testDataClass = ConfigQueryParamsBuilder()
        testDataClass.queryParams["platform"] shouldBeEqualTo InAppMessagingConstants.ANDROID_PLATFORM_ENUM
    }
}
