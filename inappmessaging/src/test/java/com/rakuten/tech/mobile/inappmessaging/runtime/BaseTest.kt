package com.rakuten.tech.mobile.inappmessaging.runtime

import org.junit.After
import org.junit.Before
import org.mockito.Mockito
import org.robolectric.annotation.Config

/**
 * Base test class of all test classes.
 */
@Config(sdk = [22])
open class BaseTest {
    @Before
    open fun setup() {
        InApp.errorCallback = null
        InAppMessaging.setUninitializedInstance()
    }

    /**
     * See [Memory leak in mockito-inline...](https://github.com/mockito/mockito/issues/1614)
     */
    @After
    open fun tearDown() {
        Mockito.framework().clearInlineMocks()
        InApp.errorCallback = null
        InAppMessaging.setUninitializedInstance()
    }
}
