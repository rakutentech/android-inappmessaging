package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.Context
import androidx.work.WorkerParameters
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.never
import com.rakuten.tech.mobile.inappmessaging.runtime.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.ImpressionWorker
import org.amshove.kluent.*
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import timber.log.Timber

/**
 * Test class for AccountRepository class.
 */
@Ignore("base class")
open class AccountRepositorySpec : BaseTest() {
    @Before
    override fun setup() {
        super.setup()
        AccountRepository.instance().userInfoProvider = null
    }
}
class AccountRepositoryDefaultSpec : AccountRepositorySpec() {

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
    fun `should get id tracking identifier`() {
        AccountRepository.instance().getIdTrackingIdentifier() shouldBeEqualTo ""
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
    fun `should get id tracking identifier with valid provider`() {
        AccountRepository.instance().userInfoProvider = TestUserInfoProvider()
        AccountRepository.instance()
                .getIdTrackingIdentifier() shouldBeEqualTo TestUserInfoProvider().provideIdTrackingIdentifier()
    }
}

class AccountRepositoryNullSpec : AccountRepositorySpec() {

    @Test
    fun `should get rae token with null values`() {
        val mockProvider = Mockito.mock(TestUserInfoProvider::class.java)
        `when`(mockProvider.provideRaeToken()).thenReturn(null)
        AccountRepository.instance().userInfoProvider = mockProvider
        AccountRepository.instance().getRaeToken() shouldBeEqualTo ""
    }

    @Test
    fun `should get user id with null values`() {
        val mockProvider = Mockito.mock(TestUserInfoProvider::class.java)
        `when`(mockProvider.provideUserId()).thenReturn(null)
        AccountRepository.instance().userInfoProvider = mockProvider
        AccountRepository.instance().getUserId() shouldBeEqualTo ""
    }

    @Test
    fun `should get rakuten id with null values`() {
        val mockProvider = Mockito.mock(TestUserInfoProvider::class.java)
        `when`(mockProvider.provideRakutenId()).thenReturn(null)
        AccountRepository.instance().userInfoProvider = mockProvider
        AccountRepository.instance().getRakutenId() shouldBeEqualTo ""
    }

    @Test
    fun `should get id tracking identifier with null values`() {
        val mockProvider = Mockito.mock(TestUserInfoProvider::class.java)
        `when`(mockProvider.provideIdTrackingIdentifier()).thenReturn(null)
        AccountRepository.instance().userInfoProvider = mockProvider
        AccountRepository.instance().getUserId() shouldBeEqualTo ""
    }
}

@SuppressWarnings("LargeClass")
class AccountRepositoryUsageSpec : AccountRepositorySpec() {

    private val mockLogger = Mockito.mock(Timber.Tree::class.java)
    private val captor = argumentCaptor<String>()

    @Test
    fun `should get be called once for get rae token`() {
        val mockAcctRepo = Mockito.mock(AccountRepository::class.java)

        `when`(mockAcctRepo.userInfoProvider).thenReturn(TestUserInfoProvider())
        `when`(mockAcctRepo.getRaeToken()).thenReturn(TestUserInfoProvider().provideRaeToken().toString())
        HostAppInfoRepository.instance().addHostInfo(HostAppInfo(InAppMessagingTestConstants.APP_ID,
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                InAppMessagingTestConstants.SUB_KEY, InAppMessagingTestConstants.LOCALE))
        val context = Mockito.mock(Context::class.java)
        val workerParameters = Mockito.mock(WorkerParameters::class.java)
        val impressionRequest = Mockito.mock(ImpressionRequest::class.java)
        val worker = ImpressionWorker(context, workerParameters)
        worker.createReportImpressionCall("https://host/impression/", impressionRequest,
                accountRepo = mockAcctRepo)
        Mockito.verify(mockAcctRepo).getRaeToken()
    }

    @Test
    fun `should get be called once for get user id`() {
        val mockAcctRepo = Mockito.mock(AccountRepository::class.java)

        `when`(mockAcctRepo.userInfoProvider).thenReturn(TestUserInfoProvider())
        `when`(mockAcctRepo.getUserId()).thenReturn(TestUserInfoProvider().provideUserId().toString())
        `when`(mockAcctRepo.getRakutenId()).thenReturn("")
        `when`(mockAcctRepo.getIdTrackingIdentifier()).thenReturn("")
        RuntimeUtil.getUserIdentifiers(mockAcctRepo).shouldHaveSize(1)
        Mockito.verify(mockAcctRepo).getUserId()
    }

    @Test
    fun `should get be called once for get rakuten id`() {
        val mockAcctRepo = Mockito.mock(AccountRepository::class.java)

        `when`(mockAcctRepo.userInfoProvider).thenReturn(TestUserInfoProvider())
        `when`(mockAcctRepo.getUserId()).thenReturn("")
        `when`(mockAcctRepo.getRakutenId()).thenReturn(TestUserInfoProvider().provideRakutenId().toString())
        `when`(mockAcctRepo.getIdTrackingIdentifier()).thenReturn("")
        RuntimeUtil.getUserIdentifiers(mockAcctRepo).shouldHaveSize(1)
        Mockito.verify(mockAcctRepo).getRakutenId()
    }

    @Test
    fun `should get be called once for get id tracking identifier`() {
        val mockAcctRepo = Mockito.mock(AccountRepository::class.java)
        val provider = TestUserInfoProvider()
        provider.idTrackingIdentifier = TestUserInfoProvider.TEST_ID_TRACKING_IDENTIFIER
        `when`(mockAcctRepo.userInfoProvider).thenReturn(provider)
        `when`(mockAcctRepo.getUserId()).thenReturn("")
        `when`(mockAcctRepo.getRakutenId()).thenReturn("")
        `when`(mockAcctRepo
                .getIdTrackingIdentifier()).thenReturn(provider.provideIdTrackingIdentifier().toString())
        RuntimeUtil.getUserIdentifiers(mockAcctRepo).shouldHaveSize(1)
        Mockito.verify(mockAcctRepo).getRakutenId()
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
    fun `should return true for changed id tracking identifier`() {
        val infoProvider = TestUserInfoProvider()
        AccountRepository.instance().userInfoProvider = infoProvider
        // initial setting of hashed user info
        AccountRepository.instance().updateUserInfo()

        // updated
        infoProvider.idTrackingIdentifier = "tracking-id"

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

    @Test
    fun `should not crash when hashing failed`() {
        // note: this should never occur since "MD5" is supported since API 1
        val infoProvider = TestUserInfoProvider()
        AccountRepository.instance().userInfoProvider = infoProvider
        infoProvider.idTrackingIdentifier = TestUserInfoProvider.TEST_ID_TRACKING_IDENTIFIER
        // initial setting of hashed user info
        AccountRepository.instance().updateUserInfo("test").shouldBeTrue()

        AccountRepository.instance().userInfoHash shouldBeEqualTo TestUserInfoProvider.TEST_USER_ID +
                TestUserInfoProvider.TEST_RAKUTEN_ID + TestUserInfoProvider.TEST_ID_TRACKING_IDENTIFIER
    }

    @Test
    fun `should log with both have valid values`() {
        val provider = TestUserInfoProvider()
        provider.idTrackingIdentifier = TestUserInfoProvider.TEST_ID_TRACKING_IDENTIFIER
        AccountRepository.instance().userInfoProvider = provider
        try {
            AccountRepository.instance().logWarningForUserInfo("test", mockLogger)
            if (BuildConfig.DEBUG) {
                Assert.fail()
            }
        } catch (e: IllegalStateException) {
            e.localizedMessage shouldBeEqualTo AccountRepository.ID_TRACKING_ERR_MSG
        }

        Mockito.verify(mockLogger).w(captor.capture())
        captor.firstValue shouldBeInstanceOf String::class.java
    }

    @Test
    fun `should not log with only rae token have valid value`() {
        AccountRepository.instance().userInfoProvider = object : UserInfoProvider {
            override fun provideRaeToken() = "valid"

            override fun provideUserId() = "userid"

            override fun provideRakutenId() = ""
        }
        AccountRepository.instance().logWarningForUserInfo("test", mockLogger)

        Mockito.verify(mockLogger, never()).w(captor.capture())
    }

    @Test
    fun `should not log with only tracking id have valid value`() {
        AccountRepository.instance().userInfoProvider = object : UserInfoProvider {
            override fun provideRaeToken() = ""

            override fun provideUserId() = ""

            override fun provideRakutenId() = ""

            override fun provideIdTrackingIdentifier() = "valid"
        }
        AccountRepository.instance().logWarningForUserInfo("test", mockLogger)

        Mockito.verify(mockLogger, never()).w(captor.capture())
    }

    @Test
    fun `should not log with both have empty values`() {
        AccountRepository.instance().userInfoProvider = object : UserInfoProvider {
            override fun provideRaeToken() = ""

            override fun provideUserId() = ""

            override fun provideRakutenId() = ""
        }
        AccountRepository.instance().logWarningForUserInfo("test", mockLogger)

        Mockito.verify(mockLogger, never()).w(captor.capture())
    }

    @Test
    fun `should not log with both have null values`() {
        AccountRepository.instance().userInfoProvider = object : UserInfoProvider {
            override fun provideRaeToken(): String? = null

            override fun provideUserId() = ""

            override fun provideRakutenId() = ""

            override fun provideIdTrackingIdentifier(): String? = null
        }
        AccountRepository.instance().logWarningForUserInfo("test", mockLogger)

        Mockito.verify(mockLogger, never()).w(captor.capture())
    }

    @Test
    fun `should log with only rae token have valid value and not have user id`() {
        AccountRepository.instance().userInfoProvider = object : UserInfoProvider {
            override fun provideRaeToken() = "valid"

            override fun provideUserId() = ""

            override fun provideRakutenId() = ""
        }
        try {
            AccountRepository.instance().logWarningForUserInfo("test", mockLogger)
            if (BuildConfig.DEBUG) {
                Assert.fail()
            }
        } catch (e: IllegalStateException) {
            e.localizedMessage shouldBeEqualTo AccountRepository.TOKEN_USER_ERR_MSG
        }
        Mockito.verify(mockLogger).w(captor.capture())
        captor.firstValue shouldBeInstanceOf String::class.java
    }
}
