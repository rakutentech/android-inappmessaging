package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.LegacyEventTrackerHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Impression
import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.ExecutionException

/**
 * Test class for ImpressionManager.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ImpressionManagerSpec : BaseTest() {

    private val eventBroadcaster = Mockito.mock(LegacyEventTrackerHelper::class.java)

    @Before
    override fun setup() {
        super.setup()
        impressionList = ImpressionManager().createImpressionList(VALID_IMPRESSION_TYPES)
    }

    @Test
    fun `should create impression list with correct attributes`() {
        impressionList!![0].timestamp shouldBeGreaterThan 0L
        impressionList!![1].type shouldBeEqualTo ImpressionType.ACTION_ONE.typeId
    }

    @Test
    fun `should throw exception when create impression list with wrong arg`() {
        ImpressionManager().createImpressionList(INVALID_IMPRESSION_TYPES).isEmpty()
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun `should invoke start impression worker`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        ImpressionManager().scheduleReportImpression(impressionList!!, "1234", false,
                eventBroadcaster::sendEvent)
        val status =
                WorkManager.getInstance(ApplicationProvider.getApplicationContext<Context>())
                        .getWorkInfosByTag(IMPRESSION_WORKER_NAME)
        status.get().shouldHaveSize(1)
    }

    @Test
    fun `should invoke broadcaster`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        ImpressionManager().scheduleReportImpression(
                impressionList!!,
                "1234",
                false,
                eventBroadcaster::sendEvent)
        Mockito.verify(eventBroadcaster).sendEvent(ArgumentMatchers.anyString(), ArgumentMatchers.anyMap<String, Any>())
    }

    companion object {
        private const val IMPRESSION_WORKER_NAME = "iam_impression_work"
        private val VALID_IMPRESSION_TYPES: MutableList<ImpressionType> =
                mutableListOf(ImpressionType.ACTION_ONE, ImpressionType.OPT_OUT)
        private val INVALID_IMPRESSION_TYPES: MutableList<ImpressionType> =
                mutableListOf(ImpressionType.ACTION_ONE, ImpressionType.IMPRESSION)
        private var impressionList: List<Impression>? = null
    }
}
