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

    @Before
    fun setup() {
        PingResponseMessageRepository.instance().clearMessages()
    }

    @Test
    fun `should add message list with valid list`() {
        val messageList = ArrayList<Message>()
        val message0 = ValidTestMessage()
        val message1 = ValidTestMessage("1234")
        messageList.add(message0)
        messageList.add(message1)
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
        val messageList = ArrayList<Message>()
        val message0 = ValidTestMessage()
        val message1 = ValidTestMessage("1234")
        messageList.add(message0)
        messageList.add(message1)
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        PingResponseMessageRepository.instance().clearMessages()
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)
    }

    @Test
    fun `should contain correct messages after clearing then adding`() {
        val messageList = ArrayList<Message>()
        val message0 = ValidTestMessage()
        val message1 = ValidTestMessage("1234")
        messageList.add(message0)
        messageList.add(message1)
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        PingResponseMessageRepository.instance().clearMessages()
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)

        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        PingResponseMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
    }
}
