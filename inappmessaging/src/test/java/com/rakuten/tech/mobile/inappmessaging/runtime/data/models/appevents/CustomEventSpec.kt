package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ValueType
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.AnalyticsKey
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldHaveKey
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import java.util.Date

/**
 * Test CustomEvent class.
 */
class CustomEventSpec : BaseTest() {
    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception with empty name`() {
        CustomEvent("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception with name length more than max`() {
        CustomEvent("X".repeat(256))
    }

    @Test
    fun `should throw correct exception message with empty name`() {
        try {
            CustomEvent("")
        } catch (e: IllegalArgumentException) {
            e.localizedMessage!! shouldBeEqualTo InAppMessagingConstants.EVENT_NAME_EMPTY_EXCEPTION
        }
    }

    @Test
    fun `should throw correct exception message with name length more than max`() {
        try {
            CustomEvent("X".repeat(256))
        } catch (e: IllegalArgumentException) {
            e.localizedMessage!! shouldBeEqualTo InAppMessagingConstants.EVENT_NAME_TOO_LONG_EXCEPTION
        }
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
class CustomEventParameterizedSpec(
    val testName: String,
    private val attrType: ValueType,
    private val attrValue: String,
) {

    @Test
    fun `should add correct attribute type and value`() {
        when (attrType) {
            ValueType.STRING -> confirmString()
            ValueType.INTEGER -> confirmInt()
            ValueType.DOUBLE -> confirmDouble()
            ValueType.BOOLEAN -> confirmBoolean()
            ValueType.TIME_IN_MILLI -> confirmMilli()
            else -> {}
        }
    }

    private fun confirmMilli() {
        confirmValues(
            CustomEvent(EVENT_NAME).addAttribute(DATE_ATTRIBUTE, Date(attrValue.toLong())),
            DATE_ATTRIBUTE, DATE_ATTRIBUTE, attrValue, ValueType.TIME_IN_MILLI.typeId,
        )
        confirmValues(
            CustomEvent(EVENT_NAME).addAttribute(DATE_ATTRIBUTE_UPPER, Date(attrValue.toLong())),
            DATE_ATTRIBUTE, DATE_ATTRIBUTE, attrValue, ValueType.TIME_IN_MILLI.typeId,
        )
    }

    private fun confirmBoolean() {
        confirmValues(
            CustomEvent(EVENT_NAME).addAttribute(BOOLEAN_ATTRIBUTE, attrValue.toBoolean()),
            BOOLEAN_ATTRIBUTE, BOOLEAN_ATTRIBUTE, attrValue, ValueType.BOOLEAN.typeId,
        )
        confirmValues(
            CustomEvent(EVENT_NAME).addAttribute(BOOLEAN_ATTRIBUTE_UPPER, attrValue.toBoolean()),
            BOOLEAN_ATTRIBUTE, BOOLEAN_ATTRIBUTE, attrValue, ValueType.BOOLEAN.typeId,
        )
    }

    private fun confirmDouble() {
        confirmValues(
            CustomEvent(EVENT_NAME).addAttribute(DOUBLE_ATTRIBUTE, attrValue.toDouble()),
            DOUBLE_ATTRIBUTE, DOUBLE_ATTRIBUTE, attrValue, ValueType.DOUBLE.typeId,
        )
        confirmValues(
            CustomEvent(EVENT_NAME).addAttribute(DOUBLE_ATTRIBUTE_UPPER, attrValue.toDouble()),
            DOUBLE_ATTRIBUTE, DOUBLE_ATTRIBUTE, attrValue, ValueType.DOUBLE.typeId,
        )
    }

    private fun confirmInt() {
        confirmValues(
            CustomEvent(EVENT_NAME).addAttribute(INTEGER_ATTRIBUTE, attrValue.toInt()),
            INTEGER_ATTRIBUTE, INTEGER_ATTRIBUTE, attrValue, ValueType.INTEGER.typeId,
        )
        confirmValues(
            CustomEvent(EVENT_NAME).addAttribute(INTEGER_ATTRIBUTE_UPPER, attrValue.toInt()),
            INTEGER_ATTRIBUTE, INTEGER_ATTRIBUTE, attrValue, ValueType.INTEGER.typeId,
        )
    }

    private fun confirmString() {
        confirmValues(
            CustomEvent(EVENT_NAME).addAttribute(STRING_ATTRIBUTE, attrValue),
            STRING_ATTRIBUTE, STRING_ATTRIBUTE, attrValue, ValueType.STRING.typeId,
        )
        confirmValues(
            CustomEvent(EVENT_NAME).addAttribute(STRING_ATTRIBUTE_UPPER, attrValue),
            STRING_ATTRIBUTE, STRING_ATTRIBUTE, attrValue, ValueType.STRING.typeId,
        )
    }

    private fun confirmValues(event: CustomEvent, key: String, name: String, value: String, type: Int) {
        event.isPersistentType().shouldBeFalse()
        event.getAttributeMap()[key]?.name shouldBeEqualTo name
        event.getAttributeMap()[key]?.value shouldBeEqualTo value
        event.getAttributeMap()[key]?.valueType shouldBeEqualTo type
        val ratMap = event.getRatEventMap()
        ratMap shouldHaveKey AnalyticsKey.EVENT_NAME.key
        ratMap shouldHaveKey AnalyticsKey.TIMESTAMP.key
        ratMap shouldHaveKey AnalyticsKey.CUSTOM_ATTRIBUTES.key
    }

    companion object {
        private const val STRING_ATTRIBUTE = "string"
        private const val INTEGER_ATTRIBUTE = "integer"
        private const val DOUBLE_ATTRIBUTE = "double"
        private const val BOOLEAN_ATTRIBUTE = "boolean"
        private const val DATE_ATTRIBUTE = "date"
        private const val STRING_ATTRIBUTE_UPPER = "STRING"
        private const val INTEGER_ATTRIBUTE_UPPER = "INTEGER"
        private const val DOUBLE_ATTRIBUTE_UPPER = "DOUBLE"
        private const val BOOLEAN_ATTRIBUTE_UPPER = "BOOLEAN"
        private const val DATE_ATTRIBUTE_UPPER = "DATE"
        private const val EVENT_NAME = "test"

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0} type test",
        )
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("String", ValueType.STRING, "attribute value"),
                arrayOf("Integer", ValueType.INTEGER, "1"),
                arrayOf("Double", ValueType.DOUBLE, "10.5"),
                arrayOf("Boolean", ValueType.BOOLEAN, "true"),
                arrayOf("Boolean", ValueType.BOOLEAN, "false"),
                arrayOf("Long", ValueType.TIME_IN_MILLI, "9999"),
            )
        }
    }
}
