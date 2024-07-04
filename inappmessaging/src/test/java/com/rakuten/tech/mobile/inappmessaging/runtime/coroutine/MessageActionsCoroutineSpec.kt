package com.rakuten.tech.mobile.inappmessaging.runtime.coroutine

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.any
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson.MessageMapper
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.PushPrimerTrackerManager
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TestDataHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TooltipHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for MessageActionsCoroutine
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
internal class MessageActionsCoroutineSpec(
    val testName: String,
    private val resourceId: Int,
    private val isOpt: Boolean,
    private val isTooltip: Boolean,
) : BaseTest() {
    private val activity = Mockito.mock(Activity::class.java)

    @Before
    override fun setup() {
        super.setup()

        `when`(activity.packageManager).thenReturn(
            ApplicationProvider
                .getApplicationContext<Context>()
                .packageManager,
        )

        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id",
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
    }

    @Test
    fun `should return false when message is null`() {
        DisplayManager.instance().removeMessage(HostAppInfoRepository.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(null, resourceId, isOpt)
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when campaign id is empty`() {
        DisplayManager.instance().removeMessage(HostAppInfoRepository.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(
            MessageMapper.mapFrom(TestDataHelper.createDummyMessage(campaignId = "")), resourceId, isOpt,
        )
        result.shouldBeFalse()
    }

    @Test
    fun `should return true when campaign is valid`() {
        val message = if (isTooltip) TooltipHelper.createMessage() else TestDataHelper.createDummyMessage()
        DisplayManager.instance().removeMessage(HostAppInfoRepository.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(MessageMapper.mapFrom(message), resourceId, isOpt)
        result.shouldBeTrue()
    }

    @Test
    fun `should update repo after campaign is displayed`() {
        val message = TestDataHelper.createDummyMessage()
        CampaignRepository.instance().clearMessages()
        CampaignRepository.instance().syncWith(listOf(message), 0)
        val readinessManager = MessageReadinessManager.instance()
        readinessManager.clearMessages()
        readinessManager.addMessageToQueue(message.campaignId)
        val currImpressions = message.impressionsLeft!!
        DisplayManager.instance().removeMessage(HostAppInfoRepository.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(MessageMapper.mapFrom(message), resourceId, isOpt)
        val updatedMessage = CampaignRepository.instance().messages.values.first()

        result.shouldBeTrue()
        updatedMessage.impressionsLeft shouldBeEqualTo currImpressions - 1
        updatedMessage.isOptedOut shouldBeEqualTo isOpt

        readinessManager.clearMessages()
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "{0} testing",
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
                arrayOf("Tooltip Tip - content", R.id.message_tip, false, true),
                arrayOf("Tooltip Tip - content", -99, false, true),
            )
        }
    }
}

@SuppressWarnings(
    "LargeClass",
)
@RunWith(RobolectricTestRunner::class)
class MessageActionsCoroutineFuncSpec : BaseTest() {
    private val action = MessageActionsCoroutine()
    private val message = MessageMapper.mapFrom(TestDataHelper.createDummyMessage())

    @After
    override fun tearDown() {
        super.setup()
        InAppMessaging.setNotConfiguredInstance(true)
        InAppMessaging.instance().onPushPrimer = null
        InAppMessaging.instance().onVerifyContext = { _, _ -> true }
    }

    @Test
    fun `should return invalid impression type`() {
        action.getOnClickBehaviorType(-2) shouldBeEqualTo ImpressionType.INVALID
    }

    @Test
    fun `should return null for action one with empty buttons`() {
        val message = message.copy(buttons = listOf())
        action.getOnClickBehavior(ImpressionType.ACTION_ONE, message).shouldBeNull()
    }

    @Test
    fun `should return null for action two with invalid button size`() {
        val message = message.copy(buttons = listOf())
        action.getOnClickBehavior(ImpressionType.ACTION_TWO, message).shouldBeNull()
    }

    @Test
    fun `should return null for click with null content`() {
        val message = message.copy(content = null)
        action.getOnClickBehavior(ImpressionType.CLICK_CONTENT, message).shouldBeNull()
    }

    @Test
    fun `should return null for invalid impression`() {
        val message = message.copy(content = null)
        action.getOnClickBehavior(ImpressionType.INVALID, message).shouldBeNull()
    }

    @Test
    fun `should start activity for redirect`() {
        val activity = setupActivity()
        action.handleAction(OnClickBehavior(1, "https://test"))

        Mockito.verify(activity).startActivity(any())
    }

    @Test
    fun `should start activity for deeplink`() {
        val activity = setupActivity()
        val onClick = OnClickBehavior(2, "https://test")
        action.handleAction(onClick)
        Mockito.verify(activity).startActivity(any())

        InAppMessaging.instance().unregisterMessageDisplayActivity()
        action.handleAction(onClick)
        Mockito.verify(activity).startActivity(any())
    }

    @Test
    fun `should not start activity for exit`() {
        val activity = setupActivity()
        action.handleAction(OnClickBehavior(3, "https://test"))

        Mockito.verify(activity, never()).startActivity(any())
    }

    @Test
    fun `should not start activity for null url and activity`() {
        val activity = setupActivity()
        val onClick = OnClickBehavior(2, null)
        action.handleAction(onClick)
        Mockito.verify(activity, never()).startActivity(any())

        InAppMessaging.instance().unregisterMessageDisplayActivity()
        action.handleAction(onClick)
        Mockito.verify(activity, never()).startActivity(any())
    }

    @Test
    fun `should not start activity for empty url and null activity`() {
        val activity = setupActivity()
        val onClick = OnClickBehavior(2, "")
        action.handleAction(onClick)
        Mockito.verify(activity, never()).startActivity(any())

        InAppMessaging.instance().unregisterMessageDisplayActivity()
        action.handleAction(onClick)
        Mockito.verify(activity, never()).startActivity(any())
    }

    @Test
    fun `should not crash on activity not found`() {
        val activity = setupActivity()
        `when`(activity.startActivity(any())).thenThrow(ActivityNotFoundException())
        action.handleAction(OnClickBehavior(2, "https://test"))
    }

    @Test
    fun `should invoke callback for primer`() {
        setupActivity()
        val function: () -> Unit = {}
        val mockCallback = Mockito.mock(function.javaClass)

        InAppMessaging.instance().onPushPrimer = mockCallback
        action.handleAction(OnClickBehavior(4, ""), "test")

        PushPrimerTrackerManager.campaignId shouldBeEqualTo "test"
        Mockito.verify(mockCallback).invoke()

        PushPrimerTrackerManager.campaignId = ""
    }

    @Test
    fun `should not invoke callback for primer`() {
        verifyPushPrimer(false)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `should not invoke callback for primer in tiramisu`() {
        verifyPushPrimer(true)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `should request permission in tiramisu`() {
        val mockAct = setupActivity(true)
        InAppMessaging.instance().onPushPrimer = null
        action.handleAction(OnClickBehavior(4, ""), "test")

        PushPrimerTrackerManager.campaignId shouldBeEqualTo "test"
        Mockito.verify(mockAct).requestPermissions(any(), any())
    }

    @Test
    fun `should handle empty buttons`() {
        val message = message.copy(buttons = listOf())
        action.executeTask(message, R.id.message_single_button, false).shouldBeTrue()
    }

    @Test
    fun `should handle empty content`() {
        val message = message.copy(content = null)
        action.executeTask(message, R.id.slide_up, false).shouldBeTrue()
    }

    @SuppressWarnings("LongMethod")
    @Test
    fun `should handle invalid attribute type`() {
        val message = message.copy(
            buttons = listOf(
                MessageButton(
                    "", "", OnClickBehavior(4, ""),
                    "",
                    embeddedEvent = Trigger(
                        1, 4, "custom",
                        triggerAttributes = mutableListOf(
                            TriggerAttribute("attribute1", "attrValue1", 0, 0),
                        ),
                    ),
                ),
            ),
        )
        action.executeTask(message, R.id.message_single_button, false).shouldBeTrue()
    }

    private fun setupActivity(isTiramisu: Boolean = false): Activity {
        val activity = Mockito.mock(Activity::class.java)
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (isTiramisu) {
            val bundle = Bundle()
            bundle.putString("com.rakuten.tech.mobile.inappmessaging.subscriptionkey", "test")
            context.applicationInfo.metaData = bundle
        }
        InAppMessaging.initialize(context, true)
        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())

        return activity
    }

    private fun verifyPushPrimer(isTiramisu: Boolean) {
        setupActivity(true)
        val function: () -> Unit = {}
        val mockCallback = Mockito.mock(function.javaClass)
        InAppMessaging.instance().onPushPrimer = null
        action.handleAction(OnClickBehavior(4, ""), "test")

        PushPrimerTrackerManager.campaignId shouldBeEqualTo if (isTiramisu)"test" else ""
        Mockito.verify(mockCallback, never()).invoke()
    }
}

@RunWith(AndroidJUnit4::class)
class MessageActionsCoroutineTiramisuSpec {

    @Test
    fun `should request push permission`() {
        val activity = setupActivity()
        InAppMessaging.instance().onPushPrimer = null
        val mockChecker = Mockito.mock(BuildVersionChecker::class.java)
        `when`(mockChecker.isAndroidTAndAbove()).thenReturn(true)
        MessageActionsCoroutine().handlePushPrimer("test", mockChecker)

        Mockito.verify(activity).requestPermissions(any(), any())
        PushPrimerTrackerManager.campaignId shouldBeEqualTo "test"
        PushPrimerTrackerManager.campaignId = ""
    }

    @Test
    fun `should not request push permission for unregistered activity`() {
        val activity = setupActivity()
        InAppMessaging.instance().onPushPrimer = null
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        val mockChecker = Mockito.mock(BuildVersionChecker::class.java)
        `when`(mockChecker.isAndroidTAndAbove()).thenReturn(true)
        MessageActionsCoroutine().handlePushPrimer("test", mockChecker)

        Mockito.verify(activity, never()).requestPermissions(any(), any())

        PushPrimerTrackerManager.campaignId shouldBeEqualTo "test"
        PushPrimerTrackerManager.campaignId = ""
    }

    private fun setupActivity(): Activity {
        val activity = Mockito.mock(Activity::class.java)
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())

        return activity
    }
}
