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
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.InvalidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalDisplayedMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ReadyForDisplayMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.TooltipMessageRepository
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
    private val isOpt: Boolean,
    private val isTooltip: Boolean
) : BaseTest() {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
                name = "{0} testing"
        )
        fun data(): Collection<Array<Any>> {
            return listOf(
                    arrayOf("Close button - isOpt true", R.id.message_close_button, true, false),
                    arrayOf("Close button - isOpt false", R.id.message_close_button, false, false),
                    arrayOf("Single button - isOpt true", R.id.message_single_button, true, false),
                    arrayOf("Single button - isOpt false", R.id.message_single_button, false, false),
                    arrayOf("Right button - isOpt true", R.id.message_button_right, true, false),
                    arrayOf("Right button - isOpt false", R.id.message_button_right, false, false),
                    arrayOf("Left button - isOpt true", R.id.message_button_left, true, false),
                    arrayOf("Left button - isOpt false", R.id.message_button_left, false, false),
                    arrayOf("Content - isOpt true", R.id.slide_up, true, false),
                    arrayOf("Content - isOpt false", R.id.slide_up, false, false),
                    arrayOf("Back - isOpt true", MessageActionsCoroutine.BACK_BUTTON, true, false),
                    arrayOf("Back - isOpt false", MessageActionsCoroutine.BACK_BUTTON, false, false),
                    arrayOf("Tooltip View - content", R.id.message_tooltip_image_view, false, true),
                    arrayOf("Tooltip Tip- content", R.id.message_tip, false, true),
                    arrayOf("Tooltip Tip- content", -99, false, true)
            )
        }
    }

    private val message = MessageMixerResponseSpec.response.data[0].campaignData
    private val activity = Mockito.mock(Activity::class.java)
    private val type = MessageActionsCoroutine.getOnClickBehaviorType(resourceId)

    @Before
    override fun setup() {
        super.setup()
        `when`(activity.packageManager).thenReturn(ApplicationProvider
                .getApplicationContext<Context>().packageManager)

        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
    }

    @Test
    fun `should return false when message is null`() {
        DisplayManager.instance().removeMessage(InAppMessaging.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(null, type, isOpt)
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when campaign id is null`() {
        DisplayManager.instance().removeMessage(InAppMessaging.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(InvalidTestMessage(), type, isOpt)
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when campaign id is empty`() {
        DisplayManager.instance().removeMessage(InAppMessaging.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(ValidTestMessage(""), type, isOpt)
        result.shouldBeFalse()
    }

    @Test
    fun `should add message to display repo`() {
        val numberOfTimesDisplayed: Int = LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message)
        DisplayManager.instance().removeMessage(InAppMessaging.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(message, type, isOpt)
        result.shouldBeTrue()
        LocalDisplayedMessageRepository.instance()
                .numberOfTimesDisplayed(message) shouldBeEqualTo numberOfTimesDisplayed + 1
    }

    @Test
    fun `should remove message from ready repo`() {
        val messageList = ArrayList<Message>()
        val msg = if (isTooltip) {
            ValidTestMessage(type = InAppMessageType.TOOLTIP.typeId)
        } else {
            message
        }
        messageList.add(msg)
        if (isTooltip) {
            TooltipMessageRepository.instance().replaceAllMessages(messageList)
            TooltipMessageRepository.instance().getCampaign(ValidTestMessage.DEFAULT_CAMPAIGN_ID).shouldNotBeNull()
        } else {
            ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
            ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(1)
        }

        DisplayManager.instance().removeMessage(InAppMessaging.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(msg, type, isOpt)
        if (result) {
            DisplayManager.instance().displayMessage()
        }
        if (isTooltip) {
            TooltipMessageRepository.instance().getCampaign(ValidTestMessage.DEFAULT_CAMPAIGN_ID).shouldBeNull()
        } else {
            ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)
        }
    }
}
