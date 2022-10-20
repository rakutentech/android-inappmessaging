package com.rakuten.test.helpers

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers
import com.rakuten.test.MainActivity
import com.rakuten.test.R
import okhttp3.mockwebserver.MockWebServer

object Utils {
    fun performNormalFlow(mockServer: MockWebServer, jsonFileName: String, sleep: Long = 1000) {
        MockServerHelper.pingJsonFilename = jsonFileName
        mockServer.dispatcher = MockServerHelper.dispatcher

        val scenario = launchActivity<MainActivity>()

        // Workaround to trigger `configure` for every test
        // because launchActivity and close does not seem to kill app not triggering ping.
        onView(ViewMatchers.withId(R.id.reconfigure)).perform(click())
        onView(ViewMatchers.withId(R.id.edit_config_url)).perform(replaceText(Constants.CONFIG_URL))
        onView(ViewMatchers.withText(android.R.string.ok)).perform(click())

        onView(ViewMatchers.withId(R.id.login_successful)).perform(click())
        Thread.sleep(sleep)
        onView(ViewMatchers.withId(com.rakuten.tech.mobile.inappmessaging.runtime.R.id.message_close_button))
            .perform(click())

        scenario.close()
    }
}