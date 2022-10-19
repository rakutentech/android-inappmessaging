package com.rakuten.test.accessibility

import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult
import com.rakuten.test.helpers.Utils
import com.rakuten.test.helpers.Constants
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModalAccessibilityTest {
    private var mockServer = MockWebServer()

    @Before
    fun beforeTest() {
        mockServer.start(Constants.PORT)
        mockServer.url(Constants.PATH)
    }

    @After
    fun afterTest() {
        mockServer.shutdown()
    }

    @Test
    fun modalTextOnly() {
        Utils.performNormalFlow(mockServer, "modal-text-only.json")
    }

    @Test
    fun modalImageOnly() {
        Utils.performNormalFlow(mockServer,"modal-image-only.json", 3000)
    }

    @Test
    fun modalTextImage() {
        Utils.performNormalFlow(mockServer,"modal-text-image.json", 3000)
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            AccessibilityChecks.enable()
                .setRunChecksFromRootView(true)
                .setThrowExceptionFor(AccessibilityCheckResult.AccessibilityCheckResultType.WARNING)
        }
    }
}
