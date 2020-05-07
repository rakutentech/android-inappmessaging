package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import org.amshove.kluent.*
import org.junit.Test
import org.mockito.Mockito

/**
 * Test class for RuntimeUtil.
 */
class RuntimeUtilSpec : BaseTest() {

    @Test
    fun `should get user identifiers not null`() {
        RuntimeUtil.getUserIdentifiers().shouldNotBeNull()
    }

    @Test
    fun `should get user identifier with user id`() {
        val mockProvider = Mockito.mock(UserInfoProvider::class.java)
        When calling mockProvider.provideUserId() itReturns "test_user_id"
        When calling mockProvider.provideRakutenId() itReturns ""
        AccountRepository.instance().userInfoProvider = mockProvider
        RuntimeUtil.getUserIdentifiers().shouldHaveSize(1)
    }

    @Test
    fun `should get user identifier with rakuten id`() {
        val mockProvider = Mockito.mock(UserInfoProvider::class.java)
        When calling mockProvider.provideUserId() itReturns ""
        When calling mockProvider.provideRakutenId() itReturns "test_rakuten_id"
        AccountRepository.instance().userInfoProvider = mockProvider
        RuntimeUtil.getUserIdentifiers().shouldHaveSize(1)
    }

    @Test
    fun `should get user identifier with both user and rakuten id`() {
        val mockProvider = Mockito.mock(UserInfoProvider::class.java)
        When calling mockProvider.provideUserId() itReturns "test_user_id"
        When calling mockProvider.provideRakutenId() itReturns "test_rakuten_id"
        AccountRepository.instance().userInfoProvider = mockProvider
        RuntimeUtil.getUserIdentifiers().shouldHaveSize(2)
    }

    @Test
    fun `should get retrofit with proper attributes`() {
        val retrofit = RuntimeUtil.getRetrofit()
        retrofit.baseUrl().shouldNotBeNull()
        retrofit.converterFactories().shouldNotBeNull()
        retrofit.callbackExecutor().shouldNotBeNull()
    }

    @Test
    fun `should get null with invalid url`() {
        RuntimeUtil.getImage("invalid").shouldBeNull()
    }
}
