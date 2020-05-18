package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

/**
 * Test for BaseEvent class.
 */
class BaseEventSpec : BaseTest() {

    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception with empty name`() {
        TestEvent(EventType.CUSTOM, "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception with name length more than max`() {
        TestEvent(EventType.CUSTOM, "X".repeat(256))
    }

    @Test
    fun `should throw correct exception message with empty name`() {
        try {
            TestEvent(EventType.CUSTOM, "")
        } catch (e: IllegalArgumentException) {
            e.localizedMessage!! shouldBeEqualTo InAppMessagingConstants.EVENT_NAME_EMPTY_EXCEPTION
        }
    }

    @Test
    fun `should throw correct exception message with name length more than max`() {
        try {
            TestEvent(EventType.CUSTOM, "X".repeat(256))
        } catch (e: IllegalArgumentException) {
            e.localizedMessage!! shouldBeEqualTo InAppMessagingConstants.EVENT_NAME_TOO_LONG_EXCEPTION
        }
    }

    /**
     * Test logEvent which extends BaseEvent.
     */
    private class TestEvent(eventType: EventType, eventName: String) : BaseEvent(eventType, eventName, false)
}
