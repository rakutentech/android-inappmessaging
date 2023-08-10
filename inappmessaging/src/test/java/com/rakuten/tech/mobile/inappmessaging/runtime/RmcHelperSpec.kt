package com.rakuten.tech.mobile.inappmessaging.runtime

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RmcHelperSpec {

    private val mockContext = mock(Context::class.java)

    @Before
    fun setup() {
        `when`(mockContext.resources).thenReturn(mock(Resources::class.java))
    }

    @Test
    fun `isRmcIntegrated should return true`() {
        `when`(mockContext.getString(anyInt())).thenReturn("1.0.0")

        RmcHelper.isRmcIntegrated(mockContext) shouldBeEqualTo true
    }

    @Test
    fun `isRmcIntegrated should return false`() {
        `when`(mockContext.getString(anyInt())).thenReturn(null)

        RmcHelper.isRmcIntegrated(mockContext) shouldBeEqualTo false
    }

    @Test
    fun `getRmcVersion should return version from resource`() {
        `when`(mockContext.getString(anyInt())).thenReturn("1.0.0")

        RmcHelper.getRmcVersion(mockContext) shouldBeEqualTo "1.0.0"
    }

    @Test
    fun `getRmcVersion should return null if resource does not exist`() {
        RmcHelper.getRmcVersion(ApplicationProvider.getApplicationContext()) shouldBeEqualTo null
    }
}
