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
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessagingTestConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.ConfigScheduler
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import org.amshove.kluent.*
import org.junit.Before
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
class ConfigWorkerSpec : BaseTest() {

    @Mock
    private val mockResponse: Response<ConfigResponse?>? = null
    private val context = Mockito.mock(Context::class.java)
    private val workerParameters = Mockito.mock(WorkerParameters::class.java)
    private val mockHostRespository = Mockito.mock(HostAppInfoRepository::class.java)
    private val mockConfigRespository = Mockito.mock(ConfigResponseRepository::class.java)
    private val mockMessageScheduler = Mockito.mock(MessageMixerPingScheduler::class.java)
    private val mockRetry = Mockito.mock(RetryDelayUtil::class.java)
    private val mockConfigScheduler = Mockito.mock(ConfigScheduler::class.java)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        ConfigScheduler.currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
    }

    @Test
    fun `should retry if server fail`() {
        When calling mockResponse?.isSuccessful itReturns false
        When calling mockResponse?.code() itReturns 500
        When calling mockResponse?.body() itReturns null
        ConfigWorker(context,
                workerParameters).onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.retry()
    }

    @Test
    fun `should fail if request fail`() {
        When calling mockResponse?.isSuccessful itReturns false
        When calling mockResponse?.code() itReturns 400
        val worker = ConfigWorker(context, workerParameters)
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should fail if hostapp id is null`() {
        When calling mockHostRespository.getPackageName() itReturns null
        val worker = ConfigWorker(context, workerParameters, mockHostRespository, ConfigResponseRepository.instance(),
                MessageMixerPingScheduler.instance())
        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should fail if hostapp id is empty`() {
        When calling mockHostRespository.getPackageName() itReturns ""
        val worker = ConfigWorker(context, workerParameters, mockHostRespository, ConfigResponseRepository.instance(),
                MessageMixerPingScheduler.instance())
        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should fail if hostapp version is null`() {
        When calling mockHostRespository.getPackageName() itReturns "valid.package.name"
        val worker = ConfigWorker(context, workerParameters, mockHostRespository, ConfigResponseRepository.instance(),
                MessageMixerPingScheduler.instance())
        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should fail if hostapp version is empty`() {
        When calling mockHostRespository.getPackageName() itReturns "valid.package.name"
        When calling mockHostRespository.getVersion() itReturns ""
        val worker = ConfigWorker(context, workerParameters, mockHostRespository, ConfigResponseRepository.instance(),
                MessageMixerPingScheduler.instance())
        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return success with valid values`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val app = ctx.packageManager.getApplicationInfo(ctx.packageName,
                PackageManager.GET_META_DATA)
        val bundle = app.metaData
        When calling mockHostRespository.getPackageName() itReturns ctx.packageName
        val version = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
        When calling mockHostRespository.getVersion() itReturns version
        When calling mockHostRespository.getConfigUrl() itReturns bundle.getString(CONFIG_KEY, "")
        When calling mockHostRespository
                .getInAppMessagingSubscriptionKey() itReturns bundle.getString(SUBSCRIPTION_KEY, "")
        val worker = ConfigWorker(context, workerParameters, mockHostRespository, mockConfigRespository,
                mockMessageScheduler)
        val expected = if (HostAppInfoRepository.instance().getConfigUrl().isNullOrEmpty())
            ListenableWorker.Result.retry() else ListenableWorker.Result.success()
        worker.doWork() shouldBeEqualTo expected
    }

    @Test
    fun `should return success with valid values not mock`() {
        val testAppInfo = HostAppInfo("rakuten.com.tech.mobile.test",
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                "test-key", InAppMessagingTestConstants.LOCALE)
        HostAppInfoRepository.instance().addHostInfo(testAppInfo)
        val context: Context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        Settings.Secure.putString(ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID, "test_device_id")
        val bundle = context.packageManager.getApplicationInfo(context.packageName,
                PackageManager.GET_META_DATA).metaData

        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test",
                bundle.getString(CONFIG_KEY, ""), isDebugLogging = false, isForTesting = true)
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
        val worker = ConfigWorker(context, workerParameters, mockHostRespository,
                mockConfigRespository, mockMessageScheduler, mockConfigScheduler)
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockConfigScheduler).startConfig(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY))

        When calling mockResponse.isSuccessful itReturns true
        When calling mockResponse.code() itReturns 200
        When calling mockResponse.body() itReturns Mockito.mock(ConfigResponse::class.java)
        worker.onResponse(mockResponse) shouldBeEqualTo ListenableWorker.Result.Success()
        ConfigScheduler.currDelay shouldBeEqualTo RetryDelayUtil.INITIAL_BACKOFF_DELAY
    }

    companion object {
        private const val CONFIG_KEY = "com.rakuten.tech.mobile.inappmessaging.configurl"
        private const val SUBSCRIPTION_KEY = "com.rakuten.tech.mobile.inappmessaging.subscriptionkey"
    }
}
