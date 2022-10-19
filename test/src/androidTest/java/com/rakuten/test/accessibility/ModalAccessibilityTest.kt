package com.rakuten.test.accessibility

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult
import com.rakuten.test.MainActivity
import com.rakuten.test.helpers.Constants
import com.rakuten.test.helpers.MockServerHelper
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.runner.RunWith
import com.rakuten.test.R as AppR
import com.rakuten.tech.mobile.inappmessaging.runtime.R as IamR

@RunWith(AndroidJUnit4::class)
class ModalAccessibilityTest {

    /**
     * Not properly working if running tests all at once probably due to MockWebServer setup.
     * Need to manually execute each test separately.
     *
     */

    @Rule
    @JvmField
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun modalTextOnly() {
        MockServerHelper.doIamRequest(mockServer, "modal-text-only.json")

        onView(withId(AppR.id.login_successful)).perform(click())
        Thread.sleep(1000)
        onView(withId(IamR.id.message_close_button)).perform(click())
    }

    @Test
    fun modalImageOnly() {
        MockServerHelper.doIamRequest(mockServer, "modal-image-only.json")

        onView(withId(AppR.id.login_successful)).perform(click())
        Thread.sleep(1000)
        onView(withId(IamR.id.message_close_button)).perform(click())
    }

    @Test
    fun modalTextImage() {
        MockServerHelper.doIamRequest(mockServer, "modal-text-image.json")

        onView(withId(AppR.id.login_successful)).perform(click())
        Thread.sleep(1000)
        onView(withId(IamR.id.message_close_button)).perform(click())
    }

    companion object {
        private val mockServer = MockWebServer()

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            mockServer.start(Constants.PORT)
            mockServer.url(Constants.PATH)

            AccessibilityChecks.enable()
                .setRunChecksFromRootView(true)
                .setThrowExceptionFor(AccessibilityCheckResult.AccessibilityCheckResultType.WARNING)
        }

        @AfterClass
        fun afterClass() {
            mockServer.shutdown()
        }
    }
}
