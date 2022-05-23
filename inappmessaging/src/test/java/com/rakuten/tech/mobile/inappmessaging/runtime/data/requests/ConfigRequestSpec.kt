package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.rakuten.tech.mobile.inappmessaging.runtime.fromJson
import com.rakuten.tech.mobile.inappmessaging.runtime.toJson
import com.squareup.moshi.Moshi
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

/**
 * Test for ConfigRequest class.
 */
class ConfigRequestSpec {

    @Test
    fun `should return correct json from value`() {
        val request = ConfigQueryParamsBuilder("com.package.test", "jp", "0.0.1", "1.6.0-SNAPSHOT")
        val jsonString = Moshi.Builder().build().toJson(data = request)
        jsonString shouldBeEqualTo REQUEST
    }

    @Test
    fun `should return correct values from json`() {
        // for coverage
        val request = Moshi.Builder().build().fromJson<ConfigQueryParamsBuilder>(data = REQUEST.trimIndent())

        request!!.queryParams["platform"] shouldBeEqualTo 2.toDouble()
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
