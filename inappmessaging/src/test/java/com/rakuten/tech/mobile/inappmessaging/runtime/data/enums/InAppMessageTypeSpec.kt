package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import android.os.Build
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class InAppMessageTypeSpec(private val id: Int, private val expected: Any?) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
                name = "{0} type test"
        )
        fun data(): List<Array<out Any?>> {
            return listOf(
                    arrayOf(0, InAppMessageType.INVALID),
                    arrayOf(1, InAppMessageType.MODAL),
                    arrayOf(2, InAppMessageType.FULL),
                    arrayOf(3, InAppMessageType.SLIDE),
                    arrayOf(4, InAppMessageType.HTML),
                    arrayOf(5, InAppMessageType.TOOLTIP),
                    arrayOf(6, null)
            )
        }
    }

    @Test
    fun `should return correct type from id`() {
        InAppMessageType.getById(id) shouldBeEqualTo expected
    }
}
