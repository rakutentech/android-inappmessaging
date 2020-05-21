package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents

import android.os.Build
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ValueType
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
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
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class CustomEventParameterizedSpec(
    val testName: String,
    val event: CustomEvent,
    val attrType: ValueType,
    val attrValue: String
) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
                name = "{0} type test"
        )
        fun data(): Collection<Array<Any>> {
            return listOf(
                    arrayOf("String", CustomEvent(EVENT_NAME), ValueType.STRING, "attribute value"),
                    arrayOf("Integer", CustomEvent(EVENT_NAME), ValueType.INTEGER, "1"),
                    arrayOf("Double", CustomEvent(EVENT_NAME), ValueType.DOUBLE, "10.5"),
                    arrayOf("Boolean", CustomEvent(EVENT_NAME), ValueType.BOOLEAN, "true"),
                    arrayOf("Boolean", CustomEvent(EVENT_NAME), ValueType.BOOLEAN, "false"),
                    arrayOf("Long", CustomEvent(EVENT_NAME), ValueType.TIME_IN_MILLI, "9999")
            )
        }

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
    }

    @Test
    @Suppress("LongMethod")
    fun `should add correct attribute type and value`() {
        when (attrType) {
            ValueType.STRING -> {
                event.addAttribute(STRING_ATTRIBUTE, attrValue)
                event.getAttributeMap()[STRING_ATTRIBUTE]?.name shouldEqual STRING_ATTRIBUTE
                event.getAttributeMap()[STRING_ATTRIBUTE]?.value shouldEqual attrValue
                event.getAttributeMap()[STRING_ATTRIBUTE]?.valueType shouldEqual ValueType.STRING.typeId
            }
            ValueType.INTEGER -> {
                event.addAttribute(INTEGER_ATTRIBUTE, attrValue.toInt())
                event.getAttributeMap()[INTEGER_ATTRIBUTE]?.name shouldEqual INTEGER_ATTRIBUTE
                event.getAttributeMap()[INTEGER_ATTRIBUTE]?.value shouldEqual attrValue
                event.getAttributeMap()[INTEGER_ATTRIBUTE]?.valueType shouldEqual ValueType.INTEGER.typeId
            }
            ValueType.DOUBLE -> {
                event.addAttribute(DOUBLE_ATTRIBUTE, attrValue.toDouble())
                event.getAttributeMap()[DOUBLE_ATTRIBUTE]?.name shouldEqual DOUBLE_ATTRIBUTE
                event.getAttributeMap()[DOUBLE_ATTRIBUTE]?.value shouldEqual attrValue
                event.getAttributeMap()[DOUBLE_ATTRIBUTE]?.valueType shouldEqual ValueType.DOUBLE.typeId
            }
            ValueType.BOOLEAN -> {
                event.addAttribute(BOOLEAN_ATTRIBUTE, attrValue.toBoolean())
                event.getAttributeMap()[BOOLEAN_ATTRIBUTE]?.name shouldEqual BOOLEAN_ATTRIBUTE
                event.getAttributeMap()[BOOLEAN_ATTRIBUTE]?.value shouldEqual attrValue
                event.getAttributeMap()[BOOLEAN_ATTRIBUTE]?.valueType shouldEqual ValueType.BOOLEAN.typeId
            }
            ValueType.TIME_IN_MILLI -> {
                event.addAttribute(DATE_ATTRIBUTE, Date(attrValue.toLong()))
                event.getAttributeMap()[DATE_ATTRIBUTE]?.name shouldEqual DATE_ATTRIBUTE
                event.getAttributeMap()[DATE_ATTRIBUTE]?.value shouldEqual attrValue
                event.getAttributeMap()[DATE_ATTRIBUTE]?.valueType shouldEqual ValueType.TIME_IN_MILLI.typeId
            }
            else -> {
            }
        }
    }

    @Test
    @Suppress("LongMethod")
    fun `should add correct attribute type and value with uppercase key`() {
        when (attrType) {
            ValueType.STRING -> {
                event.addAttribute(STRING_ATTRIBUTE_UPPER, attrValue)
                event.getAttributeMap()[STRING_ATTRIBUTE_UPPER]?.name shouldEqual STRING_ATTRIBUTE
                event.getAttributeMap()[STRING_ATTRIBUTE_UPPER]?.value shouldEqual attrValue
                event.getAttributeMap()[STRING_ATTRIBUTE_UPPER]?.valueType shouldEqual ValueType.STRING.typeId
            }
            ValueType.INTEGER -> {
                event.addAttribute(INTEGER_ATTRIBUTE_UPPER, attrValue.toInt())
                event.getAttributeMap()[INTEGER_ATTRIBUTE_UPPER]?.name shouldEqual INTEGER_ATTRIBUTE
                event.getAttributeMap()[INTEGER_ATTRIBUTE_UPPER]?.value shouldEqual attrValue
                event.getAttributeMap()[INTEGER_ATTRIBUTE_UPPER]?.valueType shouldEqual ValueType.INTEGER.typeId
            }
            ValueType.DOUBLE -> {
                event.addAttribute(DOUBLE_ATTRIBUTE_UPPER, attrValue.toDouble())
                event.getAttributeMap()[DOUBLE_ATTRIBUTE_UPPER]?.name shouldEqual DOUBLE_ATTRIBUTE
                event.getAttributeMap()[DOUBLE_ATTRIBUTE_UPPER]?.value shouldEqual attrValue
                event.getAttributeMap()[DOUBLE_ATTRIBUTE_UPPER]?.valueType shouldEqual ValueType.DOUBLE.typeId
            }
            ValueType.BOOLEAN -> {
                event.addAttribute(BOOLEAN_ATTRIBUTE_UPPER, attrValue.toBoolean())
                event.getAttributeMap()[BOOLEAN_ATTRIBUTE_UPPER]?.name shouldEqual BOOLEAN_ATTRIBUTE
                event.getAttributeMap()[BOOLEAN_ATTRIBUTE_UPPER]?.value shouldEqual attrValue
                event.getAttributeMap()[BOOLEAN_ATTRIBUTE_UPPER]?.valueType shouldEqual ValueType.BOOLEAN.typeId
            }
            ValueType.TIME_IN_MILLI -> {
                event.addAttribute(DATE_ATTRIBUTE_UPPER, Date(attrValue.toLong()))
                event.getAttributeMap()[DATE_ATTRIBUTE_UPPER]?.name shouldEqual DATE_ATTRIBUTE
                event.getAttributeMap()[DATE_ATTRIBUTE_UPPER]?.value shouldEqual attrValue
                event.getAttributeMap()[DATE_ATTRIBUTE_UPPER]?.valueType shouldEqual ValueType.TIME_IN_MILLI.typeId
            }
            else -> {
            }
        }
    }

    @Test
    fun `should not be persistent type`() {
        event.isPersistentType().shouldBeFalse()
    }
}
