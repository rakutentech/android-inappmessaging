package com.rakuten.tech.mobile.inappmessaging.runtime

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ClassUtil
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RmcHelperSpec {

    private val mockContext = mock(Context::class.java)
    private val mockClassUtil = mockStatic(ClassUtil::class.java)

    @After
    fun tearDown() {
        mockClassUtil.close()
    }

    @Test
    fun `isRmcIntegrated should return true`() {
        mockClassUtil.`when`<Any> { ClassUtil.hasClass(anyString()) }.thenReturn(true)

        RmcHelper.isRmcIntegrated() shouldBeEqualTo true
    }

    @Test
    fun `isRmcIntegrated should return false`() {
        mockClassUtil.`when`<Any> { ClassUtil.hasClass(anyString()) }.thenReturn(false)

        RmcHelper.isRmcIntegrated() shouldBeEqualTo false
    }

    @Test
    fun `getRmcVersion should return version from resource`() {
        mockClassUtil.`when`<Any> { ClassUtil.hasClass(anyString()) }.thenReturn(true)
        `when`(mockContext.getString(anyInt())).thenReturn(InAppMessagingTestConstants.RMC_VERSION)
        `when`(mockContext.resources).thenReturn(mock(Resources::class.java))

        RmcHelper.getRmcVersion(mockContext) shouldBeEqualTo InAppMessagingTestConstants.RMC_VERSION
    }

    @Test
    fun `getRmcVersion should return null if resource does not exist`() {
        mockClassUtil.`when`<Any> { ClassUtil.hasClass(anyString()) }.thenReturn(true)

        RmcHelper.getRmcVersion(ApplicationProvider.getApplicationContext()) shouldBeEqualTo null
    }
}
