package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents

import android.os.Build
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

/**
 * Test class for AppStartEvent.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class EventSpec(
    val eventName: String,
    val event: BaseEvent,
    val expectedType: EventType,
    val expectedName: String,
    val expectedPersistType: Boolean
) : BaseTest() {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0} event type")
        fun data(): Collection<Array<Any>> {
            return listOf(
                    arrayOf("AppStart", AppStartEvent(), EventType.APP_START,
                            EventType.APP_START.name.toLowerCase(Locale.getDefault()), true),
                    arrayOf("Custom", CustomEvent("custom"), EventType.CUSTOM, "custom", false),
                    arrayOf("Custom UpperCase", CustomEvent("CuStOm"), EventType.CUSTOM, "custom", false),
                    arrayOf("LoginSuccessful",
                            LoginSuccessfulEvent(), EventType.LOGIN_SUCCESSFUL,
                            EventType.LOGIN_SUCCESSFUL.name.toLowerCase(Locale.getDefault()),
                            false),
                    arrayOf("PurchaseSuccessful",
                            PurchaseSuccessfulEvent(),
                            EventType.PURCHASE_SUCCESSFUL,
                            EventType.PURCHASE_SUCCESSFUL.name.toLowerCase(Locale.getDefault()),
                            false)
            )
        }
    }

    @Test
    fun `should have correct event type`() {
        event.getEventType() shouldEqual expectedType.typeId
    }

    @Test
    fun `should have correct event name`() {
        event.getEventName() shouldEqual expectedName
    }

    @Test
    fun `should have valid timestamp`() {
        event.getTimestamp() shouldBeGreaterThan 0L
    }

    @Test
    fun `RAT event map should contain correct keys`() {
        val map = event.getRatEventMap()
        map shouldHaveKey InAppMessagingConstants.RAT_EVENT_KEY_EVENT_NAME
        map shouldHaveKey InAppMessagingConstants.RAT_EVENT_KEY_EVENT_TIMESTAMP
    }

    @Test
    fun `RAT event map should correct values`() {
        val map = event.getRatEventMap()
        map[InAppMessagingConstants.RAT_EVENT_KEY_EVENT_NAME] shouldEqual expectedName
        map[InAppMessagingConstants.RAT_EVENT_KEY_EVENT_TIMESTAMP] as Long shouldBeGreaterThan 0L
    }

    @Test
    fun `attribute map should be empty`() {
        if (event is PurchaseSuccessfulEvent) {
            event.getAttributeMap().shouldHaveSize(4)
        } else {
            event.getAttributeMap().shouldBeEmpty()
        }
    }

    @Test
    fun `should not be persistent type`() {
        event.isPersistentType() shouldEqual expectedPersistType
    }
}
