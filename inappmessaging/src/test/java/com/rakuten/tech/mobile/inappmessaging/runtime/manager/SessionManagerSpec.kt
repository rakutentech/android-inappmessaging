package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseEndpoints
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.SessionManager.onSessionUpdate
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldHaveSize
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.ExecutionException

/**
 * Test class for SessionManager.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SessionManagerSpec : BaseTest() {

    private var configResponseData = Mockito.mock(ConfigResponseData::class.java)
    private var endpoints = Mockito.mock(ConfigResponseEndpoints::class.java)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun `should start ping message mixer on log in successful`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        Settings.Secure.putString(
                ApplicationProvider.getApplicationContext<Context>().contentResolver,
                Settings.Secure.ANDROID_ID,
                "test_device_id")
        InAppMessaging.init(ApplicationProvider.getApplicationContext(), "test", "",
                isDebugLogging = true, isForTesting = true)
        When calling configResponseData.enabled itReturns true
        When calling configResponseData.endpoints itReturns endpoints
        ConfigResponseRepository.instance().addConfigResponse(configResponseData)
        onSessionUpdate()
        WorkManager.getInstance(ApplicationProvider.getApplicationContext())
                .getWorkInfosByTag(MESSAGE_MIXER_PING_WORKER).get().shouldHaveSize(1)
    }

    companion object {
        private const val MESSAGE_MIXER_PING_WORKER = "iam_message_mixer_worker"
    }
}
