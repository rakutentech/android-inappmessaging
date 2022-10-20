package com.rakuten.test.helpers

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

object MockServerHelper {
    var pingJsonFilename = ""

    val dispatcher: Dispatcher = object : Dispatcher() {

        @Throws(InterruptedException::class)
        override fun dispatch(request: RecordedRequest): MockResponse {
            with(request.path) {
                when {
                    contains("/config") ->
                        return MockResponse()
                            .setBody(JsonFileReader("${Constants.RESP_STUB_DIR}/config.json").content)
                    contains("/ping") ->
                        return MockResponse()
                            .setBody(JsonFileReader("${Constants.RESP_STUB_DIR}/$pingJsonFilename").content)
                    contains("/display_permission") ->
                        return MockResponse()
                            .setBody(JsonFileReader("${Constants.RESP_STUB_DIR}/display-permission.json").content)
                    contains("/impression") ->
                        return MockResponse().setResponseCode(200)
                    else ->
                        return MockResponse().setResponseCode(500)
                }
            }
        }
    }
}