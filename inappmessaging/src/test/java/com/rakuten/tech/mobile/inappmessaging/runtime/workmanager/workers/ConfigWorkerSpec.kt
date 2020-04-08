package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessagingTestConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
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

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should retry if server fail`() {
        When calling mockResponse?.isSuccessful itReturns false
        When calling mockResponse?.code() itReturns 500
        When calling mockResponse?.body() itReturns null
        ConfigWorker(context, workerParameters).onResponse(mockResponse!!) shouldEqual ListenableWorker.Result.retry()
    }

    @Test
    fun `should fail if request fail`() {
        When calling mockResponse?.isSuccessful itReturns false
        When calling mockResponse?.code() itReturns 400
        val worker = ConfigWorker(context, workerParameters)
        worker.onResponse(mockResponse!!) shouldEqual ListenableWorker.Result.failure()
    }

    @Test
    fun `should fail if hostapp id is null`() {
        When calling mockHostRespository.getPackageName() itReturns null
        val worker = ConfigWorker(context, workerParameters, mockHostRespository, ConfigResponseRepository.instance(),
                MessageMixerPingScheduler.instance())
        worker.doWork() shouldEqual ListenableWorker.Result.failure()
    }

    @Test
    fun `should fail if hostapp id is empty`() {
        When calling mockHostRespository.getPackageName() itReturns ""
        val worker = ConfigWorker(context, workerParameters, mockHostRespository, ConfigResponseRepository.instance(),
                MessageMixerPingScheduler.instance())
        worker.doWork() shouldEqual ListenableWorker.Result.failure()
    }

    @Test
    fun `should fail if hostapp version is null`() {
        When calling mockHostRespository.getPackageName() itReturns "valid.package.name"
        val worker = ConfigWorker(context, workerParameters, mockHostRespository, ConfigResponseRepository.instance(),
                MessageMixerPingScheduler.instance())
        worker.doWork() shouldEqual ListenableWorker.Result.failure()
    }

    @Test
    fun `should fail if hostapp version is empty`() {
        When calling mockHostRespository.getPackageName() itReturns "valid.package.name"
        When calling mockHostRespository.getVersion() itReturns ""
        val worker = ConfigWorker(context, workerParameters, mockHostRespository, ConfigResponseRepository.instance(),
                MessageMixerPingScheduler.instance())
        worker.doWork() shouldEqual ListenableWorker.Result.failure()
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
        val worker = ConfigWorker(context, workerParameters, mockHostRespository, mockConfigRespository,
                mockMessageScheduler)
        worker.doWork() shouldEqual ListenableWorker.Result.retry() // success if valid CONFIG_URL is set
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
        val app = context.packageManager.getApplicationInfo(context.packageName,
                PackageManager.GET_META_DATA)
        val bundle = app.metaData

        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test",
                bundle.getString(CONFIG_KEY, ""),
                isDebugLogging = false, isForTesting = true)
        val worker = ConfigWorker(context, workerParameters)
        worker.doWork() shouldEqual ListenableWorker.Result.retry() // success if valid CONFIG_URL is set
    }

    companion object {
        private const val CONFIG_KEY = "com.rakuten.tech.mobile.inappmessaging.configurl"
    }
}
