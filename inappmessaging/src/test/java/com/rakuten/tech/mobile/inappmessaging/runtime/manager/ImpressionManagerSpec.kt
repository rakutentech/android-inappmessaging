package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.rakuten.tech.mobile.inappmessaging.runtime.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Impression
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.rat.RatImpression
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Test class for ImpressionManager.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ImpressionManagerSpec : BaseTest() {

    private val eventTracker = Mockito.mock(EventTrackerHelper::class.java)

    @Before
    override fun setup() {
        super.setup()
        impressionList = ImpressionManager.createImpressionList(VALID_IMPRESSION_TYPES)
    }

    @Test
    fun `should create impression list with correct attributes`() {
        impressionList!![0].timestamp shouldBeGreaterThan 0L
        impressionList!![0].type shouldBeEqualTo ImpressionType.ACTION_ONE.typeId
    }

    @Test
    fun `should return empty when create impression list with wrong arg`() {
        ImpressionManager.createImpressionList(IMPRESSION_TYPES).isEmpty()
        ImpressionManager.createImpressionList(INVALID_TYPES).isEmpty()
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun `should invoke start impression worker`() {
        setupEventBroadcaster()
        val status =
            WorkManager.getInstance(ApplicationProvider.getApplicationContext())
                .getWorkInfosByTag(IMPRESSION_WORKER_NAME)
        status.get().shouldHaveSize(1)
    }

    @Test
    @SuppressWarnings("LongMethod")
    fun `should invoke broadcaster`() {
        val captor = setupEventBroadcaster()

        val map = captor.firstValue
        map[InAppMessagingConstants.RAT_EVENT_CAMP_ID] shouldBeEqualTo "1234"
        (map[InAppMessagingConstants.RAT_EVENT_SUBS_ID] as String).shouldNotBeEmpty()
        (map[InAppMessagingConstants.RAT_EVENT_IMP] as List<RatImpression>) shouldHaveSize impressionList!!.size
    }

    @Test
    fun `should not invoke broadcaster if empty list`() {
        ImpressionManager.scheduleReportImpression(
            emptyList(),
            "1234",
            false,
            eventTracker::sendEvent
        )
        Mockito.verify(eventTracker, never()).sendEvent(
            ArgumentMatchers.anyString(), ArgumentMatchers.anyMap<String, Any>()
        )
    }

    @Test
    fun `should invoke broadcaster with valid impression content`() {
        ImpressionManager.impressionMap["1234"] = Impression(ImpressionType.IMPRESSION, Date().time)
        val captor = setupEventBroadcaster()

        val map = captor.firstValue
        map[InAppMessagingConstants.RAT_EVENT_CAMP_ID] shouldBeEqualTo "1234"
        (map[InAppMessagingConstants.RAT_EVENT_SUBS_ID] as String).shouldNotBeEmpty()
        (map[InAppMessagingConstants.RAT_EVENT_IMP] as List<RatImpression>) shouldHaveSize impressionList!!.size

        ImpressionManager.impressionMap.clear()
    }

    @Test
    fun `should invoke broadcaster for impression type`() {
        ImpressionManager.sendImpressionEvent(
            "1234", listOf(Impression(ImpressionType.IMPRESSION, Date().time)), eventTracker::sendEvent
        )

        val captor = argumentCaptor<Map<String, Any>>()
        Mockito.verify(eventTracker).sendEvent(
            eq(InAppMessagingConstants.RAT_EVENT_KEY_IMPRESSION), captor.capture()
        )

        val map = captor.firstValue
        map[InAppMessagingConstants.RAT_EVENT_CAMP_ID] shouldBeEqualTo "1234"
        (map[InAppMessagingConstants.RAT_EVENT_SUBS_ID] as String).shouldNotBeEmpty()
        (map[InAppMessagingConstants.RAT_EVENT_IMP] as List<RatImpression>) shouldHaveSize 1
    }

    @Test
    fun `should not invoke broadcaster for empty list`() {
        ImpressionManager.sendImpressionEvent("1234", listOf(), eventTracker::sendEvent)

        val captor = argumentCaptor<Map<String, Any>>()
        Mockito.verify(eventTracker, never()).sendEvent(eq(InAppMessagingConstants.RAT_EVENT_KEY_IMPRESSION), any())
    }

    @SuppressWarnings("LongMethod")
    private fun setupEventBroadcaster(): KArgumentCaptor<Map<String, Any>> {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id"
        )
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        ImpressionManager.scheduleReportImpression(
            impressionList!!,
            "1234",
            false,
            eventTracker::sendEvent
        )
        val captor = argumentCaptor<Map<String, Any>>()
        Mockito.verify(eventTracker).sendEvent(
            eq(InAppMessagingConstants.RAT_EVENT_KEY_IMPRESSION), captor.capture()
        )
        return captor
    }

    companion object {
        private const val IMPRESSION_WORKER_NAME = "iam_impression_work"
        private val VALID_IMPRESSION_TYPES: MutableList<ImpressionType> =
            mutableListOf(ImpressionType.ACTION_ONE, ImpressionType.OPT_OUT)
        private val IMPRESSION_TYPES: MutableList<ImpressionType> =
            mutableListOf(ImpressionType.ACTION_ONE, ImpressionType.IMPRESSION)
        private val INVALID_TYPES: MutableList<ImpressionType> =
            mutableListOf(ImpressionType.ACTION_ONE, ImpressionType.INVALID)
        private var impressionList: List<Impression>? = null
    }
}
