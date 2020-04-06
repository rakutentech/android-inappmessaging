package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import android.os.Build
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class OperatorTypeSpec(private val id: Int, private val expected: Any?) {
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
                    arrayOf(9, null)
            )
        }
    }

    @Test
    fun `should return correct type from id`() {
        OperatorType.getById(id) shouldEqual expected
    }
}
