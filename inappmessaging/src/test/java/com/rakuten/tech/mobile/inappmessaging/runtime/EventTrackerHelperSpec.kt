package com.rakuten.tech.mobile.inappmessaging.runtime

import android.os.Build
import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for EventTrackerHelper class.
 */

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@SuppressWarnings("kotlin:S100")
class SendEventSpec(
    private val eventName: String,
    private val data: Map<String, *>?,
    private val expected: Boolean
) {

    private val sendEvent = EventTrackerHelper::sendEvent
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf("event1", mapOf(Pair("param1", Any())), true),
                arrayOf("event2", emptyMap<String, Any>(), true),
                arrayOf("event3", mapOf(Pair(Any(), Any())), true),
                arrayOf("event4", null, true),
                arrayOf("", emptyMap<String, Any>(), false)
            )
        }
    }

    @Test
    fun `should return if event was sent`() {
        sendEvent(eventName, data) shouldBeEqualTo expected
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@SuppressWarnings("kotlin:S100")
class HasClassSpec(
    private val className: String,
    private val expected: Boolean
) {

    private val hasClass = EventTrackerHelper::hasClass
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf("com.rakuten.tech.mobile.analytics.Event", true),
                arrayOf("", false),
                arrayOf("com.rakuten.tech.mobile.NonExistingClass", false)
            )
        }
    }

    @Test
    fun `should return if class exists`() {
        hasClass(className) shouldNotBeEqualTo expected
    }
}
