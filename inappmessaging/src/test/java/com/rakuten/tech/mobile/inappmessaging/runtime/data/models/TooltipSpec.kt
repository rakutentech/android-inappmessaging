package com.rakuten.tech.mobile.inappmessaging.runtime.data.models

import com.google.gson.Gson
import org.amshove.kluent.shouldBeEquivalentTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test

@OptIn(ExperimentalStdlibApi::class)
class TooltipSpec {

    @Test
    fun `should deserialize Tooltip from json field names set`() {
        val json = """{"UIElement":"view-id","position":"top-left"}"""
        val testDataClass = Gson().fromJson(json, Tooltip::class.java)
        testDataClass.shouldBeEquivalentTo(
            Tooltip(
                id = "view-id",
                position = "top-left",
            )
        )
        testDataClass.isValid().shouldBeTrue()
    }

    @Test
    fun `should return false when tooltip id is invalid`() {
        Tooltip(id = "").isValid().shouldBeFalse()
    }

    @Test
    fun `should return false when position is invalid`() {
        Tooltip(position = "").isValid().shouldBeFalse()
    }
}