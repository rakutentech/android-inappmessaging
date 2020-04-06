package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import android.os.Build
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ButtonActionTypeSpec(private val id: Int, private val expected: Any?) {
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
                    arrayOf(4, null)
            )
        }
    }

    @Test
    fun `should return correct type from id`() {
        ButtonActionType.getById(id) shouldEqual expected
    }
}
