package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.OperatorType
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test

/**
 * Test class for ValueMatchingUtil.
 */
@SuppressWarnings("LargeClass")
class ValueMatchingUtilSpec : BaseTest() {
    /* ---------------------------------- Testing for null arguments --------------------------*/
    @Test
    fun `should be false when integer event value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                null, OperatorType.EQUALS, 1).shouldBeFalse()
    }

    @Test
    fun `should be false when integer operator type argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1, null, 1).shouldBeFalse()
    }

    @Test
    fun `should be false when integer trigger value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1, OperatorType.EQUALS, null).shouldBeFalse()
    }

    @Test
    fun `should be false when long event value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                null, OperatorType.EQUALS, 1L, true).shouldBeFalse()
    }

    @Test
    fun `should be false when long operator type argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1L, null, 1L, true).shouldBeFalse()
    }

    @Test
    fun `should be false when long trigger value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1L, OperatorType.EQUALS, null, true).shouldBeFalse()
    }

    @Test
    fun `should be false when double event value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                null, OperatorType.EQUALS, 1.1).shouldBeFalse()
    }

    @Test
    fun `should be false when double operator type argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1.1, null, 1.1).shouldBeFalse()
    }

    @Test
    fun `should be false when double trigger value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1.1, OperatorType.EQUALS, null).shouldBeFalse()
    }

    @Test
    fun `should be false when boolean event value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                null, OperatorType.EQUALS, true).shouldBeFalse()
    }

    @Test
    fun `should be false when boolean operator type argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                true, null, true).shouldBeFalse()
    }

    @Test
    fun `should be false when boolean trigger value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                true, OperatorType.EQUALS, null).shouldBeFalse()
    }

    @Test
    fun `should be false when string event value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                null, OperatorType.EQUALS, "").shouldBeFalse()
    }

    @Test
    fun `should be false when string operator type argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                "", null, "").shouldBeFalse()
    }

    @Test
    fun `should be false when string trigger value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                "", OperatorType.EQUALS, null).shouldBeFalse()
    }

    /* ---------------------------------- Testing for matching results --------------------------*/
    @Test
    fun `should compare integer yield correct result`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1, OperatorType.EQUALS, 1).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1, OperatorType.DOES_NOT_EQUAL, 1).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1, OperatorType.GREATER_THAN, 1).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1, OperatorType.LESS_THAN, 1).shouldBeFalse()
    }

    @Test
    fun `should compare double yield correct result`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1.00001, OperatorType.EQUALS, 1.00001).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1.00001, OperatorType.DOES_NOT_EQUAL, 1.00001).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1.00001, OperatorType.GREATER_THAN, 1.00001).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
                1.00001, OperatorType.LESS_THAN, 1.00001).shouldBeFalse()
    }

    @Test
    fun `should compare non time long yield true`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_VALUE, OperatorType.EQUALS, LONG_VALUE, false).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_VALUE, OperatorType.DOES_NOT_EQUAL, LONG_OTHER_VALUE, false).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_OTHER_VALUE, OperatorType.GREATER_THAN, LONG_VALUE, false).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_VALUE, OperatorType.LESS_THAN, LONG_OTHER_VALUE, false).shouldBeTrue()
    }

    @Test
    fun `should compare non time long yield false`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_VALUE, OperatorType.EQUALS, LONG_OTHER_VALUE, false).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_VALUE, OperatorType.DOES_NOT_EQUAL, LONG_VALUE, false).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_VALUE, OperatorType.GREATER_THAN, LONG_VALUE, false).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_VALUE, OperatorType.LESS_THAN, LONG_VALUE, false).shouldBeFalse()
    }

    @Test
    fun `should compare time long yield true`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                TIME_IN_MILLIS, OperatorType.EQUALS, TIME_DIFF,
                true).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_IN_MILLIS, OperatorType.DOES_NOT_EQUAL, TIME_IN_MILLIS - 2 * TIME_IN_MILLIS_TOLERANCE,
            true).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_IN_MILLIS * 2, OperatorType.GREATER_THAN, TIME_DIFF,
            true).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_IN_MILLIS / 2, OperatorType.LESS_THAN, TIME_DIFF,
            true).shouldBeTrue()
    }

    @Test
    fun `should compare time long yield false`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_IN_MILLIS, OperatorType.EQUALS, TIME_IN_MILLIS - 2 * TIME_IN_MILLIS_TOLERANCE,
            true).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_IN_MILLIS, OperatorType.DOES_NOT_EQUAL, TIME_DIFF, true).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_IN_MILLIS, OperatorType.GREATER_THAN, TIME_DIFF, true).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_IN_MILLIS, OperatorType.LESS_THAN, TIME_DIFF, true).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_IN_MILLIS, OperatorType.GREATER_THAN, TIME_IN_MILLIS, true).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_IN_MILLIS, OperatorType.LESS_THAN, TIME_IN_MILLIS, true).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_IN_MILLIS, OperatorType.IS_BLANK, TIME_DIFF, true).shouldBeFalse()
    }

    @Test
    fun `should compare boolean yield correct result`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                true, OperatorType.EQUALS, true).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
                true, OperatorType.DOES_NOT_EQUAL, true).shouldBeFalse()
    }

    @Test
    fun `should compare string yield correct result`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
                "", OperatorType.EQUALS, TEST_STRING).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
                "", OperatorType.DOES_NOT_EQUAL, TEST_STRING).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
                TEST_STRING, OperatorType.MATCHES_REGEX, TEST_STRING_REGEX).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
                TEST_STRING, OperatorType.DOES_NOT_MATCH_REGEX, TEST_STRING_REGEX).shouldBeFalse()
    }

    @Test
    fun `should be false when compare int with regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(1,
                OperatorType.MATCHES_REGEX, 1).shouldBeFalse()
    }
    @Test
    fun `should be false when compare int with not match regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(1,
                OperatorType.DOES_NOT_MATCH_REGEX, 1).shouldBeFalse()
    }

    @Test
    fun `should be false when compare double with regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(1.0,
                OperatorType.MATCHES_REGEX, 1.0).shouldBeFalse()
    }
    @Test
    fun `should be false when compare double with not match regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(1.0,
                OperatorType.DOES_NOT_MATCH_REGEX, 1.0).shouldBeFalse()
    }

    @Test
    fun `should be false when compare long with regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(1L,
                OperatorType.MATCHES_REGEX, 1L, false).shouldBeFalse()
    }

    @Test
    fun `should be false when compare long with not match regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(1L,
                OperatorType.DOES_NOT_MATCH_REGEX, 1L, false).shouldBeFalse()
    }

    @Test
    fun `should be true when compare empty string`() {
        ValueMatchingUtil.isOperatorConditionSatisfied("",
                OperatorType.IS_BLANK, "").shouldBeTrue()
    }

    @Test
    fun `should be true when compare not empty string`() {
        ValueMatchingUtil.isOperatorConditionSatisfied("xx",
                OperatorType.IS_NOT_BLANK, "xx").shouldBeTrue()
    }

    @Test
    fun `should be false when compare string with greater than`() {
        ValueMatchingUtil.isOperatorConditionSatisfied("xx",
                OperatorType.GREATER_THAN, "xx").shouldBeFalse()
    }

    @Test
    fun `should be false when compare string with less than`() {
        ValueMatchingUtil.isOperatorConditionSatisfied("xx",
                OperatorType.LESS_THAN, "xx").shouldBeFalse()
    }

    @Test
    fun `should be false when compare boolean with regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(true,
                OperatorType.MATCHES_REGEX, true).shouldBeFalse()
    }

    @Test
    fun `should be false when compare boolean with not match regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(true,
                OperatorType.DOES_NOT_MATCH_REGEX, true).shouldBeFalse()
    }

    @Test
    fun `should be false when compare boolean with is blank`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(true,
                OperatorType.IS_BLANK, true).shouldBeFalse()
    }

    @Test
    fun `should be false when compare boolean with is not blank`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(true,
                OperatorType.IS_NOT_BLANK, true).shouldBeFalse()
    }

    @Test
    fun `should be false when compare boolean with greater than`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(true,
                OperatorType.GREATER_THAN, true).shouldBeFalse()
    }

    @Test
    fun `should be false when compare boolean with less than`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(true,
                OperatorType.LESS_THAN, true).shouldBeFalse()
    }

    companion object {
        private const val TEST_STRING = "abc"
        private const val TEST_STRING_REGEX = "(.*)b(.*)"
        private const val TIME_IN_MILLIS = 1553293167L
        private const val TIME_IN_MILLIS_TOLERANCE = 1000
        private const val TIME_DIFF = TIME_IN_MILLIS - TIME_IN_MILLIS_TOLERANCE
        private const val LONG_VALUE = 100001L
        private const val LONG_OTHER_VALUE = 100002L
    }
}
