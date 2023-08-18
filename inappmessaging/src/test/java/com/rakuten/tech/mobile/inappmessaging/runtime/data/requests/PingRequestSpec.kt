package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.CampaignType
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class PingRequestSpec {

    private val pingRequest = PingRequest(
        appVersion = "appVersion",
        userIdentifiers = listOf(),
        supportedTypes = listOf(CampaignType.REGULAR.typeId),
        rmcSdkVersion = "1.0.0",
    )

    @Test
    fun `should serialize PingRequest with correct json field names`() {
        val json = """
            >{"appVersion":"appVersion","userIdentifiers":[],"supportedCampaignTypes":[1],"rmcSdkVersion":"1.0.0"}
        """.trimMargin(">").replace("\n", "")

        Gson().toJson(pingRequest) shouldBeEqualTo json
    }

    @Test
    fun `should not serialize PingRequest with null values`() {
        val testDataClass = pingRequest.copy(rmcSdkVersion = null)
        val json = """{"appVersion":"appVersion","userIdentifiers":[],"supportedCampaignTypes":[1]}"""

        Gson().toJson(testDataClass) shouldBeEqualTo json
    }
}
