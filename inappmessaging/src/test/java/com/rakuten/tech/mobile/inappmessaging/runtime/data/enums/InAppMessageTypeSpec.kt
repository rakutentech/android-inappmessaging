package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class InAppMessageTypeSpec(private val id: Int, private val expected: Any?) {
    @Test
    fun `should return correct type from id`() {
        InAppMessageType.getById(id) shouldBeEqualTo expected
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0} type test",
        )
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf(0, InAppMessageType.INVALID),
                arrayOf(1, InAppMessageType.MODAL),
                arrayOf(2, InAppMessageType.FULL),
                arrayOf(3, InAppMessageType.SLIDE),
                arrayOf(4, InAppMessageType.HTML),
                arrayOf(5, InAppMessageType.TOOLTIP),
                arrayOf(-1, null),
            )
        }
    }
}
