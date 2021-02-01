package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Test class for PingResponseMessageRepository.
 */
class PingResponseMessageRepositorySpec : BaseTest() {
    private val message0 = ValidTestMessage()
    private val message1 = ValidTestMessage("1234")
    private val messageList = ArrayList<Message>()

    @Before
    fun setup() {
        PingResponseMessageRepository.instance().clearMessages()
        messageList.clear()
        messageList.add(message0)
        messageList.add(message1)
    }

    @Test
    fun `should add message list with valid list`() {
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
        PingResponseMessageRepository.instance().getAllMessagesCopy()[0] shouldBeEqualTo message0
        PingResponseMessageRepository.instance().getAllMessagesCopy()[1] shouldBeEqualTo message1
    }

    @Test
    fun `should throw exception when list is null`() {
        try {
            PingResponseMessageRepository.instance().replaceAllMessages(null)
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            e.localizedMessage shouldBeEqualTo InAppMessagingConstants.ARGUMENT_IS_NULL_EXCEPTION
        }
    }

    @Test
    fun `should not throw exception when list is empty`() {
        PingResponseMessageRepository.instance().replaceAllMessages(ArrayList())
    }

    @Test
    fun `should be empty after clearing`() {
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        PingResponseMessageRepository.instance().clearMessages()
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)
    }

    @Test
    fun `should contain correct messages after clearing then adding`() {
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        PingResponseMessageRepository.instance().clearMessages()
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)

        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
    }

    @Test
    fun `should increment all matching from single item`() {
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().incrementTimesClosed(listOf(message0))
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            if (msg.getCampaignId() != "1234") {
                msg.getNumberOfTimesClosed() shouldBeEqualTo 1
            } else {
                msg.getNumberOfTimesClosed() shouldBeEqualTo 0
            }
        }
    }

    @Test
    fun `should increment all matching from multiple items`() {
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().incrementTimesClosed(messageList)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 1
        }
    }

    @Test
    fun `should not increment if no matching`() {
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().incrementTimesClosed(listOf(ValidTestMessage("4321")))
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
        }
    }
}
