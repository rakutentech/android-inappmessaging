package com.rakuten.tech.mobile.inappmessaging.runtime.runnable

import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.facebook.soloader.SoLoader
import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.CampaignData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for DisplayMessageRunnable.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@SuppressWarnings("LargeClass")
class DisplayMessageRunnableSpec : BaseTest() {
    private val message = Mockito.mock(CampaignData::class.java)
    private val hostAppActivity = Mockito.mock(Activity::class.java)
    private val view = Mockito.mock(View::class.java)
    private val window = Mockito.mock(Window::class.java)

    @Before
    override fun setup() {
        super.setup()
        SoLoader.setInTestMode()
        When calling view!!.id itReturns 12343254
        When calling hostAppActivity.window itReturns window
    }

    @Test
    fun `should not throw exception fo invalid message type`() {
        When calling message.getType() itReturns 0
        DisplayMessageRunnable(message, hostAppActivity, IMAGE_ASPECT_RATIO).run()
    }

    @Test
    fun `should not throw exception fo invalid does not exist`() {
        When calling message.getType() itReturns 100
        DisplayMessageRunnable(message, hostAppActivity, IMAGE_ASPECT_RATIO).run()
    }

    @Test(expected = NullPointerException::class)
    fun `should throw null pointer exception when modal`() {
        When calling message.getType() itReturns InAppMessageType.MODAL.typeId
        DisplayMessageRunnable(message, hostAppActivity, IMAGE_ASPECT_RATIO).run()
    }

    @Test(expected = NullPointerException::class)
    fun `should throw null pointer exception when fullscreen`() {
        When calling message.getType() itReturns InAppMessageType.FULL.typeId
        DisplayMessageRunnable(message, hostAppActivity, IMAGE_ASPECT_RATIO).run()
    }

    @Test
    fun `should throw exception with mock activity for full`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
        When calling message.getType() itReturns InAppMessageType.FULL.typeId
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD.trimIndent(),
                MessagePayload::class.java)
        When calling hostAppActivity
                .layoutInflater itReturns LayoutInflater.from(ApplicationProvider.getApplicationContext())
        DisplayMessageRunnable(message, hostAppActivity, IMAGE_ASPECT_RATIO).run()
    }

    @Test
    fun `should not throw exception with mock activity for modal`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
        When calling message.getType() itReturns InAppMessageType.MODAL.typeId
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD.trimIndent(),
                MessagePayload::class.java)
        When calling hostAppActivity
                .layoutInflater itReturns LayoutInflater.from(ApplicationProvider.getApplicationContext())
        DisplayMessageRunnable(message, hostAppActivity, IMAGE_ASPECT_RATIO).run()
    }

    @Test
    fun `should not throw exception with mock activity for slide`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
        When calling message.getType() itReturns InAppMessageType.SLIDE.typeId
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_SLIDE.trimIndent(),
                MessagePayload::class.java)
        When calling hostAppActivity
                .layoutInflater itReturns LayoutInflater.from(ApplicationProvider.getApplicationContext())
        DisplayMessageRunnable(message, hostAppActivity, IMAGE_ASPECT_RATIO).run()
    }

    @Test
    fun `should not throw exception with mock activity for modal and buttons`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
        When calling message.getType() itReturns InAppMessageType.MODAL.typeId
        When calling message.getMessagePayload() itReturns Gson().fromJson(MESSAGE_PAYLOAD_TWO_BUTTONS.trimIndent(),
                MessagePayload::class.java)
        When calling hostAppActivity
                .layoutInflater itReturns LayoutInflater.from(ApplicationProvider.getApplicationContext())
        DisplayMessageRunnable(message, hostAppActivity, IMAGE_ASPECT_RATIO).run()
    }

    companion object {
        private const val IMAGE_ASPECT_RATIO = 0.75f
        private const val MESSAGE_PAYLOAD = """
            {
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
                                "eventType":1,
                                "eventName":"event",
                                "attributes":[{
                                    "name":"attribute",
                                    "value":"attrValue",
                                    "type":1,
                                    "operator":1
                                }]
                            }
                        }]
                    },
                    "displaySettings":{
                        "endTimeMillis":1584109800000,
                        "optOut":true,
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
            }
        """
        private const val MESSAGE_PAYLOAD_SLIDE = """
            {
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
                                "eventType":1,
                                "eventName":"event",
                                "attributes":[{
                                    "name":"attribute",
                                    "value":"attrValue",
                                    "type":1,
                                    "operator":1
                                }]
                            }
                        }]
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
            }
        """
        private const val MESSAGE_PAYLOAD_TWO_BUTTONS = """
            {
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
                                "eventType":1,
                                "eventName":"event",
                                "attributes":[{
                                    "name":"attribute",
                                    "value":"attrValue",
                                    "type":1,
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
                                "eventType":1,
                                "eventName":"event",
                                "attributes":[{
                                    "name":"attribute",
                                    "value":"attrValue",
                                    "type":1,
                                    "operator":1
                                }]
                            }
                        }]
                    },
                    "displaySettings":{
                        "endTimeMillis":1584109800000,
                        "optOut":true,
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
            }
        """
    }
}
