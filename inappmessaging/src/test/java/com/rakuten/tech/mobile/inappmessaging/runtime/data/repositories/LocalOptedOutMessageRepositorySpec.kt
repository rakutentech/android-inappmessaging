package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test

/**
 * Test class for LocalOptedOutMessageRepository.
 */
class LocalOptedOutMessageRepositorySpec : BaseTest() {
    @Test
    fun `should add message correctly`() {
        val message = ValidTestMessage()
        LocalOptedOutMessageRepository.instance().addMessage(message)
        LocalOptedOutMessageRepository.instance().hasMessage(message.getCampaignId()).shouldBeTrue()
        LocalOptedOutMessageRepository.instance().hasMessage("123").shouldBeFalse()
    }

    @Test
    fun `should return false after clearing`() {
        val message = ValidTestMessage()
        LocalOptedOutMessageRepository.instance().addMessage(message)
        LocalOptedOutMessageRepository.instance().hasMessage(message.getCampaignId()).shouldBeTrue()

        LocalOptedOutMessageRepository.instance().clearMessages()
        LocalOptedOutMessageRepository.instance().hasMessage(message.getCampaignId()).shouldBeFalse()
    }

    @Test
    fun `should return true after clearing then adding`() {
        val message = ValidTestMessage()
        LocalOptedOutMessageRepository.instance().addMessage(message)
        LocalOptedOutMessageRepository.instance().hasMessage(message.getCampaignId()).shouldBeTrue()

        LocalOptedOutMessageRepository.instance().clearMessages()
        LocalOptedOutMessageRepository.instance().hasMessage(message.getCampaignId()).shouldBeFalse()

        LocalOptedOutMessageRepository.instance().addMessage(message)
        LocalOptedOutMessageRepository.instance().hasMessage(message.getCampaignId()).shouldBeTrue()
    }
}
