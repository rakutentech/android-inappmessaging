package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class CommonUtilSpec(
    private val className: String,
    private val expected: Boolean,
) {
    private val hasClass = CommonUtil::hasClass

    @Test
    fun `should return if class exists`() {
        hasClass(className) shouldBeEqualTo expected
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf("com.rakuten.tech.mobile.analytics.Event", true),
                arrayOf("", false),
                arrayOf("com.rakuten.tech.mobile.NonExistingClass", false),
            )
        }
    }
}
