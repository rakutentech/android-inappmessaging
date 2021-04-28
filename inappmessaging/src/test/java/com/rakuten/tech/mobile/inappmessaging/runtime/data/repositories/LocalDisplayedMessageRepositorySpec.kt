package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.*
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
import kotlin.test.fail

/**
 * Test class for LocalDisplayedMessageRepository.
 */
@RunWith(RobolectricTestRunner::class)
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
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test-key", "",
                isForTesting = true)

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

    @Test
    fun `should save and restore values for different users`() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)

        val message = ValidTestMessage()
        LocalDisplayedMessageRepository.instance().addMessage(message)
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 1

        infoProvider.rakutenId = "user2"
        AccountRepository.instance().updateUserInfo()
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 0

        // revert to initial user info
        infoProvider.rakutenId = TestUserInfoProvider.TEST_RAKUTEN_ID
        AccountRepository.instance().updateUserInfo()
        LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message) shouldBeEqualTo 1
    }

    @Test
    fun `should return zero for times closed when not first launch`() {
        initializeInstance(TestUserInfoProvider())
        LocalDisplayedMessageRepository.isInitialLaunch = false
        LocalDisplayedMessageRepository.instance().setRemovedMessage("1234")

        LocalDisplayedMessageRepository.instance().numberOfTimesClosed("1234") shouldBeEqualTo 0
    }

    @Test
    fun `should return correct number of times closed for removed when first launch`() {
        initializeInstance(TestUserInfoProvider(), false)
        LocalDisplayedMessageRepository.isInitialLaunch = true
        LocalDisplayedMessageRepository.instance().setRemovedMessage("1234")

        LocalDisplayedMessageRepository.instance().numberOfTimesClosed("1234") shouldBeEqualTo 1
        // should not increment since no longer initial launch
        LocalDisplayedMessageRepository.instance().numberOfTimesClosed("1234") shouldBeEqualTo 1

        // should increment since initial launch
        LocalDisplayedMessageRepository.isInitialLaunch = true
        LocalDisplayedMessageRepository.instance().numberOfTimesClosed("1234") shouldBeEqualTo 2
    }

    @Test
    fun `should return false for removed when first launch and no removed messages`() {
        initializeInstance(TestUserInfoProvider(), false)
        LocalDisplayedMessageRepository.isInitialLaunch = true
        LocalDisplayedMessageRepository.instance().setRemovedMessage("")

        LocalDisplayedMessageRepository.instance().numberOfTimesClosed("1234") shouldBeEqualTo 0

        LocalDisplayedMessageRepository.instance().setRemovedMessage(null)

        LocalDisplayedMessageRepository.instance().numberOfTimesClosed("1234") shouldBeEqualTo 0
    }

    @Test
    fun `should return false not the same campaign id`() {
        initializeInstance(TestUserInfoProvider(), false)
        LocalDisplayedMessageRepository.isInitialLaunch = true
        LocalDisplayedMessageRepository.instance().setRemovedMessage("1234")

        LocalDisplayedMessageRepository.instance().numberOfTimesClosed("4321") shouldBeEqualTo 0
    }

    private fun initializeInstance(infoProvider: UserInfoProvider, isCache: Boolean = true) {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test", "",
                isDebugLogging = true, isForTesting = true, isCacheHandling = isCache)
        InAppMessaging.instance().registerPreference(infoProvider)
    }
}
