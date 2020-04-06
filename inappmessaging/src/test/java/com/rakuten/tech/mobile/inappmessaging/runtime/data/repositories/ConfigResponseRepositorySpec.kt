package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponse
import org.amshove.kluent.shouldEqual
import org.junit.Test

/**
 * Test class for ConfigResponseRepository class.
 */
class ConfigResponseRepositorySpec : BaseTest() {

    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception for null data`() {
        ConfigResponseRepository.instance().addConfigResponse(null)
    }

    @Test
    fun `should be empty string for impression endpoints with initial values`() {
        ConfigResponseRepository.resetInstance()
        ConfigResponseRepository.instance().getImpressionEndpoint() shouldEqual ""
    }

    @Test
    fun `should be empty string for display endpoints with initial values`() {
        ConfigResponseRepository.resetInstance()
        ConfigResponseRepository.instance().getDisplayPermissionEndpoint() shouldEqual ""
    }

    @Test
    fun `should be empty string for ping endpoints with initial values`() {
        ConfigResponseRepository.resetInstance()
        ConfigResponseRepository.instance().getPingEndpoint() shouldEqual ""
    }

    @Test
    fun `should be valid value for impression endpoints with initial values`() {
        val response = Gson().fromJson(CONFIG_RESPONSE.trimIndent(), ConfigResponse::class.java)
        ConfigResponseRepository.instance().addConfigResponse(response.data)
        ConfigResponseRepository.instance().getImpressionEndpoint() shouldEqual response.data?.endpoints?.impression
    }

    @Test
    fun `should be valid value for display endpoints with initial values`() {
        val response = Gson().fromJson(CONFIG_RESPONSE.trimIndent(), ConfigResponse::class.java)
        ConfigResponseRepository.instance().addConfigResponse(response.data)
        ConfigResponseRepository.instance()
                .getDisplayPermissionEndpoint() shouldEqual response.data?.endpoints?.displayPermission
    }

    @Test
    fun `should be valid value for ping endpoints with initial values`() {
        val response = Gson().fromJson(CONFIG_RESPONSE.trimIndent(), ConfigResponse::class.java)
        ConfigResponseRepository.instance().addConfigResponse(response.data)
        ConfigResponseRepository.instance().getPingEndpoint() shouldEqual response.data?.endpoints?.ping
    }

    companion object {
        private const val CONFIG_RESPONSE = """{
            "data":{
                "enabled":true,
                "endpoints":{
                    "displayPermission":"https://sample.display.permission",
                    "impression":"https://sample.impression",
                    "ping":"https://sample.ping"
                }
            }
        }"""
    }
}
