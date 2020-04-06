package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import android.os.Build
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ImpressionTypeSpec(private val id: Int, private val expected: Any?) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
                name = "{0} type test"
        )
        fun data(): List<Array<out Any?>> {
            return listOf(
                    arrayOf(0, ImpressionType.INVALID),
                    arrayOf(1, ImpressionType.IMPRESSION),
                    arrayOf(2, ImpressionType.ACTION_ONE),
                    arrayOf(3, ImpressionType.ACTION_TWO),
                    arrayOf(4, ImpressionType.EXIT),
                    arrayOf(5, ImpressionType.CLICK_CONTENT),
                    arrayOf(6, ImpressionType.OPT_OUT),
                    arrayOf(7, null)
            )
        }
    }

    @Test
    fun `should return correct type from id`() {
        ImpressionType.getById(id) shouldEqual expected
    }
}
