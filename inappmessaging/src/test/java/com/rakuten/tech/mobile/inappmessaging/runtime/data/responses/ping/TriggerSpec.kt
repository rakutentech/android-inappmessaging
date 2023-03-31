package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.Test
import java.util.*

class TriggerSpec {
    @Test
    fun `should return null trigger matching event name for invalid event`() {
        val trigger = Trigger(0, EventType.INVALID.typeId, "test", mutableListOf())

        trigger.matchingEventName.shouldBeNull()
    }

    @Test
    fun `should return null trigger matching event name for non-existent event`() {
        val trigger = Trigger(0, 99, "test", mutableListOf())

        trigger.matchingEventName.shouldBeNull()
    }

    @Test
    fun `should return correct trigger matching event name for custom event`() {
        val trigger = Trigger(0, EventType.CUSTOM.typeId, "test", mutableListOf())

        val expected = trigger.eventName.lowercase(Locale.getDefault())
        trigger.matchingEventName.shouldBeEqualTo(expected)
    }

    @Test
    fun `should return correct trigger matching event name for normal event`() {
        val trigger = Trigger(0, EventType.APP_START.typeId, "test", mutableListOf())

        val expected = EventType.APP_START.name.lowercase(Locale.getDefault())
        trigger.matchingEventName.shouldBeEqualTo(expected)
    }
}
