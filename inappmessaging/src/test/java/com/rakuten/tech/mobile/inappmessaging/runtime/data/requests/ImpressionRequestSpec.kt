package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeEquivalentTo
import org.junit.Test

@OptIn(ExperimentalStdlibApi::class)
class ImpressionRequestSpec {

    @Test
    fun `should serialize ImpressionRequest with correct json field names`() {
        val testDataClass = ImpressionRequest(
            campaignId = "test-campaignId",
            isTest = false,
            appVersion = "test-appVersion",
            sdkVersion = "test-sdkVersion",
            userIdentifiers = listOf(),
            impressions = listOf(Impression(ImpressionType.EXIT, 0)),
            rmcSdkVersion = null,
        )
        val json = """
            >{"campaignId":"test-campaignId","isTest":false,"appVersion":"test-appVersion",
            >"sdkVersion":"test-sdkVersion","userIdentifiers":[],
            >"impressions":[{"impType":"EXIT","timestamp":0,"type":4}]}
        """.trimMargin(">").replace("\n", "")
        Gson().toJson(testDataClass).shouldBeEqualTo(json)
    }

    @Test
    fun `should deserialize ImpressionRequest from json field names set`() {
        val json = """{"isTest":true,"userIdentifiers":[],"impressions":[]}"""
        val testDataClass = Gson().fromJson(json, ImpressionRequest::class.java)
        testDataClass.shouldBeEquivalentTo(
            ImpressionRequest(
                campaignId = null,
                isTest = true,
                appVersion = null,
                sdkVersion = null,
                userIdentifiers = listOf(),
                impressions = listOf(),
                rmcSdkVersion = null,
            ),
        )
    }
}
