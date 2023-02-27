package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class CampaignTypeSpec(private val id: Int, private val expected: Any?) {
    @Test
    fun `should return correct type from id`() {
        CampaignType.getById(id) shouldBeEqualTo expected
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0} type test",
        )
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf(0, CampaignType.INVALID),
                arrayOf(1, CampaignType.REGULAR),
                arrayOf(2, CampaignType.PUSH_PRIMER),
                arrayOf(3, null),
            )
        }
    }
}
