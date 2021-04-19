package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.Context
import androidx.work.WorkerParameters
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessagingTestConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.TestUserInfoProvider
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.ImpressionWorker
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

/**
 * Test class for AccountRepository class.
 */
class AccountRepositorySpec : BaseTest() {
    @Before
    fun setup() {
        AccountRepository.instance().userInfoProvider = null
    }

    @Test
    fun `should get rae token`() {
        AccountRepository.instance().getRaeToken() shouldBeEqualTo ""
    }

    @Test
    fun `should get user id`() {
        AccountRepository.instance().getUserId() shouldBeEqualTo ""
    }

    @Test
    fun `should get rakuten id`() {
        AccountRepository.instance().getRakutenId() shouldBeEqualTo ""
    }

    @Test
    fun `should get user id with valid provider`() {
        AccountRepository.instance().userInfoProvider = TestUserInfoProvider()
        AccountRepository.instance().getUserId() shouldBeEqualTo TestUserInfoProvider().provideUserId()
    }

    @Test
    fun `should get rae token with valid provider`() {
        AccountRepository.instance().userInfoProvider = TestUserInfoProvider()
        AccountRepository.instance().getRaeToken() shouldBeEqualTo "OAuth2 " + TestUserInfoProvider().provideRaeToken()
    }

    @Test
    fun `should get rakuten id with valid provider`() {
        AccountRepository.instance().userInfoProvider = TestUserInfoProvider()
        AccountRepository.instance().getRakutenId() shouldBeEqualTo TestUserInfoProvider().provideRakutenId()
    }

    @Test
    fun `should get rae token with null values`() {
        val mockProvider = Mockito.mock(TestUserInfoProvider::class.java)
        When calling mockProvider.provideRaeToken() itReturns null
        AccountRepository.instance().userInfoProvider = mockProvider
        AccountRepository.instance().getRaeToken() shouldBeEqualTo ""
    }

    @Test
    fun `should get user id with null values`() {
        val mockProvider = Mockito.mock(TestUserInfoProvider::class.java)
        When calling mockProvider.provideUserId() itReturns null
        AccountRepository.instance().userInfoProvider = mockProvider
        AccountRepository.instance().getUserId() shouldBeEqualTo ""
    }

    @Test
    fun `should get rakuten id with null values`() {
        val mockProvider = Mockito.mock(TestUserInfoProvider::class.java)
        When calling mockProvider.provideRakutenId() itReturns null
        AccountRepository.instance().userInfoProvider = mockProvider
        AccountRepository.instance().getRakutenId() shouldBeEqualTo ""
    }

    @Test
    fun `should get be called once for get rae token`() {
        val mockAcctRepo = Mockito.mock(AccountRepository::class.java)

        When calling mockAcctRepo.userInfoProvider itReturns TestUserInfoProvider()
        When calling mockAcctRepo.getRaeToken() itReturns TestUserInfoProvider().provideRaeToken().toString()
        HostAppInfoRepository.instance().addHostInfo(HostAppInfo(InAppMessagingTestConstants.APP_ID,
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                InAppMessagingTestConstants.SUB_KEY, InAppMessagingTestConstants.LOCALE))
        val context = Mockito.mock(Context::class.java)
        val workerParameters = Mockito.mock(WorkerParameters::class.java)
        val impressionRequest = Mockito.mock(ImpressionRequest::class.java)
        val worker = ImpressionWorker(context, workerParameters)
        worker.createReportImpressionCall("https://host/impression/", impressionRequest,
                accountRepo = mockAcctRepo)
        Mockito.verify(mockAcctRepo, Mockito.times(1)).getRaeToken()
    }

    @Test
    fun `should get be called once for get user id`() {
        val mockAcctRepo = Mockito.mock(AccountRepository::class.java)

        When calling mockAcctRepo.userInfoProvider itReturns TestUserInfoProvider()
        When calling mockAcctRepo.getUserId() itReturns TestUserInfoProvider().provideUserId().toString()
        When calling mockAcctRepo.getRakutenId() itReturns ""
        RuntimeUtil.getUserIdentifiers(mockAcctRepo).shouldHaveSize(1)
        Mockito.verify(mockAcctRepo, Mockito.times(1)).getUserId()
    }

    @Test
    fun `should get be called once for get rakuten id`() {
        val mockAcctRepo = Mockito.mock(AccountRepository::class.java)

        When calling mockAcctRepo.userInfoProvider itReturns TestUserInfoProvider()
        When calling mockAcctRepo.getUserId() itReturns ""
        When calling mockAcctRepo.getRakutenId() itReturns TestUserInfoProvider().provideRakutenId().toString()
        RuntimeUtil.getUserIdentifiers(mockAcctRepo).shouldHaveSize(1)
        Mockito.verify(mockAcctRepo, Mockito.times(1)).getRakutenId()
    }

    @Test
    fun `should return false if no update`() {
        val infoProvider = TestUserInfoProvider()
        AccountRepository.instance().userInfoProvider = infoProvider
        // initial setting of hashed user info
        AccountRepository.instance().updateUserInfo().shouldBeTrue()

        AccountRepository.instance().updateUserInfo().shouldBeFalse()
    }

    @Test
    fun `should return true for changed user id`() {
        val infoProvider = TestUserInfoProvider()
        AccountRepository.instance().userInfoProvider = infoProvider
        // initial setting of hashed user info
        AccountRepository.instance().updateUserInfo()

        // updated
        infoProvider.userId = "user-id"

        AccountRepository.instance().updateUserInfo().shouldBeTrue()
    }

    @Test
    fun `should return true for changed rakuten id`() {
        val infoProvider = TestUserInfoProvider()
        AccountRepository.instance().userInfoProvider = infoProvider
        // initial setting of hashed user info
        AccountRepository.instance().updateUserInfo()

        // updated
        infoProvider.rakutenId = "rakuten-id"

        AccountRepository.instance().updateUserInfo().shouldBeTrue()
    }

    @Test
    fun `should return false for changed rae token`() {
        val infoProvider = TestUserInfoProvider()
        AccountRepository.instance().userInfoProvider = infoProvider
        // initial setting of hashed user info
        AccountRepository.instance().updateUserInfo()

        // updated
        infoProvider.raeToken = "rae-token"

        AccountRepository.instance().updateUserInfo().shouldBeFalse()
    }
}
