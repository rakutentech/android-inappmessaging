package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.OperatorType
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test

/**
 * Test class for ValueMatchingUtil.
 */
open class ValueMatchingUtilSpec : BaseTest() {
    /* ---------------------------------- Testing for matching results --------------------------*/
    @Test
    fun `should compare integer yield correct result`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(1, OperatorType.EQUALS, 1).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(1, OperatorType.DOES_NOT_EQUAL, 1).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(1, OperatorType.GREATER_THAN, 1).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(1, OperatorType.LESS_THAN, 1).shouldBeFalse()
    }

    @Test
    fun `should compare double yield correct result`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(DBVAL, OperatorType.EQUALS, DBVAL).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(DBVAL, OperatorType.DOES_NOT_EQUAL, DBVAL).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(DBVAL, OperatorType.GREATER_THAN, DBVAL).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(DBVAL, OperatorType.LESS_THAN, DBVAL).shouldBeFalse()
    }

    @Test
    fun `should compare boolean yield correct result`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(true, OperatorType.EQUALS, true).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(true, OperatorType.DOES_NOT_EQUAL, true).shouldBeFalse()
    }

    @Test
    fun `should compare string yield correct result`() {
        ValueMatchingUtil.isOperatorConditionSatisfied("", OperatorType.EQUALS, STR).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied("", OperatorType.DOES_NOT_EQUAL, STR).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(STR, OperatorType.MATCHES_REGEX, STR_REGEX).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            STR, OperatorType.DOES_NOT_MATCH_REGEX, STR_REGEX
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when compare int with regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(1, OperatorType.MATCHES_REGEX, 1).shouldBeFalse()
    }

    @Test
    fun `should be false when compare int with not match regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(1, OperatorType.DOES_NOT_MATCH_REGEX, 1).shouldBeFalse()
    }

    @Test
    fun `should be false when compare double with regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(1.0, OperatorType.MATCHES_REGEX, 1.0).shouldBeFalse()
    }

    @Test
    fun `should be false when compare double with not match regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            1.0, OperatorType.DOES_NOT_MATCH_REGEX, 1.0
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when compare long with regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            1L, OperatorType.MATCHES_REGEX, 1L, false
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when compare long with not match regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            1L, OperatorType.DOES_NOT_MATCH_REGEX, 1L, false
        ).shouldBeFalse()
    }

    @Test
    fun `should be true when compare empty string`() {
        ValueMatchingUtil.isOperatorConditionSatisfied("", OperatorType.IS_BLANK, "").shouldBeTrue()
    }

    @Test
    fun `should be true when compare not empty string`() {
        ValueMatchingUtil.isOperatorConditionSatisfied("xx", OperatorType.IS_NOT_BLANK, "xx").shouldBeTrue()
    }

    @Test
    fun `should be false when compare string with greater than`() {
        ValueMatchingUtil.isOperatorConditionSatisfied("xx", OperatorType.GREATER_THAN, "xx").shouldBeFalse()
    }

    @Test
    fun `should be false when compare string with less than`() {
        ValueMatchingUtil.isOperatorConditionSatisfied("xx", OperatorType.LESS_THAN, "xx").shouldBeFalse()
    }

    @Test
    fun `should be false when compare boolean with regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(true, OperatorType.MATCHES_REGEX, true).shouldBeFalse()
    }

    @Test
    fun `should be false when compare boolean with not match regex`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            true, OperatorType.DOES_NOT_MATCH_REGEX, true
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when compare boolean with is blank`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(true, OperatorType.IS_BLANK, true).shouldBeFalse()
    }

    @Test
    fun `should be false when compare boolean with is not blank`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(true, OperatorType.IS_NOT_BLANK, true).shouldBeFalse()
    }

    @Test
    fun `should be false when compare boolean with greater than`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(true, OperatorType.GREATER_THAN, true).shouldBeFalse()
    }

    @Test
    fun `should be false when compare boolean with less than`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(true, OperatorType.LESS_THAN, true).shouldBeFalse()
    }

    companion object {
        private const val STR = "abc"
        private const val STR_REGEX = "(.*)b(.*)"
        private const val DBVAL = 1.00001
    }
}

class ValueMatchingUtilNullSpec : ValueMatchingUtilSpec() {
    /* ---------------------------------- Testing for null arguments --------------------------*/
    @Test
    fun `should be false when integer event value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            null, OperatorType.EQUALS, 1
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when integer operator type argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            1, null, 1
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when integer trigger value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            1, OperatorType.EQUALS, null
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when long event value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            null, OperatorType.EQUALS, 1L, true
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when long operator type argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            1L, null, 1L, true
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when long trigger value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            1L, OperatorType.EQUALS, null, true
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when double event value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            null, OperatorType.EQUALS, 1.1
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when double operator type argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            1.1, null, 1.1
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when double trigger value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            1.1, OperatorType.EQUALS, null
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when boolean event value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            null, OperatorType.EQUALS, true
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when boolean operator type argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            true, null, true
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when boolean trigger value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            true, OperatorType.EQUALS, null
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when string event value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            null, OperatorType.EQUALS, ""
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when string operator type argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            "", null, ""
        ).shouldBeFalse()
    }

    @Test
    fun `should be false when string trigger value argument is null`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            "", OperatorType.EQUALS, null
        ).shouldBeFalse()
    }
}

class ValueMatchingUtilLongSpec : ValueMatchingUtilSpec() {
    @Test
    fun `should compare non time long yield true`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(LONG_VAL, OperatorType.EQUALS, LONG_VAL, false).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_VAL, OperatorType.DOES_NOT_EQUAL, LONG_OT_VALUE, false
        ).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_OT_VALUE, OperatorType.GREATER_THAN, LONG_VAL, false
        ).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_VAL, OperatorType.LESS_THAN, LONG_OT_VALUE, false
        ).shouldBeTrue()
    }

    @Test
    fun `should compare non time long yield false`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_VAL, OperatorType.EQUALS, LONG_OT_VALUE, false
        ).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_VAL, OperatorType.DOES_NOT_EQUAL, LONG_VAL, false
        ).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_VAL, OperatorType.GREATER_THAN, LONG_VAL, false
        ).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            LONG_VAL, OperatorType.LESS_THAN, LONG_VAL, false
        ).shouldBeFalse()
    }

    @Test
    fun `should compare time long yield true`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(TIME_MS, OperatorType.EQUALS, TIME_DIFF, true).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_MS, OperatorType.DOES_NOT_EQUAL, TIME_MS - 2 * TIME_TOL, true
        ).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_MS * 2, OperatorType.GREATER_THAN, TIME_DIFF, true
        ).shouldBeTrue()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_MS / 2, OperatorType.LESS_THAN, TIME_DIFF, true
        ).shouldBeTrue()
    }

    @Test
    fun `should compare time long yield false`() {
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_MS, OperatorType.EQUALS, TIME_MS - 2 * TIME_TOL, true
        ).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_MS, OperatorType.DOES_NOT_EQUAL, TIME_DIFF, true
        ).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_MS, OperatorType.GREATER_THAN, TIME_DIFF, true
        ).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_MS, OperatorType.LESS_THAN, TIME_DIFF, true
        ).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(
            TIME_MS, OperatorType.GREATER_THAN, TIME_MS, true
        ).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(TIME_MS, OperatorType.LESS_THAN, TIME_MS, true).shouldBeFalse()
        ValueMatchingUtil.isOperatorConditionSatisfied(TIME_MS, OperatorType.IS_BLANK, TIME_DIFF, true).shouldBeFalse()
    }

    companion object {
        private const val TIME_MS = 1553293167L
        private const val TIME_TOL = 1000
        private const val TIME_DIFF = TIME_MS - TIME_TOL
        private const val LONG_VAL = 100001L
        private const val LONG_OT_VALUE = 100002L
    }
}
