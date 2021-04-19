package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.google.gson.Gson
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

/**
 * Test for ConfigRequest class.
 */
class ConfigRequestSpec {
    @Test
    fun `should return correct values from json`() {
        // for coverage
        val request = Gson().fromJson(REQUEST.trimIndent(), ConfigRequest::class.java)

        val queryParams: MutableMap<String, Any?> =
                mutableMapOf(
                        "platform" to 2.0,
                        "appId" to "com.package.test",
                        "sdkVersion" to "1.6.0-SNAPSHOT",
                        "appVersion" to "0.0.1",
                        "locale" to "jp")

        request.queryParam shouldBeEqualTo queryParams
    }

    companion object {

        private const val REQUEST = "{" +
                "\"appId\":\"com.package.test\"," +
                "\"sdkVersion\":\"1.6.0-SNAPSHOT\"," +
                "\"appVersion\":\"0.0.1\"," +
                "\"locale\":\"jp\"," +
                "\"queryParam\":{\"platform\":2,\"appId\":\"com.package.test\",\"sdkVersion\":\"1.6.0-SNAPSHOT\",\"appVersion\":\"0.0.1\",\"locale\":\"jp\"} }"
    }
}
