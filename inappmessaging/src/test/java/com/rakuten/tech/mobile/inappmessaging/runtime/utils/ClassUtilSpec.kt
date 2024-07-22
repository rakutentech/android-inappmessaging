package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import org.amshove.kluent.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ClassUtilSpec(
    private val className: String,
    private val expected: Boolean,
) {

    @Test
    fun `should return if class exists`() {
        ClassUtil.hasClass(className) shouldBe expected
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
