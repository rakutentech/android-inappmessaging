package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class SlideFromDirectionTypeSpec(private val id: Int, private val expected: Any?) {
    @Test
    fun `should return correct type from id`() {
        SlideFromDirectionType.getById(id) shouldBeEqualTo expected
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0} type test"
        )
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf(0, SlideFromDirectionType.INVALID),
                arrayOf(1, SlideFromDirectionType.BOTTOM),
                arrayOf(2, SlideFromDirectionType.TOP),
                arrayOf(3, SlideFromDirectionType.LEFT),
                arrayOf(4, SlideFromDirectionType.RIGHT),
                arrayOf(5, SlideFromDirectionType.INVALID)
            )
        }
    }
}
