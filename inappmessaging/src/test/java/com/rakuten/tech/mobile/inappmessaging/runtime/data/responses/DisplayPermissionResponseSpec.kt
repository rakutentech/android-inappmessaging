package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses

import com.google.gson.Gson
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeEquivalentTo
import org.junit.Test

@OptIn(ExperimentalStdlibApi::class)
class DisplayPermissionResponseSpec {

    @Test
    fun `should serialize DisplayPermissionResponse with correct json field names`() {
        val testDataClass = DisplayPermissionResponse(
            display = true,
            shouldPing = false,
        )
        val json = """{"display":true,"performPing":false}"""
        Gson().toJson(testDataClass).shouldBeEqualTo(json)
    }

    @Test
    fun `should deserialize DisplayPermissionResponse from json field names set`() {
        val json = """{"display":false,"performPing":true}"""
        val testDataClass = Gson().fromJson(json, DisplayPermissionResponse::class.java)
        testDataClass.shouldBeEquivalentTo(
            DisplayPermissionResponse(
                display = false,
                shouldPing = true,
            ),
        )
    }
}
