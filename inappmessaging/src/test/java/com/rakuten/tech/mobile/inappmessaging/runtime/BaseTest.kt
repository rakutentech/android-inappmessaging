package com.rakuten.tech.mobile.inappmessaging.runtime

import org.junit.After
import org.mockito.Mockito
import org.robolectric.annotation.Config

/**
 * Base test class of all test classes.
 */
@Config(sdk = [22])
open class BaseTest {
    /**
     * See [Memory leak in mockito-inline...](https://github.com/mockito/mockito/issues/1614)
     */
    @After
    fun clearMocks() {
        Mockito.framework().clearInlineMocks()
    }
}
