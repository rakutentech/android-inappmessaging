package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class EventTypeSpec(private val id: Int, private val expected: Any?) {
    @Test
    fun `should return correct type from id`() {
        EventType.getById(id) shouldBeEqualTo expected
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0} type test",
        )
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf(0, EventType.INVALID),
                arrayOf(1, EventType.APP_START),
                arrayOf(2, EventType.LOGIN_SUCCESSFUL),
                arrayOf(3, EventType.PURCHASE_SUCCESSFUL),
                arrayOf(4, EventType.CUSTOM),
                arrayOf(-1, null),
            )
        }
    }
}
