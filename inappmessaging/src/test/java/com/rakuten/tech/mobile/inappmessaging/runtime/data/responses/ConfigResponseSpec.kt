package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses

import com.google.gson.Gson
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeEquivalentTo
import org.junit.Test

@OptIn(ExperimentalStdlibApi::class)
class ConfigResponseSpec {

    @Test
    fun `should serialize ConfigResponse with correct json field names`() {
        val testDataClass = ConfigResponse(
            data = ConfigResponseData(
                rollOutPercentage = 100,
                endpoints = ConfigResponseEndpoints(
                    ping = "http://ping",
                    impression = "http://impression",
                    displayPermission = "http://display_permission",
                ),
            ),
        )
        val json = """
            >{"data":{"endpoints":{"ping":"http://ping","impression":"http://impression",
            >"displayPermission":"http://display_permission"},"rolloutPercentage":100}}
        """.trimMargin(">").replace("\n", "")
        Gson().toJson(testDataClass).shouldBeEqualTo(json)
    }

    @Test
    fun `should deserialize ConfigResponse from json field names set`() {
        val json = """{"data":{"rolloutPercentage":90}}"""
        val testDataClass = Gson().fromJson(json, ConfigResponse::class.java)
        testDataClass.shouldBeEquivalentTo(
            ConfigResponse(
                data = ConfigResponseData(
                    rollOutPercentage = 90,
                ),
            ),
        )
    }
}
