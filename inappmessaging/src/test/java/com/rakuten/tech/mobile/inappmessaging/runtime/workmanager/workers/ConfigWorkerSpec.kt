package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InApp
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessagingTestConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RetryDelayUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.ConfigScheduler
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import okhttp3.ResponseBody
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response
import java.net.HttpURLConnection

/**
 * Test class for ConfigWorker.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@Ignore("base class")
open class ConfigWorkerSpec : BaseTest() {

    @Mock
    internal val mockResponse: Response<ConfigResponse?>? = null
    internal val ctx = Mockito.mock(Context::class.java)
    internal val workParam = Mockito.mock(WorkerParameters::class.java)
    internal val mockHostRepo = Mockito.mock(HostAppInfoRepository::class.java)
    internal val mockConfigRepo = Mockito.mock(ConfigResponseRepository::class.java)
    internal val mockMsgSched = Mockito.mock(MessageMixerPingScheduler::class.java)
    internal val mockRetry = Mockito.mock(RetryDelayUtil::class.java)
    internal val mockConfigSched = Mockito.mock(ConfigScheduler::class.java)

    @Before
    override fun setup() {
        super.setup()
        MockitoAnnotations.initMocks(this)
        ConfigScheduler.currDelay = RetryDelayUtil.INITIAL_BACKOFF_DELAY
        ConfigResponseRepository.resetInstance()
        ConfigWorker.serverErrorCounter.set(0)
    }

    internal fun initializeInstance() {
        val testAppInfo = HostAppInfo(
            "rakuten.com.tech.mobile.test",
            InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
            "test-key", InAppMessagingTestConstants.LOCALE
        )
        HostAppInfoRepository.instance().addHostInfo(testAppInfo)
        val context: Context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID, "test_device_id"
        )

        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), true)
    }

    internal fun setupMock(rollout: Int) {
        val mockConfig = Mockito.mock(ConfigResponse::class.java)
        val mockData = Mockito.mock(ConfigResponseData::class.java)
        `when`(mockResponse?.isSuccessful).thenReturn(true)
        `when`(mockResponse?.isSuccessful).thenReturn(true)
        `when`(mockResponse?.body()).thenReturn(mockConfig)
        `when`(mockConfig.data).thenReturn(mockData)
        `when`(mockData.rollOutPercentage).thenReturn(rollout)
    }

    internal fun setupErrorBody() {
        val errorBody = Mockito.mock(ResponseBody::class.java)
        `when`(mockResponse?.errorBody()).thenReturn(errorBody)
        `when`(errorBody.string()).thenReturn("error")
    }

    companion object {
        internal const val CONFIG_KEY = "com.rakuten.tech.mobile.inappmessaging.configurl"
        internal const val SUB_KEY = "com.rakuten.tech.mobile.inappmessaging.subscriptionkey"
    }
}

class ConfigWorkerSuccessSpec : ConfigWorkerSpec() {
    @Test
    fun `should return success with valid values`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val app = ctx.packageManager.getApplicationInfo(ctx.packageName, PackageManager.GET_META_DATA)
        val bundle = app.metaData
        `when`(mockHostRepo.getPackageName()).thenReturn(ctx.packageName)
        val version = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
        `when`(mockHostRepo.getVersion()).thenReturn(version)
        `when`(mockHostRepo.getConfigUrl()).thenReturn(bundle.getString(CONFIG_KEY, ""))
        `when`(mockHostRepo.getSubscriptionKey()).thenReturn(bundle.getString(SUB_KEY, ""))
        val worker = ConfigWorker(this.ctx, workParam, mockHostRepo, mockConfigRepo, mockMsgSched)
        val expected = if (bundle.getString(CONFIG_KEY, "").isNullOrEmpty()) {
            ListenableWorker.Result.retry()
        } else ListenableWorker.Result.success()
        worker.doWork() shouldBeEqualTo expected
    }

//    @Test
    fun `should return success with valid values not mock`() {
        initializeInstance()

        val worker = ConfigWorker(ctx, workParam)
        val expected = if (HostAppInfoRepository.instance().getConfigUrl().isNullOrEmpty()) {
            ListenableWorker.Result.retry()
        } else ListenableWorker.Result.success()
        worker.doWork() shouldBeEqualTo expected
    }

    @Test
    fun `should return success but will trigger next config when too many request error`() {
        `when`(mockResponse?.isSuccessful).thenReturn(false)
        `when`(mockResponse?.code()).thenReturn(RetryDelayUtil.RETRY_ERROR_CODE)
        ConfigWorker(
            ctx, workParam, HostAppInfoRepository.instance(), ConfigResponseRepository.instance(),
            MessageMixerPingScheduler.instance(), mockConfigSched, mockRetry
        )
            .onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()

        Mockito.verify(mockConfigSched).startConfig(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY), anyOrNull())
        Mockito.verify(mockRetry).getNextDelay(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY))
    }

    @Test
    fun `should use correct backoff delay`() {
        `when`(mockResponse?.isSuccessful).thenReturn(false)
        `when`(mockResponse?.code()).thenReturn(RetryDelayUtil.RETRY_ERROR_CODE)
        val worker = ConfigWorker(
            ctx, workParam, HostAppInfoRepository.instance(),
            ConfigResponseRepository.instance(), MessageMixerPingScheduler.instance(), mockConfigSched
        )
        verifyBackoff(worker)
    }

    @Test
    fun `should reset initial delay`() {
        `when`(mockResponse?.isSuccessful).thenReturn(false)
        `when`(mockResponse?.code()).thenReturn(RetryDelayUtil.RETRY_ERROR_CODE)
        setupErrorBody()
        val worker = ConfigWorker(
            ctx, workParam, mockHostRepo,
            mockConfigRepo, mockMsgSched, mockConfigSched
        )
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockConfigSched).startConfig(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY), anyOrNull())

        `when`(mockResponse.isSuccessful).thenReturn(true)
        `when`(mockResponse.code()).thenReturn(200)
        `when`(mockResponse.body()).thenReturn(Mockito.mock(ConfigResponse::class.java))
        worker.onResponse(mockResponse) shouldBeEqualTo ListenableWorker.Result.Success()
        ConfigScheduler.currDelay shouldBeEqualTo RetryDelayUtil.INITIAL_BACKOFF_DELAY
    }

    @Test
    fun `should use correct backoff delay if server fail less than 3 times`() {
        initializeInstance()
        InAppMessaging.instance() shouldBeInstanceOf InApp::class.java
        `when`(mockResponse?.isSuccessful).thenReturn(false)
        `when`(mockResponse?.code()).thenReturn(HttpURLConnection.HTTP_INTERNAL_ERROR)
        setupErrorBody()
        val worker = ConfigWorker(
            ctx, workParam, HostAppInfoRepository.instance(),
            ConfigResponseRepository.instance(), MessageMixerPingScheduler.instance(), mockConfigSched
        )

        verifyBackoff(worker)

        // should not reset instance
        InAppMessaging.instance() shouldBeInstanceOf InApp::class.java
    }

    private fun verifyBackoff(worker: ConfigWorker) {
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockConfigSched).startConfig(eq(RetryDelayUtil.INITIAL_BACKOFF_DELAY), anyOrNull())
        ConfigScheduler.currDelay shouldBeGreaterThan (RetryDelayUtil.INITIAL_BACKOFF_DELAY * 2)

        worker.onResponse(mockResponse) shouldBeEqualTo ListenableWorker.Result.Success()
        Mockito.verify(mockConfigSched).startConfig(
            AdditionalMatchers.gt(RetryDelayUtil.INITIAL_BACKOFF_DELAY * 2), anyOrNull()
        )
        ConfigScheduler.currDelay shouldBeGreaterThan (RetryDelayUtil.INITIAL_BACKOFF_DELAY * 4)
    }
}

class ConfigWorkerFailSpec : ConfigWorkerSpec() {
    @Test
    fun `should fail if server fail more than 3 times`() {
        initializeInstance()
        InAppMessaging.instance() shouldBeInstanceOf InApp::class.java
        `when`(mockResponse?.isSuccessful).thenReturn(false)
        `when`(mockResponse?.code()).thenReturn(HttpURLConnection.HTTP_INTERNAL_ERROR)
        `when`(mockResponse?.body()).thenReturn(null)
        val worker = ConfigWorker(
            ctx, workParam, HostAppInfoRepository.instance(),
            ConfigResponseRepository.instance(), MessageMixerPingScheduler.instance(), mockConfigSched
        )
        repeat(3) {
            worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.success()
        }
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.failure()

        // should not reset instance
        InAppMessaging.instance() shouldBeInstanceOf InApp::class.java
    }

    @Test
    fun `should fail if request fail`() {
        initializeInstance()
        InAppMessaging.instance() shouldBeInstanceOf InApp::class.java
        `when`(mockResponse?.isSuccessful).thenReturn(false)
        `when`(mockResponse?.code()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST)
        val worker = ConfigWorker(ctx, workParam)
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.failure()

        setupErrorBody()
        worker.onResponse(mockResponse!!) shouldBeEqualTo ListenableWorker.Result.failure()

        // should reset instance
        InAppMessaging.instance() shouldBeInstanceOf InAppMessaging.NotConfiguredInAppMessaging::class.java
    }

    @Test
    fun `should fail if hostapp id is empty`() {
        `when`(mockHostRepo.getPackageName()).thenReturn("")
        val worker = ConfigWorker(
            ctx, workParam, mockHostRepo, ConfigResponseRepository.instance(),
            MessageMixerPingScheduler.instance()
        )
        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should fail if hostapp version is empty`() {
        `when`(mockHostRepo.getPackageName()).thenReturn("valid.package.name")
        `when`(mockHostRepo.getVersion()).thenReturn("")
        val worker = ConfigWorker(
            ctx, workParam, mockHostRepo, ConfigResponseRepository.instance(),
            MessageMixerPingScheduler.instance()
        )
        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should fail if subscription key is empty`() {
        `when`(mockHostRepo.getPackageName()).thenReturn("valid.package.name")
        `when`(mockHostRepo.getVersion()).thenReturn("valid.version")
        `when`(mockHostRepo.getSubscriptionKey()).thenReturn("")
        val worker = ConfigWorker(
            ctx, workParam, mockHostRepo, ConfigResponseRepository.instance(),
            MessageMixerPingScheduler.instance()
        )
        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }
}
