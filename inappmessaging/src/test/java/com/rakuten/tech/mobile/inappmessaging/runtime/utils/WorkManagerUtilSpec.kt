package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import androidx.work.NetworkType
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import org.amshove.kluent.shouldEqual
import org.junit.Test

/**
 * Test class of WorkManagerUtil.
 */
class WorkManagerUtilSpec : BaseTest() {

    @Test
    fun `should get correct network connected constraint`() {
        val constraints = WorkManagerUtil.getNetworkConnectedConstraint()
        constraints.requiredNetworkType shouldEqual NetworkType.CONNECTED
    }
}
