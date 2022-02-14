package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.app.Activity
import android.content.res.Resources
import android.view.View
import android.widget.ScrollView
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldHaveSize
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TooltipMessageRepositorySpec : BaseTest() {
    private val tooltip = Tooltip("target", "top-center", "testurl", 5)
    private val message0 = ValidTestMessage(type = InAppMessageType.TOOLTIP.typeId, tooltip = tooltip)
    private val message1 = ValidTestMessage("123", type = InAppMessageType.TOOLTIP.typeId, tooltip = tooltip)
    private val message3 = ValidTestMessage("456", type = InAppMessageType.TOOLTIP.typeId)
    private val messageList = ArrayList<Message>()
    private val mockActivity = Mockito.mock(Activity::class.java)
    private val mockView = Mockito.mock(View::class.java)
    private val mockResource = Mockito.mock(Resources::class.java)

    @Before
    override fun setup() {
        super.setup()
        TooltipMessageRepository.instance().clearMessages()
        InAppMessaging
        messageList.clear()
        messageList.add(message0)
        messageList.add(message1)
        messageList.add(message3)
        PingResponseMessageRepository.instance().clearMessages()
        PingResponseMessageRepository.instance().replaceAllMessages(messageList)
        InAppMessaging.init(ApplicationProvider.getApplicationContext())
        InAppMessaging.instance().registerMessageDisplayActivity(mockActivity)
        `when`(mockActivity.packageName).thenReturn("test")
        `when`(mockActivity.resources).thenReturn(mockResource)
        `when`(mockResource.getIdentifier(eq("target"), eq("id"), any())).thenReturn(1)
        `when`(mockActivity.findViewById<View>(1)).thenReturn(mockView)
    }

    @Test
    fun `should add message list with valid list`() {
        verifyValidMessageList()
    }

    @Test
    fun `should not throw exception when list is empty`() {
        TooltipMessageRepository.instance().replaceAllMessages(ArrayList())
    }

    @Test
    fun `should return correct message`() {
        TooltipMessageRepository.instance().replaceAllMessages(messageList)
        TooltipMessageRepository.instance().getCampaign("123").shouldBeEqualTo(message1)
        TooltipMessageRepository.instance().getCampaign("321").shouldBeNull()
    }

    @Test
    fun `should be empty after clearing`() {
        TooltipMessageRepository.instance().replaceAllMessages(messageList)
        TooltipMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        TooltipMessageRepository.instance().clearMessages()
        TooltipMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)
    }

    @Test
    fun `should contain correct messages after clearing then adding`() {
        TooltipMessageRepository.instance().replaceAllMessages(messageList)
        TooltipMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)

        TooltipMessageRepository.instance().clearMessages()
        TooltipMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)

        TooltipMessageRepository.instance().replaceAllMessages(messageList)
        TooltipMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
    }

    @Test
    fun `should remove matching message and increment times closed`() {
        TooltipMessageRepository.instance().replaceAllMessages(messageList)
        TooltipMessageRepository.instance().removeMessage(message0.getCampaignId(), true)
        TooltipMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(1)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            if (msg.getCampaignId() == message0.getCampaignId()) {
                msg.getNumberOfTimesClosed() shouldBeEqualTo 1
            } else {
                msg.getNumberOfTimesClosed() shouldBeEqualTo 0
            }
        }
    }

    @Test
    fun `should remove matching message but not increment`() {
        TooltipMessageRepository.instance().replaceAllMessages(messageList)
        TooltipMessageRepository.instance().removeMessage(message0.getCampaignId(), false)
        TooltipMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(1)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
        }
    }

    @Test
    fun `should not remove and not increment when no match`() {
        TooltipMessageRepository.instance().replaceAllMessages(messageList)
        TooltipMessageRepository.instance().removeMessage("4321", false)
        TooltipMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
        }
    }

    @Test
    fun `should not remove and not increment when no match and flag true`() {
        TooltipMessageRepository.instance().replaceAllMessages(messageList)
        TooltipMessageRepository.instance().removeMessage("4321", true)
        TooltipMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(2)
        for (msg in PingResponseMessageRepository.instance().getAllMessagesCopy()) {
            msg.getNumberOfTimesClosed() shouldBeEqualTo 0
        }
    }

    @Test
    fun `should return message list with visible scrollview`() {
        `when`(mockView.parent).thenReturn(Mockito.mock(ScrollView::class.java))
        `when`(mockView.getLocalVisibleRect(anyOrNull())).thenReturn(true)
        verifyValidMessageList()
    }

    @Test
    fun `should return empty list with not visible scrollview`() {
        `when`(mockView.parent).thenReturn(Mockito.mock(ScrollView::class.java))
        `when`(mockView.getLocalVisibleRect(anyOrNull())).thenReturn(false)
        TooltipMessageRepository.instance().replaceAllMessages(messageList)
        TooltipMessageRepository.instance().getAllMessagesCopy().shouldBeEmpty()
    }

    @Test
    fun `should return empty list with no registered activity`() {
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        TooltipMessageRepository.instance().replaceAllMessages(messageList)
        TooltipMessageRepository.instance().getAllMessagesCopy().shouldBeEmpty()
    }

    private fun verifyValidMessageList() {
        TooltipMessageRepository.instance().replaceAllMessages(messageList)
        val list = TooltipMessageRepository.instance().getAllMessagesCopy()
        list.shouldHaveSize(2)
        list[0] shouldBeEqualTo message0
        list[1] shouldBeEqualTo message1
    }
}
