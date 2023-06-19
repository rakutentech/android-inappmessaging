package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.CampaignType
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class PingRequestSpec {

    @Test
    fun `should serialize PingRequest with correct json field names`() {
        val testDataClass = PingRequest(
            appVersion = "appVersion",
            userIdentifiers = listOf(),
            supportedTypes = listOf(CampaignType.REGULAR.typeId),
            deviceId = "duMMyDeviceId",
        )
        val json = """
            {"appVersion":"appVersion","userIdentifiers":[],"supportedCampaignTypes":[1],"deviceId":"duMMyDeviceId"}
        """.trimIndent()
        Gson().toJson(testDataClass).shouldBeEqualTo(json)
    }
}
