package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.InApp.AppManifestConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for InitializationWorker.
 */
@RunWith(RobolectricTestRunner::class)
class InitializerSpec : BaseTest() {
    private val workerParameters = Mockito.mock(WorkerParameters::class.java)
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    override fun setup() {
        super.setup()
        `when`(workerParameters.inputData).thenReturn(Data.EMPTY)
        Settings.Secure.putString(context.contentResolver, Settings.Secure.ANDROID_ID, "testid")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M, Build.VERSION_CODES.S])
    fun `should add host app info with basic attributes`() {
        verifyHostAppInfo()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    @Ignore("Robolectric returns null metadata for API 33")
    fun `should add host app info with basic attributes in API 33`() {
        verifyHostAppInfo()
    }

    @Test
    fun `should not throw exception`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        Initializer.initializeSdk(context, "test", "")
    }

    @Test(expected = InAppMessagingException::class)
    fun `should throw exception with null package name`() {
        val context = Mockito.mock(Context::class.java)
        Settings.Secure.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.Secure.ANDROID_ID,
            "test_device_id",
        )
        `when`(context.contentResolver).thenReturn(ApplicationProvider.getApplicationContext<Context>().contentResolver)
        `when`(context.packageManager).thenReturn(ApplicationProvider.getApplicationContext<Context>().packageManager)
        `when`(context.resources).thenReturn(ApplicationProvider.getApplicationContext<Context>().resources)
        Initializer.initializeSdk(context, "test", "")
    }

    @Test
    fun `should generate uuid when null android ID and device with empty pref`() {
        val appCtx = ApplicationProvider.getApplicationContext<Context>()
        Settings.Secure.putString(appCtx.contentResolver, Settings.Secure.ANDROID_ID, null)

        // clear preferences
        PreferencesUtil.clear(context, "uuid")

        Initializer.initializeSdk(appCtx, "test", "")

        HostAppInfoRepository.instance().getDeviceId().shouldNotBeNullOrEmpty()
    }

    @Test
    fun `should generate uuid when null android ID and device with non-empty pref`() {
        val appCtx = ApplicationProvider.getApplicationContext<Context>()
        Settings.Secure.putString(appCtx.contentResolver, Settings.Secure.ANDROID_ID, null)

        // add test value
        PreferencesUtil.putString(context, "uuid", Initializer.ID_KEY, "test_uuid")

        Initializer.initializeSdk(appCtx, "test", "")

        HostAppInfoRepository.instance().getDeviceId() shouldBeEqualTo "test_uuid"
    }

    private fun verifyHostAppInfo() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context!!)
        InAppMessaging.initialize(context, true)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        HostAppInfoRepository.instance().getSubscriptionKey().shouldNotBeNullOrEmpty()
        HostAppInfoRepository.instance()
            .getSubscriptionKey() shouldBeEqualTo AppManifestConfig(context).subscriptionKey()
        HostAppInfoRepository.instance()
            .getPackageName() shouldBeEqualTo "com.rakuten.tech.mobile.inappmessaging.runtime.test"
        HostAppInfoRepository.instance().getVersion() shouldBeEqualTo "1.0.2"
        HostAppInfoRepository.instance().getDeviceLocale().shouldNotBeEmpty()
    }
}
