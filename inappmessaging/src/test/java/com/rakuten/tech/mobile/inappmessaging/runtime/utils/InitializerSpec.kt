package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.rakuten.tech.mobile.inappmessaging.runtime.AppManifestConfig
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.ClassCastException

/**
 * Test class for InitializationWorker.
 */
@RunWith(RobolectricTestRunner::class)
class InitializerSpec : BaseTest() {
    private val workerParameters = Mockito.mock(WorkerParameters::class.java)
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val mockUtil = Mockito.mock(SharedPreferencesUtil::class.java)
    private val mockPref = Mockito.mock(SharedPreferences::class.java)
    private val mockEditor = Mockito.mock(SharedPreferences.Editor::class.java)

    @Before
    fun setup() {
        When calling workerParameters.inputData itReturns Data.EMPTY
        Settings.Secure.putString(context.contentResolver, Settings.Secure.ANDROID_ID, "testid")

        When calling mockUtil.createSharedPreference(context, "uuid") itReturns mockPref
    }

    @Test
    fun `should add host app info with basic attributes`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context!!)
        InAppMessaging.initialize(context, true)
        InAppMessaging.instance().registerPreference(TestUserInfoProvider())
        HostAppInfoRepository.instance()
                .getInAppMessagingSubscriptionKey() shouldBeEqualTo AppManifestConfig(context).subscriptionKey()
        HostAppInfoRepository.instance()
                .getPackageName() shouldBeEqualTo "com.rakuten.tech.mobile.inappmessaging.runtime.test"
        HostAppInfoRepository.instance().getVersion() shouldBeEqualTo "1.0.2"
    }

    @Test
    fun `should not throw exception`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        Initializer.initializeSdk(context, "test", "", true)
    }

    @Test(expected = InAppMessagingException::class)
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

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `should generate uuid when null android ID and device below android m with empty pref`() {
        val appCtx = ApplicationProvider.getApplicationContext<Context>()
        Settings.Secure.putString(appCtx.contentResolver, Settings.Secure.ANDROID_ID, null)

        // clear preferences
        val sharedPref = SharedPreferencesUtil.createSharedPreference(context, "uuid")
        sharedPref.edit().clear().apply()

        Initializer.initializeSdk(appCtx, "test", "", true)

        HostAppInfoRepository.instance().getDeviceId().shouldNotBeNullOrEmpty()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `should generate uuid when null android ID and device below android m with non-empty pref`() {
        val appCtx = ApplicationProvider.getApplicationContext<Context>()
        Settings.Secure.putString(appCtx.contentResolver, Settings.Secure.ANDROID_ID, null)

        // add test value
        val sharedPref = SharedPreferencesUtil.createSharedPreference(context, "uuid")
        sharedPref.edit().clear().apply()
        sharedPref.edit().putString(Initializer.ID_KEY, "test_uuid").apply()

        Initializer.initializeSdk(appCtx, "test", "", true)

        HostAppInfoRepository.instance().getDeviceId() shouldBeEqualTo "test_uuid"
    }

    @Test
    fun `should generate uuid using mock with null android ID with empty pref`() {
        Settings.Secure.putString(context.contentResolver, Settings.Secure.ANDROID_ID, null)

        When calling mockPref.contains(Initializer.ID_KEY) itReturns false
        When calling mockPref.edit() itReturns mockEditor
        When calling mockEditor.putString(any(), any()) itReturns mockEditor

        Initializer.initializeSdk(context, "test", "", true, mockUtil)

        Mockito.verify(mockUtil).createSharedPreference(context, "uuid")
        Mockito.verify(mockPref).contains(Initializer.ID_KEY)
        Mockito.verify(mockPref).edit()
        Mockito.verify(mockEditor).putString(eq(Initializer.ID_KEY), any())
        HostAppInfoRepository.instance().getDeviceId().shouldNotBeNullOrEmpty()
    }

    @Test
    fun `should generate uuid using mock with null android ID with non-empty pref`() {
        Settings.Secure.putString(context.contentResolver, Settings.Secure.ANDROID_ID, null)

        When calling mockPref.contains(Initializer.ID_KEY) itReturns true
        When calling mockPref.getString(Initializer.ID_KEY, "") itReturns "random_uuid"

        Initializer.initializeSdk(context, "test", "", true, mockUtil)

        Mockito.verify(mockUtil).createSharedPreference(context, "uuid")
        Mockito.verify(mockPref).contains(Initializer.ID_KEY)
        Mockito.verify(mockPref, never()).edit()
        Mockito.verify(mockPref).getString(Initializer.ID_KEY, "")
        HostAppInfoRepository.instance().getDeviceId() shouldBeEqualTo "random_uuid"
    }

    @Test
    fun `should not crash and generate new if forced exception`() {
        Settings.Secure.putString(context.contentResolver, Settings.Secure.ANDROID_ID, null)

        When calling mockPref.contains(Initializer.ID_KEY) itReturns true
        When calling mockPref.getString(Initializer.ID_KEY, "") itThrows ClassCastException()
        When calling mockPref.edit() itReturns mockEditor
        When calling mockEditor.putString(any(), any()) itReturns mockEditor

        Initializer.initializeSdk(context, "test", "", true, mockUtil)

        Mockito.verify(mockUtil).createSharedPreference(context, "uuid")
        Mockito.verify(mockPref).contains(Initializer.ID_KEY)
        Mockito.verify(mockPref).edit()
        Mockito.verify(mockPref).getString(Initializer.ID_KEY, "")
        Mockito.verify(mockEditor).putString(eq(Initializer.ID_KEY), any())
        HostAppInfoRepository.instance().getDeviceId().shouldNotBeNullOrEmpty()
    }
}
