package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class CampaignDataSpec {

    private val mockPayload = Mockito.mock(MessagePayload::class.java)
    private val campaign = CampaignData(mockPayload, InAppMessageType.MODAL.typeId, null, "test", false)

    @Before
    fun setup() {
        `when`(mockPayload.title).thenReturn("${CampaignData.TOOLTIP_TAG} any")
        `when`(mockPayload.messageBody).thenReturn(TOOLTIP_CONFIG.trim())
    }

    @Test
    fun `should return null tooltip config for normal campaign`() {
        `when`(mockPayload.title).thenReturn("any")
        verifyInvalidTooltip()
    }

    @Test
    fun `should return null tooltip config for non-prefix tag`() {
        `when`(mockPayload.title).thenReturn("any ${CampaignData.TOOLTIP_TAG}")
        verifyInvalidTooltip()
    }

    @Test
    fun `should return valid tooltip config with correct format`() {
        val tooltip = campaign.getTooltipConfig()
        campaign.setMaxImpression(5)
        tooltip.shouldNotBeNull()
        tooltip.id shouldBeEqualTo "target"
        tooltip.position shouldBeEqualTo "top-center"
        tooltip.url shouldBeEqualTo "testurl"
        tooltip.autoDisappear shouldBeEqualTo 5
        campaign.getType() shouldBeEqualTo InAppMessageType.TOOLTIP.typeId
        campaign.getMaxImpressions() shouldBeEqualTo 5
        campaign.getTooltipConfig() shouldBeEqualTo tooltip
    }

    @Test
    fun `should return null tooltip config for missing id`() {
        `when`(mockPayload.messageBody).thenReturn(TOOLTIP_MISSING_ID.trim())
        verifyInvalidTooltip()
    }

    @Test
    fun `should return null tooltip config for missing position`() {
        `when`(mockPayload.messageBody).thenReturn(TOOLTIP_MISSING_POS.trim())
        verifyInvalidTooltip()
    }

    @Test
    fun `should return null tooltip config for invalid format`() {
        `when`(mockPayload.messageBody).thenReturn(TOOLTIP_INVALID.trim())
        verifyInvalidTooltip()
    }

    @Test
    fun `should return null tooltip config for null body`() {
        `when`(mockPayload.messageBody).thenReturn(null)
        verifyInvalidTooltip()
    }

    private fun verifyInvalidTooltip() {
        campaign.getTooltipConfig().shouldBeNull()
        campaign.getType() shouldBeEqualTo InAppMessageType.MODAL.typeId
    }

    companion object {
        private const val TOOLTIP_CONFIG = """{
            "UIElement": "target",
            "position": "top-center",
            "redirectURL": "testurl",
            "auto-disappear": 5
        }"""

        private const val TOOLTIP_MISSING_ID = """{
            "position": "top-center",
        }"""

        private const val TOOLTIP_MISSING_POS = """{
            "UIElement": "testtarget"
        }"""

        private const val TOOLTIP_INVALID = """{
            "invalid": "field }
        }"""
    }
}
