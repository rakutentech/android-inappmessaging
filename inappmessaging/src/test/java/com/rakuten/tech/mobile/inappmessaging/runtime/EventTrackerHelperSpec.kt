package com.rakuten.tech.mobile.inappmessaging.runtime

import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/**
 * Tests for EventTrackerHelper class.
 */

@RunWith(ParameterizedRobolectricTestRunner::class)
class EventTrackerHelperSpec(
    private val eventName: String,
    private val data: Map<String, *>?,
    private val expected: Boolean,
) {
    private val sendEvent = EventTrackerHelper::sendEvent

    @Test
    fun `should return if event was sent`() {
        sendEvent(eventName, data) shouldBeEqualTo expected
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf("event1", mapOf(Pair("param1", Any())), true),
                arrayOf("event2", emptyMap<String, Any>(), true),
                arrayOf("event3", mapOf(Pair(Any(), Any())), true),
                arrayOf("event4", null, true),
                arrayOf("", emptyMap<String, Any>(), false),
            )
        }
    }
}
