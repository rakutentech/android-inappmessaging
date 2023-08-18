package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.UserIdentifier
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeEquivalentTo
import org.junit.Test

@OptIn(ExperimentalStdlibApi::class)
class DisplayPermissionRequestSpec {

    private val displayPermissionRequest = DisplayPermissionRequest(
        campaignId = "test-campaignId",
        appVersion = "test-appVersion",
        sdkVersion = "test-sdkVersion",
        locale = "test-locale",
        lastPingInMillis = 0L,
        userIdentifier = listOf(UserIdentifier(id = "test-id", type = 0)),
        rmcSdkVersion = "1.0.0",
    )

    @Test
    fun `should serialize DisplayPermissionRequest with correct json field names`() {
        val json = """
            >{"campaignId":"test-campaignId","appVersion":"test-appVersion","sdkVersion":"test-sdkVersion",
            >"locale":"test-locale","lastPingInMillis":0,"userIdentifier":[{"id":"test-id","type":0}],
            >"rmcSdkVersion":"1.0.0","platform":2}
        """.trimMargin(">").replace("\n", "")

        Gson().toJson(displayPermissionRequest) shouldBeEqualTo json
    }

    @Test
    fun `should not serialize DisplayPermissionRequest with null values`() {
        val testDataClass = displayPermissionRequest.copy(rmcSdkVersion = null)

        val json = """
            >{"campaignId":"test-campaignId","appVersion":"test-appVersion","sdkVersion":"test-sdkVersion",
            >"locale":"test-locale","lastPingInMillis":0,"userIdentifier":[{"id":"test-id","type":0}],"platform":2}
        """.trimMargin(">").replace("\n", "")

        Gson().toJson(testDataClass) shouldBeEqualTo json
    }

    @Test
    fun `should deserialize DisplayPermissionRequest from json field names set`() {
        val json = """{"lastPingInMillis":0,"userIdentifier":[{"id":"test-id","type":0}],"platform":2}"""
        val testDataClass = Gson().fromJson(json, DisplayPermissionRequest::class.java)

        testDataClass.shouldBeEquivalentTo(
            DisplayPermissionRequest(
                campaignId = null,
                appVersion = null,
                sdkVersion = null,
                locale = null,
                lastPingInMillis = 0,
                userIdentifier = listOf(UserIdentifier(id = "test-id", type = 0)),
                rmcSdkVersion = null,
            ),
        )
    }
}
