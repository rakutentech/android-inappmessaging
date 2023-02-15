package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBePositive
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CacheUtilSpec {

    @Test
    fun `should return 1 if cache size is less than 0`() {
        CacheUtil.getMemoryCacheSize(-1).shouldBeEqualTo(1)
    }

    @Test
    fun `should return 1 if cache size is equal 0`() {
        CacheUtil.getMemoryCacheSize(0).shouldBeEqualTo(1)
    }

    @Test
    fun `should return positive cache size`() {
        CacheUtil.getMemoryCacheSize().shouldBePositive()
    }
}
