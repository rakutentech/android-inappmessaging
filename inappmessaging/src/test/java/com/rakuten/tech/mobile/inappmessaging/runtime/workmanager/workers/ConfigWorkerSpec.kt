package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InApp
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessagingTestConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalEventRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.ConfigScheduler
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

/**
 * Test class for ConfigWorker.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@Ignore("base class")
open class ConfigWorkerSpec : BaseTest() {

    @Mock
    internal val mockResponse: Response<ConfigResponse?>? = null
    internal val context = Mockito.mock(Context::class.java)
    internal val workerParameters = Mockito.mock(WorkerParameters::class.java)
    internal val mockHostRepository = Mockito.mock(HostAppInfoRepository::class.java)
    internal val mockConfigRepository = Mockito.mock(ConfigResponseRepository::class.java)
    internal val mockMessageScheduler = Mockito.mock(MessageMixerPingScheduler::class.java)
    internal val mockRetry = Mockito.mock(RetryDelayUtil::class.java)
    internal val mockConfigScheduler = Mockito.mock(ConfigScheduler::class.java)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        ConfigScheduler.currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
        LocalEventRepository.instance().clearEvents()
        ConfigResponseRepository.resetInstance()
    }

    internal fun initializeInstance() {
        val testAppInfo = HostAppInfo("rakuten.com.tech.mobile.test",
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                "test-key", InAppMessagingTestConstants.LOCALE)
        HostAppInfoRepository.instance().addHostInfo(testAppInfo)
        val context: Context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID, "test_device_id")

        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
    }

    internal fun setupMock(rollout: Int) {
        val mockConfig = Mockito.mock(ConfigResponse::class.java)
        val mockData = Mockito.mock(ConfigResponseData::class.java)
        When calling mockResponse?.isSuccessful itReturns true
        When calling mockResponse?.body() itReturns mockConfig
        When calling mockConfig.data itReturns mockData
        When calling mockData.rollOutPercentage itReturns rollout
    }

    companion object {
        internal const val CONFIG_KEY = "com.rakuten.tech.mobile.inappmessaging.configurl"
        internal const val SUBSCRIPTION_KEY = "com.rakuten.tech.mobile.inappmessaging.subscriptionkey"
    }
}

class ConfigWorkerSuccessSpec : ConfigWorkerSpec() {
    @Test
    fun `should return success with valid values`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val app = ctx.packageManager.getApplicationInfo(ctx.packageName,
                PackageManager.GET_META_DATA)
        val bundle = app.metaData
        When calling mockHostRepository.getPackageName() itReturns ctx.packageName
        val version = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
        When calling mockHostRepository.getVersion() itReturns version
        When calling mockHostRepository.getConfigUrl() itReturns bundle.getString(CONFIG_KEY, "")
        When calling mockHostRepository
                .getInAppMessagingSubscriptionKey() itReturns bundle.getString(SUBSCRIPTION_KEY, "")
        val worker = ConfigWorker(context, workerParameters, mockHostRepository, mockConfigRepository,
                mockMessageScheduler)
        val expected = if (bundle.getString(CONFIG_KEY, "").isNullOrEmpty())
            ListenableWorker.Result.retry() else ListenableWorker.Result.success()
        worker.doWork() shouldBeEqualTo expected
    }

//    @Test
    fun `should return success with valid values not mock`() {
        initializeInstance()

        val worker = ConfigWorker(context, workerParameters)
        val expected = if (HostAppInfoRepository.instance().getConfigUrl().isNullOrEmpty())
            ListenableWorker.Result.retry() else ListenableWorker.Result.success()
        worker.doWork() shouldBeEqualTo expected
    }

    @Test
    fun `should return success but will trigger next config when too many request error`() {
        When calling mockResponse?.isSuccessful itReturns false
        When calling mockResponse?.code() itReturns RetryDelayUtil.RETRY_ERROR_CODE
        ConfigWorker(context, workerParameters, HostAppInfoRepository.instance(), ConfigResponseRepository.instance(),
                MessageMixerPingScheduler.instance(), mockConfigScheduler, mockRetry)
                .onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()

        Mockito.verify(mockConfigScheduler).startConfig(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY))
        Mockito.verify(mockRetry).getNextDelay(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY))
    }

    @Test
    fun `should use correct backoff delay`() {
        When calling mockResponse?.isSuccessful itReturns false
        When calling mockResponse?.code() itReturns RetryDelayUtil.RETRY_ERROR_CODE
        val worker = ConfigWorker(context, workerParameters, HostAppInfoRepository.instance(),
                ConfigResponseRepository.instance(), MessageMixerPingScheduler.instance(), mockConfigScheduler)
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockConfigScheduler).startConfig(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY))
        ConfigScheduler.currDelay shouldBeGreaterThan (RetryDelayUtil.INITIAL_BACKOFF_DELAY * 2)

        worker.onResponse(mockResponse) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockConfigScheduler).startConfig(AdditionalMatchers.gt(RetryDelayUtil.INITIAL_BACKOFF_DELAY * 2))
        ConfigScheduler.currDelay shouldBeGreaterThan (RetryDelayUtil.INITIAL_BACKOFF_DELAY * 4)
    }

    @Test
    fun `should reset initial delay`() {
        When calling mockResponse?.isSuccessful itReturns false
        When calling mockResponse?.code() itReturns RetryDelayUtil.RETRY_ERROR_CODE
        val worker = ConfigWorker(context, workerParameters, mockHostRepository,
                mockConfigRepository, mockMessageScheduler, mockConfigScheduler)
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockConfigScheduler).startConfig(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY))

        When calling mockResponse.isSuccessful itReturns true
        When calling mockResponse.code() itReturns 200
        When calling mockResponse.body() itReturns Mockito.mock(ConfigResponse::class.java)
        worker.onResponse(mockResponse) shouldBeEqualTo ListenableWorker.Result.Success()
        ConfigScheduler.currDelay shouldBeEqualTo RetryDelayUtil.INITIAL_BACKOFF_DELAY
    }

    @Test
    fun `should log events when config is enabled`() {
        setupMock(100)
        initializeInstance()
        InAppMessaging.instance().logEvent(AppStartEvent())
        InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent())
        (InAppMessaging.instance() as InApp).tempEventList.shouldHaveSize(2)
        LocalEventRepository.instance().getEvents().shouldBeEmpty()

        val worker = ConfigWorker(context, workerParameters, HostAppInfoRepository.instance(),
                ConfigResponseRepository.instance(), mockMessageScheduler, mockConfigScheduler)
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()
        (InAppMessaging.instance() as InApp).tempEventList.shouldBeEmpty()
        LocalEventRepository.instance().getEvents().shouldHaveSize(2)
        Mockito.verify(mockMessageScheduler).pingMessageMixerService(0)
    }

    @Test
    fun `should clear temp events when config is disabled`() {
        setupMock(0)

        initializeInstance()
        InAppMessaging.instance().logEvent(AppStartEvent())
        InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent())
        (InAppMessaging.instance() as InApp).tempEventList.shouldHaveSize(2)
        LocalEventRepository.instance().getEvents().shouldBeEmpty()

        val worker = ConfigWorker(context, workerParameters, HostAppInfoRepository.instance(),
                ConfigResponseRepository.instance(), mockMessageScheduler, mockConfigScheduler)
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()
        InAppMessaging.instance() shouldBeInstanceOf InAppMessaging.NotInitializedInAppMessaging::class.java
        LocalEventRepository.instance().getEvents().shouldBeEmpty()
        Mockito.verify(mockMessageScheduler, never()).pingMessageMixerService(0)
    }
}

class ConfigWorkerFailSpec : ConfigWorkerSpec() {
    @Test
    fun `should retry if server fail`() {
        initializeInstance()
        InAppMessaging.instance() shouldBeInstanceOf InApp::class.java
        When calling mockResponse?.isSuccessful itReturns false
        When calling mockResponse?.code() itReturns 500
        When calling mockResponse?.body() itReturns null
        ConfigWorker(context,
                workerParameters).onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.retry()

        // should not reset instance
        InAppMessaging.instance() shouldBeInstanceOf InApp::class.java
    }

    @Test
    fun `should fail if request fail`() {
        initializeInstance()
        InAppMessaging.instance() shouldBeInstanceOf InApp::class.java
        When calling mockResponse?.isSuccessful itReturns false
        When calling mockResponse?.code() itReturns 400
        val worker = ConfigWorker(context, workerParameters)
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.failure()

        // should reset instance
        InAppMessaging.instance() shouldBeInstanceOf InAppMessaging.NotInitializedInAppMessaging::class.java
    }

    @Test
    fun `should fail if hostapp id is empty`() {
        When calling mockHostRepository.getPackageName() itReturns ""
        val worker = ConfigWorker(context, workerParameters, mockHostRepository, ConfigResponseRepository.instance(),
                MessageMixerPingScheduler.instance())
        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should fail if hostapp version is empty`() {
        When calling mockHostRepository.getPackageName() itReturns "valid.package.name"
        When calling mockHostRepository.getVersion() itReturns ""
        val worker = ConfigWorker(context, workerParameters, mockHostRepository, ConfigResponseRepository.instance(),
                MessageMixerPingScheduler.instance())
        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should fail if subscription key is empty`() {
        When calling mockHostRepository.getPackageName() itReturns "valid.package.name"
        When calling mockHostRepository.getVersion() itReturns "valid.version"
        When calling mockHostRepository.getInAppMessagingSubscriptionKey() itReturns ""
        val worker = ConfigWorker(context, workerParameters, mockHostRepository, ConfigResponseRepository.instance(),
                MessageMixerPingScheduler.instance())
        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }
}
