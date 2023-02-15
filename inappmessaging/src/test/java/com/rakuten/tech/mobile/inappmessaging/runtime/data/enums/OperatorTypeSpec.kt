package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class OperatorTypeSpec(private val id: Int, private val expected: Any?) {
    @Test
    fun `should return correct type from id`() {
        OperatorType.getById(id) shouldBeEqualTo expected
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0} type test"
        )
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf(0, OperatorType.INVALID),
                arrayOf(1, OperatorType.EQUALS),
                arrayOf(2, OperatorType.DOES_NOT_EQUAL),
                arrayOf(3, OperatorType.GREATER_THAN),
                arrayOf(4, OperatorType.LESS_THAN),
                arrayOf(5, OperatorType.IS_BLANK),
                arrayOf(6, OperatorType.IS_NOT_BLANK),
                arrayOf(7, OperatorType.MATCHES_REGEX),
                arrayOf(8, OperatorType.DOES_NOT_MATCH_REGEX),
                arrayOf(-1, null)
            )
        }
    }
}
