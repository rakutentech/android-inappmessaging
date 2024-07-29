package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.AnalyticsKey
import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import java.util.Locale

/**
 * Test class for AppStartEvent.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class EventSpec(
    val eventName: String,
    val event: BaseEvent,
    private val expectedType: EventType,
    private val expectedName: String,
    private val expectedPersistType: Boolean,
) : BaseTest() {

    @Test
    fun `should have correct event type`() {
        event.getEventType() shouldBeEqualTo expectedType.typeId
    }

    @Test
    fun `should have correct event name`() {
        event.getEventName() shouldBeEqualTo expectedName
    }

    @Test
    fun `should have valid timestamp`() {
        event.getTimestamp() shouldBeGreaterThan 0L
    }

    @Test
    fun `RAT event map should contain correct keys`() {
        val map = event.getRatEventMap()
        map shouldHaveKey AnalyticsKey.EVENT_NAME.key
        map shouldHaveKey AnalyticsKey.TIMESTAMP.key
    }

    @Test
    fun `RAT event map should correct values`() {
        val map = event.getRatEventMap()
        map[AnalyticsKey.EVENT_NAME.key] shouldBeEqualTo expectedName
        map[AnalyticsKey.TIMESTAMP.key] as Long shouldBeGreaterThan 0L
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
        event.isPersistentType() shouldBeEqualTo expectedPersistType
    }

    companion object {
        @SuppressWarnings("LongMethod")
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0} event type")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    "AppStart", AppStartEvent(), EventType.APP_START,
                    EventType.APP_START.name.lowercase(Locale.getDefault()), true,
                ),
                arrayOf("Custom", CustomEvent("custom"), EventType.CUSTOM, "custom", false),
                arrayOf("Custom UpperCase", CustomEvent("CuStOm"), EventType.CUSTOM, "custom", false),
                arrayOf(
                    "LoginSuccessful",
                    LoginSuccessfulEvent(), EventType.LOGIN_SUCCESSFUL,
                    EventType.LOGIN_SUCCESSFUL.name.lowercase(Locale.getDefault()),
                    false,
                ),
                arrayOf(
                    "PurchaseSuccessful",
                    PurchaseSuccessfulEvent(),
                    EventType.PURCHASE_SUCCESSFUL,
                    EventType.PURCHASE_SUCCESSFUL.name.lowercase(Locale.getDefault()),
                    false,
                ),
            )
        }
    }
}
