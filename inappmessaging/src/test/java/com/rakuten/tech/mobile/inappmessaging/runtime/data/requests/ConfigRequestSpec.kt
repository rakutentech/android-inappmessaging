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
        request.appId shouldBeEqualTo "com.package.test"
        request.appVersion shouldBeEqualTo "0.0.1"
        request.sdkVersion shouldBeEqualTo "1.6.0-SNAPSHOT"
        request.locale shouldBeEqualTo "jp"
    }

    companion object {
        private const val REQUEST = """
            {
                "appVersion":"0.0.1",
                "appId":"com.package.test",
                "sdkVersion":"1.6.0-SNAPSHOT",
                "locale":"jp"
            }
        """
    }
}
