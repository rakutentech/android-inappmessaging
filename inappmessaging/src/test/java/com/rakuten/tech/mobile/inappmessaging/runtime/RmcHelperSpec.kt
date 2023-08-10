package com.rakuten.tech.mobile.inappmessaging.runtime

import com.nhaarman.mockitokotlin2.any
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.CommonUtil
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Test
import org.mockito.Mockito.mockStatic

class RmcHelperSpec {

    private val mockCommonUtil = mockStatic(CommonUtil::class.java)

    @After
    fun tearDown() {
        mockCommonUtil.close()
    }

    @Test
    fun `isRmcIntegrated should return true`() {
        mockCommonUtil.`when`<Any> { CommonUtil.hasClass(any()) }.thenReturn(true)

        RmcHelper.isRmcIntegrated() shouldBeEqualTo true
    }

    @Test
    fun `isRmcIntegrated should return false`() {
        mockCommonUtil.`when`<Any> { CommonUtil.hasClass(any()) }.thenReturn(false)

        RmcHelper.isRmcIntegrated() shouldBeEqualTo false
    }
}
