package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.extensions.isValidUrl
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StringExtensionsSpec {

    @Test
    fun `should validate invalid URL`() {
        "ogle.1245w423321".isValidUrl().shouldBeFalse()
    }

    @Test
    fun `should validate web URL`() {
        "https://www.example.com".isValidUrl().shouldBeTrue()
    }

    @Test
    fun `should validate deeplink URL`() {
        "myapp://open?param=value".isValidUrl().shouldBeTrue()
    }
}
