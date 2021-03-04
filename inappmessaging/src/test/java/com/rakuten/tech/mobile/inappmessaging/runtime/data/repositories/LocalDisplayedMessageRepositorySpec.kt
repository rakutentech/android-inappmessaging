package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.coroutine.MessageActionsCoroutine
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.InvalidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.amshove.kluent.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.fail

/**
 * Test class for LocalDisplayedMessageRepository.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class LocalDisplayedMessageRepositorySpec : BaseTest() {

    @Before
    fun setup() {
        LocalDisplayedMessageRepository.instance().clearMessages()
    }

    @Test
    fun `should throw exception when message's campaign ID is empty`() {
        try {
            LocalDisplayedMessageRepository.instance().addMessage(InvalidTestMessage())
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            e.localizedMessage shouldBeEqualTo InAppMessagingConstants.ARGUMENT_IS_EMPTY_EXCEPTION
        }
    }

    @Test
    fun `should return valid timestamp list`() {
        val message = ValidTestMessage()
        LocalDisplayedMessageRepository.instance().addMessage(message)
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 1
    }

    @Test
    fun `should throw exception with null message`() {
        try {
            LocalDisplayedMessageRepository.instance().addMessage(null)
            fail("should throw exception")
        } catch (e: IllegalArgumentException) {
            e.localizedMessage shouldBeEqualTo InAppMessagingConstants.ARGUMENT_IS_NULL_EXCEPTION
        }
    }

    @Test
    fun `should throw exception with empty campaign id`() {
        try {
            LocalDisplayedMessageRepository.instance().addMessage(ValidTestMessage(""))
            fail("should throw exception")
        } catch (e: IllegalArgumentException) {
            e.localizedMessage shouldBeEqualTo InAppMessagingConstants.ARGUMENT_IS_EMPTY_EXCEPTION
        }
    }

    @Test
    fun `should be called once`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test-key", "")

        val mockRepo = Mockito.mock(LocalDisplayedMessageRepository::class.java)

        val message = Mockito.mock(Message::class.java)
        When calling message.getCampaignId() itReturns "id"
        MessageActionsCoroutine(mockRepo).executeTask(message, R.id.message_close_button, true)

        Mockito.verify(mockRepo, Mockito.times(1)).addMessage(message)
    }

    @Test
    fun `should return zero after clearing`() {
        val message = ValidTestMessage()
        LocalDisplayedMessageRepository.instance().addMessage(message)
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 1

        LocalDisplayedMessageRepository.instance().clearMessages()
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 0
    }

    @Test
    fun `should return one after clearing then adding`() {
        val message = ValidTestMessage()
        LocalDisplayedMessageRepository.instance().addMessage(message)
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 1

        LocalDisplayedMessageRepository.instance().clearMessages()
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 0

        LocalDisplayedMessageRepository.instance().addMessage(message)
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 1
    }
}
