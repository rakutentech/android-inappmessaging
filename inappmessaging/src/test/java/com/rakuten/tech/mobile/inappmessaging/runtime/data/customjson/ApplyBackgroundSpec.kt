package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class ApplyBackgroundSpec {
    private val message = MessageMapper.mapFrom(TestDataHelper.createDummyMessage())

    @Test
    fun `should do nothing if background setting does not exist`() {
        val uiMessage = message.applyBackground(null)
        uiMessage shouldBeEqualTo message

        uiMessage.applyBackground(Background(null))
        uiMessage shouldBeEqualTo message
    }

    @Test
    fun `should do nothing if opacity set is invalid`() {
        val uiMessage = message.applyBackground(Background(-0.5f))
        uiMessage shouldBeEqualTo message

        uiMessage.applyBackground(Background(3f))
        uiMessage shouldBeEqualTo message
    }

    @Test
    fun `should correctly map opacity setting`() {
        val uiMessage = message.applyBackground(Background(0.6f))
        uiMessage.backdropOpacity shouldBeEqualTo 0.6f
    }

    @Test
    fun `should correctly map opacity setting - minimum`() {
        val uiMessage = message.applyBackground(Background(0f))
        uiMessage.backdropOpacity shouldBeEqualTo 0f
    }

    @Test
    fun `should correctly map opacity setting - maximum`() {
        val uiMessage = message.applyBackground(Background(1f))
        uiMessage.backdropOpacity shouldBeEqualTo 1f
    }
}
