package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import android.os.Build
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ValueTypeSpec(private val id: Int, private val expected: Any?) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
                name = "{0} type test"
        )
        fun data(): List<Array<out Any?>> {
            return listOf(
                    arrayOf(0, ValueType.INVALID),
                    arrayOf(1, ValueType.STRING),
                    arrayOf(2, ValueType.INTEGER),
                    arrayOf(3, ValueType.DOUBLE),
                    arrayOf(4, ValueType.BOOLEAN),
                    arrayOf(5, ValueType.TIME_IN_MILLI),
                    arrayOf(6, null)
            )
        }
    }

    @Test
    fun `should return correct type from id`() {
        ValueType.getById(id) shouldBeEqualTo expected
    }
}
