package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingInitializationException
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for InitializationWorker.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class InitializerSpec : BaseTest() {
    private var workerParameters = Mockito.mock(WorkerParameters::class.java)
    private var context: Context? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        When calling workerParameters.inputData itReturns Data.EMPTY
        context = ApplicationProvider.getApplicationContext()
        Settings.Secure.putString(context?.contentResolver, Settings.Secure.ANDROID_ID, "testid")
    }

    @Test
    fun `should add host app info with basic attributes`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context!!)
        InAppMessaging.init(
                context!!,
                "test_sub_key",
                "",
                isDebugLogging = false,
                isForTesting = true)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        HostAppInfoRepository.instance().getInAppMessagingSubscriptionKey() shouldEqual "test_sub_key"
        HostAppInfoRepository.instance()
                .getPackageName() shouldEqual "com.rakuten.tech.mobile.inappmessaging.runtime.test"
        HostAppInfoRepository.instance().getVersion() shouldEqual "version.name.101"
    }

    @Test
    fun `should not throw exception`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Initializer.initializeSdk(
                ApplicationProvider.getApplicationContext(),
                "test", "", true)
    }

    @Test(expected = InAppMessagingInitializationException::class)
    fun `should throw exception with null package name`() {
        val context = Mockito.mock(Context::class.java)
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        When calling context.contentResolver itReturns ApplicationProvider.getApplicationContext<Context>()
                .contentResolver
        When calling context.packageManager itReturns ApplicationProvider.getApplicationContext<Context>()
                .packageManager
        When calling context.resources itReturns ApplicationProvider.getApplicationContext<Context>()
                .resources
        Initializer.initializeSdk(context, "test", "", true)
    }
}
