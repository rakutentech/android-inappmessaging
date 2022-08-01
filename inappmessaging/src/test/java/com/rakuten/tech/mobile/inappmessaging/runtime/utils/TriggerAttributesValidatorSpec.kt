package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ValueType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.CustomEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.TriggerAttribute
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class TriggerAttributesValidatorSpec : BaseTest() {

    @Test
    fun `should return true with valid string trigger`() {
        testValidValueType(ValueType.STRING)
    }

    @Test
    fun `should return true with valid integer trigger`() {
        testValidValueType(ValueType.INTEGER)
    }

    @Test
    fun `should return true with valid double trigger`() {
        testValidValueType(ValueType.DOUBLE)
    }

    @Test
    fun `should return true with valid boolean trigger`() {
        testValidValueType(ValueType.BOOLEAN)
    }

    @Test
    fun `should return true with valid time trigger`() {
        testValidValueType(ValueType.TIME_IN_MILLI)
    }

    @Test
    fun `should return false when event type mismatches`() {
        TriggerAttributesValidator.isTriggerSatisfied(
            Trigger(1, EventType.APP_START.typeId, "test", mutableListOf()),
            CustomEvent("custom")
        ).shouldBeFalse()
    }

    @Test
    fun `should return false when attribute name mismatches`() {
        val trigger = Trigger(
            0, EventType.CUSTOM.typeId, "custom",
            mutableListOf(
                TriggerAttribute("name1", "value", 1, 1)
            )
        )
        val customEvent = CustomEvent("custom")
        customEvent.addAttribute("name2", "value")
        TriggerAttributesValidator.isTriggerSatisfied(trigger, customEvent).shouldBeFalse()
    }

    @Test
    fun `should return false when attribute value type mismatches`() {
        val trigger = Trigger(
            0, EventType.CUSTOM.typeId, "custom",
            mutableListOf(
                TriggerAttribute("name", "value", 1, 1)
            )
        )
        val customEvent = CustomEvent("custom")
        customEvent.addAttribute("name", 1.0)
        TriggerAttributesValidator.isTriggerSatisfied(trigger, customEvent).shouldBeFalse()
    }

    @SuppressWarnings("LongMethod")
    private fun testValidValueType(valueType: ValueType) {
        var trigger: Trigger? = null
        val customEvent = CustomEvent("custom")

        when (valueType) {
            ValueType.STRING -> {
                trigger = Trigger(
                    0, EventType.CUSTOM.typeId, "custom",
                    mutableListOf(
                        TriggerAttribute("name", "value", 1, 1)
                    )
                )
                customEvent.addAttribute("name", "value")
            }
            ValueType.INTEGER -> {
                trigger = Trigger(
                    0, EventType.CUSTOM.typeId, "custom",
                    mutableListOf(
                        TriggerAttribute("name", "1", 2, 1)
                    )
                )
                customEvent.addAttribute("name", 1)
            }
            ValueType.DOUBLE -> {
                trigger = Trigger(
                    0, EventType.CUSTOM.typeId, "custom",
                    mutableListOf(
                        TriggerAttribute("name", "1.0", 3, 1)
                    )
                )
                customEvent.addAttribute("name", 1.0)
            }
            ValueType.BOOLEAN -> {
                trigger = Trigger(
                    0, EventType.CUSTOM.typeId, "custom",
                    mutableListOf(
                        TriggerAttribute("name", "true", 4, 1)
                    )
                )
                customEvent.addAttribute("name", true)
            }
            ValueType.TIME_IN_MILLI -> {
                val currDate = Date()
                trigger = Trigger(
                    0, EventType.CUSTOM.typeId, "custom",
                    mutableListOf(
                        TriggerAttribute("name", currDate.time.toString(), 5, 1)
                    )
                )
                customEvent.addAttribute("name", currDate)
            }
            else -> {}
        }

        trigger?.let {
            TriggerAttributesValidator.isTriggerSatisfied(it, customEvent).shouldBeTrue()
        }
    }
}
