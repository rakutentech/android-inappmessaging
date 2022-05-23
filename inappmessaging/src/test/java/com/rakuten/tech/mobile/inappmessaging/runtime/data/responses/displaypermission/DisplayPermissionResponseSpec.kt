package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.displaypermission

import android.os.Build
import com.rakuten.tech.mobile.inappmessaging.runtime.fromJson
import com.squareup.moshi.Moshi
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
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
            Moshi.Builder().build().fromJson<DisplayPermissionResponse>(data = DISPLAY_RESPONSE.trimIndent())

        private val otherResponse =
            Moshi.Builder().build().fromJson<DisplayPermissionResponse>(data = DISPLAY_OTHER_RESPONSE.trimIndent())

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0} type test"
        )
        @SuppressWarnings("LongMethod")
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf("display-true", response!!.display, true),
                arrayOf("display-false", otherResponse!!.display, false),
                arrayOf("performPing-true", otherResponse.performPing, true),
                arrayOf("performPing-false", response.performPing, false)
            )
        }
    }
}
