package com.rakuten.tech.mobile.inappmessaging.runtime.coroutine

import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalDisplayedMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ReadyForDisplayMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.CampaignData
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.DisplayManager
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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

        private const val CAMPAIGN_DATA = """
                {
                    "campaignId":"1234567890",
                    "isTest":false,
                    "maxImpressions":190,
                    "messagePayload":{
                        "backgroundColor":"#000000",
                        "frameColor":"#ffffff",
                        "header":"DEV-Test (Android In-App-Test) - Login",
                        "headerColor":"#ffffff",
                        "messageBody":"Login Test",
                        "messageBodyColor":"#ffffff",
                        "messageSettings":{
                            "controlSettings":{
                                "buttons":[{
                                    "buttonBackgroundColor":"#000000",
                                    "buttonTextColor":"#ffffff",
                                    "buttonText":"Test",
                                    "buttonBehavior":{
                                        "action":1,
                                        "uri":"https://en.wikipedia.org/wiki/Test"
                                    },
                                    "campaignTrigger":{
                                        "type":1,
                                        "eventType":4,
                                        "eventName":"custom",
                                        "attributes":[{
                                            "name":"attribute1",
                                            "value":"attrValue",
                                            "type":1,
                                            "operator":1
                                        },{
                                            "name":"attribute2",
                                            "value":"1",
                                            "type":2,
                                            "operator":1
                                        },{
                                            "name":"attribute3",
                                            "value":"1.0",
                                            "type":3,
                                            "operator":1
                                        },{
                                            "name":"attribute4",
                                            "value":"true",
                                            "type":4,
                                            "operator":1
                                        },{
                                            "name":"attribute5",
                                            "value":"1234567",
                                            "type":5,
                                            "operator":1
                                        }]
                                    }
                                },{
                                    "buttonBackgroundColor":"#000000",
                                    "buttonTextColor":"#ffffff",
                                    "buttonText":"Test",
                                    "buttonBehavior":{
                                        "action":1,
                                        "uri":"https://en.wikipedia.org/wiki/Test"
                                    },
                                    "campaignTrigger":{
                                        "type":1,
                                        "eventType":4,
                                        "eventName":"custom",
                                        "attributes":[{
                                            "name":"attribute",
                                            "value":"attrValue",
                                            "type":1,
                                            "operator":1
                                        }]
                                    }
                                }],
                                "content":{
                                    "onClickBehavior":{
                                        "action":1,
                                        "uri":"https://en.wikipedia.org/wiki/Test"
                                    },
                                    "campaignTrigger":{
                                        "type":1,
                                        "eventType":1,
                                        "eventName":"event",
                                        "attributes":[{
                                            "name":"attribute",
                                            "value":"attrValue",
                                            "type":1,
                                            "operator":1
                                        }]
                                    }
                                }
                            },
                            "displaySettings":{
                                "endTimeMillis":1584109800000,
                                "optOut":false,
                                "orientation":1,
                                "slideFrom":1,
                                "textAlign":2
                            }
                        },
                        "resource":{
                            "cropType":2,
                            "imageUrl":"https://sample.image.url/test.jpg"
                        },
                        "title":"DEV-Test (Android In-App-Test)",
                        "titleColor":"#000000"
                    },
                    "triggers":[{
                        "eventName":"Launch the App Event",
                        "eventType":1,
                        "attributes":[{
                            "name":"attribute",
                            "value":"attrValue",
                            "type":1,
                            "operator":1
                        }],
                        "type":1
                    },{
                        "eventName":"Login Event",
                        "eventType":2,
                        "attributes":[],
                        "type":1
                    }],
                    "type":2
                }"""
    }

    private val message = Gson().fromJson(CAMPAIGN_DATA.trimIndent(), CampaignData::class.java)
    private val activity = Mockito.mock(Activity::class.java)

    @Before
    fun setup() {
        When calling activity.packageManager itReturns ApplicationProvider
                .getApplicationContext<Context>().packageManager

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
        val result = MessageActionsCoroutine().executeTask(null, resourceId, isOpt)
        result.shouldBeFalse()
    }

    @Test
    fun `should add message to display repo`() {
        val numberOfTimesDisplayed: Int = LocalDisplayedMessageRepository.instance().numberOfTimesDisplayed(message)
        DisplayManager.instance().removeMessage(InAppMessaging.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(message, resourceId, isOpt)
        result.shouldBeTrue()
        LocalDisplayedMessageRepository.instance()
                .numberOfTimesDisplayed(message) shouldBeEqualTo numberOfTimesDisplayed + 1
    }

    @Test
    fun `should remove message from ready repo`() {
        val messageList = ArrayList<Message>()
        messageList.add(message)
        ReadyForDisplayMessageRepository.instance().replaceAllMessages(messageList)
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(1)

        DisplayManager.instance().removeMessage(InAppMessaging.instance().getRegisteredActivity())
        val result = MessageActionsCoroutine().executeTask(message, resourceId, isOpt)
        if (result) {
            DisplayManager.instance().displayMessage()
        }
        ReadyForDisplayMessageRepository.instance().getAllMessagesCopy().shouldHaveSize(0)
    }
}
