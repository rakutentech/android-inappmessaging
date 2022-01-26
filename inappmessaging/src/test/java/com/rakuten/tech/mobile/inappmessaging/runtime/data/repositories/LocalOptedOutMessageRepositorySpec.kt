package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test class for LocalOptedOutMessageRepository.
 */
@RunWith(RobolectricTestRunner::class)
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
    fun `should not crash while clearing message`() {
        val message = ValidTestMessage()
        InAppMessaging.setUninitializedInstance(true)
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

    @Test
    fun `should save and restore values for different users`() {
        val message = setupAndTestMultipleUser()
        LocalOptedOutMessageRepository.instance().hasMessage(message.getCampaignId()).shouldBeTrue()
    }

    @Test
    fun `should not crash and clear previous when forced cast exception`() {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)
        PreferencesUtil.putInt(
            ApplicationProvider.getApplicationContext(),
            "internal_shared_prefs_" + AccountRepository.instance().userInfoHash,
            LocalOptedOutMessageRepository.LOCAL_OPTED_OUT_KEY,
            1
        )
        LocalOptedOutMessageRepository.instance().hasMessage(ValidTestMessage().getCampaignId()).shouldBeFalse()
    }

    private fun setupAndTestMultipleUser(): ValidTestMessage {
        val infoProvider = TestUserInfoProvider()
        initializeInstance(infoProvider)

        val message = ValidTestMessage()
        LocalOptedOutMessageRepository.instance().addMessage(message)
        LocalOptedOutMessageRepository.instance().hasMessage(message.getCampaignId()).shouldBeTrue()

        infoProvider.userId = "user2"
        AccountRepository.instance().updateUserInfo()
        LocalOptedOutMessageRepository.instance().hasMessage(message.getCampaignId()).shouldBeFalse()

        // revert to initial user info
        infoProvider.userId = TestUserInfoProvider.TEST_USER_ID
        AccountRepository.instance().updateUserInfo()
        return message
    }

    private fun initializeInstance(infoProvider: UserInfoProvider) {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        InAppMessaging.instance().registerPreference(infoProvider)
    }
}
