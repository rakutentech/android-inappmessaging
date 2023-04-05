package com.rakuten.tech.mobile.inappmessaging.runtime

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.mockito.Mockito

/**
 * Base test class of all test classes.
 */
@OptIn(
    ExperimentalCoroutinesApi::class,
)
open class BaseTest {
    internal val testDispatcher = UnconfinedTestDispatcher()

    @Before
    open fun setup() {
        InAppMessaging.errorCallback = null
        InAppMessaging.setNotConfiguredInstance()

        Dispatchers.setMain(testDispatcher)
    }

    /**
     * See [Memory leak in mockito-inline...](https://github.com/mockito/mockito/issues/1614)
     */
    @After
    open fun tearDown() {
        Mockito.framework().clearInlineMocks()
        InAppMessaging.errorCallback = null
        InAppMessaging.setNotConfiguredInstance()

        Dispatchers.resetMain()
    }
}
