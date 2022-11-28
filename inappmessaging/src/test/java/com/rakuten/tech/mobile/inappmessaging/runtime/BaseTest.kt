package com.rakuten.tech.mobile.inappmessaging.runtime

import android.os.Build
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import org.junit.After
import org.junit.Before
import org.mockito.Mockito
import org.robolectric.annotation.Config

/**
 * Base test class of all test classes.
 */
@Config(sdk = [Build.VERSION_CODES.M])
open class BaseTest {

    internal val testAppInfo = HostAppInfo(
        InAppMessagingTestConstants.APP_ID, InAppMessagingTestConstants.DEVICE_ID,
        InAppMessagingTestConstants.APP_VERSION, InAppMessagingTestConstants.SUB_KEY,
        InAppMessagingTestConstants.LOCALE, isTooltipEnabled = true
    )

    @Before
    open fun setup() {
        InAppMessaging.errorCallback = null
        InAppMessaging.setNotConfiguredInstance()
        HostAppInfoRepository.instance().addHostInfo(testAppInfo)
    }

    /**
     * See [Memory leak in mockito-inline...](https://github.com/mockito/mockito/issues/1614)
     */
    @After
    open fun tearDown() {
        Mockito.framework().clearInlineMocks()
        InAppMessaging.errorCallback = null
        InAppMessaging.setNotConfiguredInstance()
    }
}
