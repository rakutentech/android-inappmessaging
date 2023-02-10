package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.displaypermission

import com.google.gson.Gson
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class DisplayPermissionResponseSpec(
    private val testname: String,
    private val actual: Boolean,
    private val expected: Boolean
) {
    @Test
    fun `should be correct value after parsing`() {
        actual shouldBeEqualTo expected
    }

    companion object {
        private const val DISPLAY_RESPONSE = """
            {
                "display":true,
                "performPing":false
            }"""

        private const val DISPLAY_OTHER_RESPONSE = """
            {
                "display":false,
                "performPing":true
            }"""

        private val response =
            Gson().fromJson(DISPLAY_RESPONSE.trimIndent(), DisplayPermissionResponse::class.java)

        private val otherResponse =
            Gson().fromJson(DISPLAY_OTHER_RESPONSE.trimIndent(), DisplayPermissionResponse::class.java)

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0} type test"
        )
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf("display-true", response.display, true),
                arrayOf("display-false", otherResponse.display, false),
                arrayOf("performPing-true", otherResponse.performPing, true),
                arrayOf("performPing-false", response.performPing, false)
            )
        }
    }
}
