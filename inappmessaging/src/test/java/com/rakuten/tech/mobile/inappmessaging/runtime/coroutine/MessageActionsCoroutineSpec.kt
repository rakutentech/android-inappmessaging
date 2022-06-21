package com.rakuten.tech.mobile.inappmessaging.runtime.coroutine

import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.InvalidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.CampaignData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageMixerResponseSpec
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for MessageActionsCoroutine
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@SuppressWarnings("LongMethod")
internal class MessageActionsCoroutineSpec(
    val testName: String,
    private val resourceId: Int,
    private val isOpt: Boolean
) : BaseTest() {

    private lateinit var message: CampaignData
    private val activity = Mockito.mock(Activity::class.java)

    @Before
    override fun setup() {
        super.setup()

        // Copy object to not modify internal properties when testing
        message = MessageMixerResponseSpec.response.data[0].campaignData.copy()

        `when`(activity.packageManager).thenReturn(
            ApplicationProvider
                .getApplicationContext<Context>().packageManager
        )

        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id"
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
    }

    @Test
    fun `should return false when message is null`() {
        DisplayManager.instance().removeMessage(InAppMessaging.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(null, resourceId, isOpt)
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when campaign id is null`() {
        DisplayManager.instance().removeMessage(InAppMessaging.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(InvalidTestMessage(), resourceId, isOpt)
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when campaign id is empty`() {
        DisplayManager.instance().removeMessage(InAppMessaging.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(ValidTestMessage(""), resourceId, isOpt)
        result.shouldBeFalse()
    }

    @Test
    fun `should update repo after campaign is displayed`() {
        CampaignRepository.instance().clearMessages()
        CampaignRepository.instance().syncWith(listOf(message), 0)
        val currImpressions = message.impressionsLeft!!
        DisplayManager.instance().removeMessage(InAppMessaging.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(message, resourceId, isOpt)
        val updatedMessage = CampaignRepository.instance().messages.values.first()

        result.shouldBeTrue()
        updatedMessage.impressionsLeft shouldBeEqualTo currImpressions - 1
        updatedMessage.isOptedOut shouldBeEqualTo isOpt
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0} testing"
        )
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("Close button - isOpt true", R.id.message_close_button, true),
                arrayOf("Close button - isOpt false", R.id.message_close_button, false),
                arrayOf("Single button - isOpt true", R.id.message_single_button, true),
                arrayOf("Single button - isOpt false", R.id.message_single_button, false),
                arrayOf("Right button - isOpt true", R.id.message_button_right, true),
                arrayOf("Right button - isOpt false", R.id.message_button_right, false),
                arrayOf("Left button - isOpt true", R.id.message_button_left, true),
                arrayOf("Left button - isOpt false", R.id.message_button_left, false),
                arrayOf("Content - isOpt true", R.id.slide_up, true),
                arrayOf("Content - isOpt false", R.id.slide_up, false),
                arrayOf("Back - isOpt true", MessageActionsCoroutine.BACK_BUTTON, true),
                arrayOf("Back - isOpt false", MessageActionsCoroutine.BACK_BUTTON, false)
            )
        }
    }
}
