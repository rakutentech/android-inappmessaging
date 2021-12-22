package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.OperatorType
import java.math.BigDecimal
import kotlin.math.abs

/**
 * Value matching utility class, it contains utility methods which compare values of the same type
 * by its operator.
 */
internal object ValueMatchingUtil {
    private const val TIME_IN_MILLIS_TOLERANCE = 1000

    /**
     * This method compares two integer values (i.e. [eventValue] and [triggerValue]) using the [operatorType].
     * Supported operators are: EQUALS, DOES_NOT_EQUAL, GREATER_THAN, LESS_THAN.
     */
    fun isOperatorConditionSatisfied(eventValue: Int?, operatorType: OperatorType?, triggerValue: Int?): Boolean {
        return if (eventValue == null || operatorType == null || triggerValue == null) {
            false
        } else when (operatorType) {
            OperatorType.EQUALS -> eventValue == triggerValue
            OperatorType.DOES_NOT_EQUAL -> eventValue != triggerValue
            OperatorType.GREATER_THAN -> eventValue > triggerValue
            OperatorType.LESS_THAN -> eventValue < triggerValue
            OperatorType.IS_BLANK, OperatorType.IS_NOT_BLANK,
            OperatorType.MATCHES_REGEX, OperatorType.DOES_NOT_MATCH_REGEX -> false
            else -> false
        }
    }

    /**
     * This method compares two Double values (i.e. [eventValue] and [triggerValue]) using the [operatorType].
     * Supported operators are: EQUALS, DOES_NOT_EQUAL, GREATER_THAN, LESS_THAN.
     */
    fun isOperatorConditionSatisfied(eventValue: Double?, operatorType: OperatorType?, triggerValue: Double?): Boolean {
        if (eventValue == null || operatorType == null || triggerValue == null) {
            return false
        }
        val eventValueDecimal = BigDecimal(eventValue.toString())
        val triggerValueDecimal = BigDecimal(triggerValue.toString())
        return when (operatorType) {
            OperatorType.EQUALS -> eventValueDecimal.compareTo(triggerValueDecimal) == 0
            OperatorType.DOES_NOT_EQUAL -> eventValueDecimal.compareTo(triggerValueDecimal) != 0
            OperatorType.GREATER_THAN -> eventValueDecimal > triggerValueDecimal
            OperatorType.LESS_THAN -> eventValueDecimal < triggerValueDecimal
            OperatorType.IS_BLANK, OperatorType.IS_NOT_BLANK,
            OperatorType.MATCHES_REGEX, OperatorType.DOES_NOT_MATCH_REGEX -> false
            else -> false
        }
    }

    /**
     * This method compares two Long values (i.e. [eventValue] and [triggerValue]) using the [operatorType].
     * Supported operators are: EQUALS, DOES_NOT_EQUAL, GREATER_THAN, LESS_THAN.
     *
     * Note: If values are in milliseconds, and difference is within 1000ms, they are
     * considered equal. If values are not time, they are compared normally.
     */
    @SuppressWarnings("ComplexMethod", "LongMethod")
    fun isOperatorConditionSatisfied(
        eventValue: Long?,
        operatorType: OperatorType?,
        triggerValue: Long?,
        isTime: Boolean
    ): Boolean {
        return if (eventValue == null || operatorType == null || triggerValue == null) {
            false
        } else if (isTime) {
            compareTime(eventValue, operatorType, triggerValue)
        } else {
            compareNonTime(eventValue, operatorType, triggerValue)
        }
    }

    private fun compareNonTime(eventValue: Long, operatorType: OperatorType, triggerValue: Long): Boolean {
        return when (operatorType) {
            OperatorType.EQUALS -> eventValue.compareTo(triggerValue) == 0
            OperatorType.DOES_NOT_EQUAL -> eventValue.compareTo(triggerValue) != 0
            OperatorType.GREATER_THAN -> eventValue > triggerValue
            OperatorType.LESS_THAN -> eventValue < triggerValue
            else -> false
        }
    }

    private fun compareTime(eventValue: Long, operatorType: OperatorType, triggerValue: Long): Boolean {
        return when (operatorType) {
            OperatorType.EQUALS -> abs(eventValue - triggerValue) <= TIME_IN_MILLIS_TOLERANCE
            OperatorType.DOES_NOT_EQUAL -> abs(eventValue - triggerValue) > TIME_IN_MILLIS_TOLERANCE
            OperatorType.GREATER_THAN -> eventValue - triggerValue > TIME_IN_MILLIS_TOLERANCE
            OperatorType.LESS_THAN -> eventValue - triggerValue < -TIME_IN_MILLIS_TOLERANCE
            else -> false
        }
    }

    /**
     * This method compares two Boolean values (i.e. [eventValue] and [triggerValue]) using the [operatorType].
     * Supported operators are: EQUALS, DOES_NOT_EQUAL.
     */
    fun isOperatorConditionSatisfied(eventValue: Boolean?, operatorType: OperatorType?, triggerValue: Boolean?):
            Boolean {
        return if (eventValue == null || operatorType == null || triggerValue == null) {
            false
        } else when (operatorType) {
            OperatorType.EQUALS -> eventValue == triggerValue
            OperatorType.DOES_NOT_EQUAL -> eventValue != triggerValue
            OperatorType.GREATER_THAN, OperatorType.LESS_THAN, OperatorType.IS_BLANK,
            OperatorType.IS_NOT_BLANK, OperatorType.MATCHES_REGEX,
            OperatorType.DOES_NOT_MATCH_REGEX -> false
            else -> false
        }
    }

    /**
     * This method compares two String case insensitive values (i.e. [eventValue] and [triggerValue])
     * using the [operatorType].
     * Supported operators are: EQUALS, DOES_NOT_EQUAL, IS_BLANK, IS_NOT_BLANK, MATCHES_REGEX,
     * DOES_NOT_MATCH_REGEX.
     */
    @SuppressWarnings("ComplexMethod")
    fun isOperatorConditionSatisfied(eventValue: String?, operatorType: OperatorType?, triggerValue: String?): Boolean {
        return if (eventValue == null || operatorType == null || triggerValue == null) {
            false
        } else when (operatorType) {
            OperatorType.EQUALS -> eventValue.equals(triggerValue, ignoreCase = true)
            OperatorType.DOES_NOT_EQUAL -> !eventValue.equals(triggerValue, ignoreCase = true)
            OperatorType.IS_BLANK -> eventValue.isEmpty()
            OperatorType.IS_NOT_BLANK -> eventValue.isNotEmpty()
            OperatorType.MATCHES_REGEX -> eventValue.matches(triggerValue.toRegex())
            OperatorType.DOES_NOT_MATCH_REGEX -> !eventValue.matches(triggerValue.toRegex())
            OperatorType.GREATER_THAN, OperatorType.LESS_THAN -> false
            else -> false
        }
    }
}
