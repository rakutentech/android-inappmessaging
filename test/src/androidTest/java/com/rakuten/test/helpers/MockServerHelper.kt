package com.rakuten.test.helpers

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

object MockServerHelper {

    fun doIamRequest(server: MockWebServer, jsonFilename: String) {
        server.enqueue(MockResponse()
            .setBody(JsonFileReader("${Constants.RESP_STUB_DIR}/config.json").content))
        server.enqueue(MockResponse()
            .setBody(JsonFileReader("${Constants.RESP_STUB_DIR}/$jsonFilename").content))
        server.enqueue(MockResponse()
            .setBody(JsonFileReader("${Constants.RESP_STUB_DIR}/display-permission.json").content))

        Thread.sleep(1000)
    }
}