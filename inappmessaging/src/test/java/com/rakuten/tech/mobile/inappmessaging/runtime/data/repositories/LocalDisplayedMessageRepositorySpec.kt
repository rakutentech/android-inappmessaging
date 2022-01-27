package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.rakuten.tech.mobile.inappmessaging.runtime.*
import com.rakuten.tech.mobile.inappmessaging.runtime.coroutine.MessageActionsCoroutine
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.InvalidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.ControlSettings
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageSettings
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.SharedPreferencesUtil.getPreferencesFile
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import java.util.*

/**
 * Test class for LocalDisplayedMessageRepository.
 */
@SuppressWarnings("LargeClass")
@RunWith(RobolectricTestRunner::class)
class LocalDisplayedMessageRepositorySpec : BaseTest() {

    private val function: (ex: Exception) -> Unit = {}
    private val mockCallback = Mockito.mock(function.javaClass)
    private val captor = argumentCaptor<InAppMessagingException>()

    @Before
    override fun setup() {
        super.setup()
        LocalDisplayedMessageRepository.instance().clearMessages()
    }

    @Test
    fun `should not crash when message's campaign ID is empty and no callback`() {
        LocalDisplayedMessageRepository.instance().addMessage(InvalidTestMessage())
    }

    @Test
    fun `should invoke callback when message's campaign ID is empty`() {
        InApp.errorCallback = mockCallback
        LocalDisplayedMessageRepository.instance().addMessage(InvalidTestMessage())

        Mockito.verify(mockCallback).invoke(captor.capture())
        captor.firstValue shouldBeInstanceOf InAppMessagingException::class.java
    }

    @Test
    fun `should return valid timestamp list`() {
        val message = ValidTestMessage()
        LocalDisplayedMessageRepository.instance().addMessage(message)
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 1
    }

    @Test
    fun `should throw exception with empty campaign id`() {
        InApp.errorCallback = mockCallback
        LocalDisplayedMessageRepository.instance().addMessage(ValidTestMessage(""))

        Mockito.verify(mockCallback).invoke(captor.capture())
        captor.firstValue shouldBeInstanceOf InAppMessagingException::class.java
    }

    @Test
    @SuppressWarnings("LongMethod")
    fun `should be called once`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)

        val mockRepo = Mockito.mock(LocalDisplayedMessageRepository::class.java)

        val message = Mockito.mock(Message::class.java)
        val payload = Mockito.mock(MessagePayload::class.java)
        val msgSettings = Mockito.mock(MessageSettings::class.java)
        val settings = Mockito.mock(ControlSettings::class.java)
        `when`(message.getCampaignId()).thenReturn("id")
        `when`(message.getMessagePayload()).thenReturn(payload)
        `when`(payload.messageSettings).thenReturn(msgSettings)
        `when`(msgSettings.controlSettings).thenReturn(settings)
        `when`(settings.buttons).thenReturn(listOf())
        MessageActionsCoroutine(mockRepo).executeTask(message, R.id.message_close_button, true)

        Mockito.verify(mockRepo).addMessage(message)
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
    fun `should not crash while clearing messages`() {
        val message = ValidTestMessage()
        InAppMessaging.setUninitializedInstance(true)
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

    @Test
    fun `should save and restore values for different users`() {
        val message = setupAndTestMultipleUser()
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 1
    }

    @Test
    fun `should not crash and clear previous when forced cast exception`() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)
        PreferencesUtil.putLong(
            ApplicationProvider.getApplicationContext(),
            getPreferencesFile(),
            LocalDisplayedMessageRepository.LOCAL_DISPLAYED_KEY, 10L
        )
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(ValidTestMessage()) shouldBeEqualTo 0
    }

    @Test
    fun `should not crash and clear previous when invalid format`() {
        val message = setupAndTestMultipleUser()
        PreferencesUtil.putString(
            ApplicationProvider.getApplicationContext(), getPreferencesFile(),
            LocalDisplayedMessageRepository.LOCAL_DISPLAYED_KEY, "invalid"
        )
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 0
    }

    @Test
    fun `should not crash and reset displayed map`() {
        val message = setupAndTestMultipleUser()
        InAppMessaging.setUninitializedInstance(true)
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 0
    }

    @Test
    @Synchronized
    fun `should return valid timestamp list after last ping`() {
        val message = ValidTestMessage()
        PingResponseMessageRepository.instance().lastPingMillis = Calendar.getInstance().timeInMillis
        LocalDisplayedMessageRepository.instance().addMessage(message)
        LocalDisplayedMessageRepository.instance().numberOfDisplaysAfterPing(message) shouldBeEqualTo 1
    }

    @Test
    @Synchronized
    fun `should return empty timestamp list after last ping`() {
        val message = ValidTestMessage()
        LocalDisplayedMessageRepository.instance().addMessage(message)
        PingResponseMessageRepository.instance().lastPingMillis = Calendar.getInstance().timeInMillis + 5
        LocalDisplayedMessageRepository.instance().numberOfDisplaysAfterPing(message) shouldBeEqualTo 0
    }

    @Test
    @Synchronized
    fun `should return valid timestamp list when ping response time is between adding two messages`() {
        val message = ValidTestMessage()
        LocalDisplayedMessageRepository.instance().addMessage(message)
        Thread.sleep(1)
        PingResponseMessageRepository.instance().lastPingMillis = Calendar.getInstance().timeInMillis
        LocalDisplayedMessageRepository.instance().addMessage(message)
        LocalDisplayedMessageRepository.instance().numberOfDisplaysAfterPing(message) shouldBeEqualTo 1
    }

    private fun setupAndTestMultipleUser(): ValidTestMessage {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)

        val message = ValidTestMessage()
        LocalDisplayedMessageRepository.instance().addMessage(message)
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 1

        infoProvider.userId = "user2"
        AccountRepository.instance().updateUserInfo()
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 0

        // revert to initial user info
        infoProvider.userId = TestUserInfoProvider.TEST_USER_ID
        AccountRepository.instance().updateUserInfo()
        return message
    }

    private fun initializeInstance(infoProvider: UserInfoProvider, isCache: Boolean = true) {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), isCache)
        InAppMessaging.instance().registerPreference(infoProvider)
    }
}
