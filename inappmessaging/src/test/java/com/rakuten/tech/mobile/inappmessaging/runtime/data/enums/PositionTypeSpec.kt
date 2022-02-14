package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import android.os.Build
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class PositionTypeSpec(private val id: String, private val expected: Any?) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0} type test"
        )
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf("top-right", PositionType.TOP_RIGHT),
                arrayOf("top-center", PositionType.TOP_CENTER),
                arrayOf("top-left", PositionType.TOP_LEFT),
                arrayOf("bottom-right", PositionType.BOTTOM_RIGHT),
                arrayOf("bottom-center", PositionType.BOTTOM_CENTER),
                arrayOf("bottom-left", PositionType.BOTTOM_LEFT),
                arrayOf("right", PositionType.RIGHT),
                arrayOf("left", PositionType.LEFT),
                arrayOf("invalid", null)
            )
        }
    }

    @Test
    fun `should return correct type from id`() {
        PositionType.getById(id) shouldBeEqualTo expected
    }
}
