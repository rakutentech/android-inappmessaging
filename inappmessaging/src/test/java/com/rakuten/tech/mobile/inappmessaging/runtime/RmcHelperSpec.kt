package com.rakuten.tech.mobile.inappmessaging.runtime

import com.nhaarman.mockitokotlin2.any
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.CommonUtil
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic

class RmcHelperSpec {

    private lateinit var mockCommonUtil: MockedStatic<CommonUtil>

    @Before
    fun setup() {
        mockCommonUtil = mockStatic(CommonUtil::class.java)
    }

    @After
    fun tearDown() {
        mockCommonUtil.close()
    }

    @Test
    fun `isUsingRmc should return true`() {
        mockCommonUtil.`when`<Any> { CommonUtil.hasClass(any()) }.thenReturn(true)

        RmcHelper.isUsingRmc() shouldBeEqualTo true
    }

    @Test
    fun `isUsingRmc should return false`() {
        mockCommonUtil.`when`<Any> { CommonUtil.hasClass(any()) }.thenReturn(false)

        RmcHelper.isUsingRmc() shouldBeEqualTo false
    }
}
