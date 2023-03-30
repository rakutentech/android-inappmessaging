package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import org.amshove.kluent.*
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

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
        `when`(mockProvider.provideUserId()).thenReturn("test_user_id")
        AccountRepository.instance().userInfoProvider = mockProvider
        RuntimeUtil.getUserIdentifiers().shouldHaveSize(1)
    }

    @Test
    fun `should get user identifier with tracking identifier`() {
        val mockProvider = Mockito.mock(UserInfoProvider::class.java)
        `when`(mockProvider.provideUserId()).thenReturn("")
        `when`(mockProvider.provideIdTrackingIdentifier()).thenReturn("test_tracking_id")
        AccountRepository.instance().userInfoProvider = mockProvider
        RuntimeUtil.getUserIdentifiers().shouldHaveSize(1)
    }

    @Test
    fun `should get user identifier with both user id and tracking identifier`() {
        val mockProvider = Mockito.mock(UserInfoProvider::class.java)
        `when`(mockProvider.provideUserId()).thenReturn("test_user_id")
        `when`(mockProvider.provideIdTrackingIdentifier()).thenReturn("test_tracking_id")
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
}
