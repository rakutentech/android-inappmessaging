package com.rakuten.tech.mobile.inappmessaging.runtime

import com.nhaarman.mockitokotlin2.any
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.CommonUtil
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.mockito.Mockito.mockStatic

class RmcHelperSpec {

    @Test
    fun `isUsingRmc should return true`() {
        mockStatic(CommonUtil::class.java).use { mockedUtil ->
            mockedUtil.`when`<Any> { CommonUtil.hasClass(any()) }.thenReturn(true)

            RmcHelper.isUsingRmc() shouldBeEqualTo true
        }
    }

    @Test
    fun `isUsingRmc should return false`() {
        mockStatic(CommonUtil::class.java).use { mockedUtil ->
            mockedUtil.`when`<Any> { CommonUtil.hasClass(any()) }.thenReturn(false)

            RmcHelper.isUsingRmc() shouldBeEqualTo false
        }
    }
}
