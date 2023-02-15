package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ButtonActionTypeSpec(private val id: Int, private val expected: Any?) {
    @Test
    fun `should return correct type from id`() {
        ButtonActionType.getById(id) shouldBeEqualTo expected
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0} type test"
        )
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf(0, ButtonActionType.INVALID),
                arrayOf(1, ButtonActionType.REDIRECT),
                arrayOf(2, ButtonActionType.DEEPLINK),
                arrayOf(3, ButtonActionType.CLOSE),
                arrayOf(4, ButtonActionType.PUSH_PRIMER),
                arrayOf(-1, null)
            )
        }
    }
}
