package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.google.gson.Gson
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

/**
 * Test for ConfigRequest class.
 */
class ConfigRequestSpec {

    @Test
    fun `should return correct json from value`() {
        val request = ConfigQueryParamsBuilder("com.package.test", "jp", "0.0.1", "1.6.0-SNAPSHOT")
        val jsonString = Gson().toJson(request)
        jsonString shouldBeEqualTo REQUEST
    }

    @Test
    fun `should return correct values from json`() {
        // for coverage
        val request = Gson().fromJson(REQUEST.trimIndent(), ConfigQueryParamsBuilder::class.java)

        request.queryParams["platform"] shouldBeEqualTo 2.toDouble()
        request.queryParams["appId"] shouldBeEqualTo "com.package.test"
        request.queryParams["sdkVersion"] shouldBeEqualTo "1.6.0-SNAPSHOT"
        request.queryParams["appVersion"] shouldBeEqualTo "0.0.1"
        request.queryParams["locale"] shouldBeEqualTo "jp"
    }

    companion object {

        private const val REQUEST = "{\"appId\":\"com.package.test\"," +
            "\"locale\":\"jp\"," +
            "\"appVersion\":\"0.0.1\"," +
            "\"sdkVersion\":\"1.6.0-SNAPSHOT\"," +
            "\"platform\":2," +
            "\"queryParams\":" +
            "{" +
            "\"platform\":2," +
            "\"appId\":\"com.package.test\"," +
            "\"sdkVersion\":\"1.6.0-SNAPSHOT\"," +
            "\"appVersion\":\"0.0.1\",\"locale\":\"jp\"" +
            "}}"
    }
}
