package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.content.Context
import androidx.work.WorkerParameters
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.never
import com.rakuten.tech.mobile.inappmessaging.runtime.*
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.RuntimeUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers.ImpressionWorker
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import org.amshove.kluent.*
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*

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
    fun `should get access token`() {
        AccountRepository.instance().getAccessToken() shouldBeEqualTo ""
    }

    @Test
    fun `should get user id`() {
        AccountRepository.instance().getUserId() shouldBeEqualTo ""
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
    fun `should get access token with valid provider`() {
        AccountRepository.instance().userInfoProvider = TestUserInfoProvider()
        AccountRepository.instance().getAccessToken() shouldBeEqualTo "OAuth2 " +
            TestUserInfoProvider().provideAccessToken()
    }

    @Test
    fun `should get id tracking identifier with valid provider`() {
        AccountRepository.instance().userInfoProvider = TestUserInfoProvider()
        AccountRepository.instance()
            .getIdTrackingIdentifier() shouldBeEqualTo TestUserInfoProvider().provideIdTrackingIdentifier()
    }

    @Test
    fun `should clear cache`() {
        val iam = mock(InAppMessaging::class.java)
        val ctx = mock(Context::class.java)
        val prefsUtil = mock(PreferencesUtil::class.java)

        `when`(iam.isLocalCachingEnabled()).thenReturn(true)
        `when`(iam.getHostAppContext()).thenReturn(ctx)
        `when`(prefsUtil.contains(any() ?: ctx, anyString(), anyString())).thenReturn(false)

        AccountRepository.instance().clearUserOldCacheStructure(inAppMessaging = iam, prefsUtil = prefsUtil)
        verify(prefsUtil).clear(any() ?: ctx, anyString())
    }

    @Test
    fun `should not clear cache`() {
        val iam = mock(InAppMessaging::class.java)
        val ctx = mock(Context::class.java)
        val prefsUtil = mock(PreferencesUtil::class.java)

        `when`(iam.isLocalCachingEnabled()).thenReturn(true)
        `when`(iam.getHostAppContext()).thenReturn(ctx)
        `when`(prefsUtil.contains(any() ?: ctx, anyString(), anyString())).thenReturn(true)

        AccountRepository.instance().clearUserOldCacheStructure(inAppMessaging = iam, prefsUtil = prefsUtil)
        verify(prefsUtil, never()).clear(any() ?: ctx, anyString())
    }
}

class AccountRepositoryNullSpec : AccountRepositorySpec() {

    @Test
    fun `should get access token with null values`() {
        val mockProvider = Mockito.mock(TestUserInfoProvider::class.java)
        `when`(mockProvider.provideAccessToken()).thenReturn(null)
        AccountRepository.instance().userInfoProvider = mockProvider
        AccountRepository.instance().getAccessToken() shouldBeEqualTo ""
    }

    @Test
    fun `should get user id with null values`() {
        val mockProvider = Mockito.mock(TestUserInfoProvider::class.java)
        `when`(mockProvider.provideUserId()).thenReturn(null)
        AccountRepository.instance().userInfoProvider = mockProvider
        AccountRepository.instance().getUserId() shouldBeEqualTo ""
    }

    @Test
    fun `should get id tracking identifier with null values`() {
        val mockProvider = Mockito.mock(TestUserInfoProvider::class.java)
        `when`(mockProvider.provideIdTrackingIdentifier()).thenReturn(null)
        AccountRepository.instance().userInfoProvider = mockProvider
        AccountRepository.instance().getUserId() shouldBeEqualTo ""
    }
}

class AccountRepositoryUsageSpec : AccountRepositorySpec() {

    private val mockLogger = Mockito.mock(InAppLogger::class.java)
    private val captor = argumentCaptor<String>()

    @Test
    fun `should get be called once for get access token`() {
        val mockAcctRepo = Mockito.mock(AccountRepository::class.java)

        `when`(mockAcctRepo.userInfoProvider).thenReturn(TestUserInfoProvider())
        `when`(mockAcctRepo.getAccessToken()).thenReturn(TestUserInfoProvider().provideAccessToken().toString())
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                InAppMessagingTestConstants.APP_ID,
                InAppMessagingTestConstants.DEVICE_ID, InAppMessagingTestConstants.APP_VERSION,
                InAppMessagingTestConstants.SUB_KEY, InAppMessagingTestConstants.LOCALE,
            ),
        )
        val context = Mockito.mock(Context::class.java)
        val workerParameters = Mockito.mock(WorkerParameters::class.java)
        val impressionRequest = Mockito.mock(ImpressionRequest::class.java)
        val worker = ImpressionWorker(context, workerParameters)
        worker.createReportImpressionCall("https://host/impression/", impressionRequest, mockAcctRepo)
        Mockito.verify(mockAcctRepo).getAccessToken()
    }

    @Test
    fun `should get be called once for get user id`() {
        val mockAcctRepo = Mockito.mock(AccountRepository::class.java)

        `when`(mockAcctRepo.userInfoProvider).thenReturn(TestUserInfoProvider())
        `when`(mockAcctRepo.getUserId()).thenReturn(TestUserInfoProvider().provideUserId().toString())
        `when`(mockAcctRepo.getIdTrackingIdentifier()).thenReturn("")
        RuntimeUtil.getUserIdentifiers(mockAcctRepo).shouldHaveSize(1)
        Mockito.verify(mockAcctRepo).getUserId()
    }

    @Test
    fun `should get be called once for get id tracking identifier`() {
        val mockAcctRepo = Mockito.mock(AccountRepository::class.java)
        val provider = TestUserInfoProvider()
        provider.idTrackingIdentifier = TestUserInfoProvider.TEST_ID_TRACKING_IDENTIFIER
        `when`(mockAcctRepo.userInfoProvider).thenReturn(provider)
        `when`(mockAcctRepo.getUserId()).thenReturn("")
        `when`(
            mockAcctRepo
                .getIdTrackingIdentifier(),
        ).thenReturn(provider.provideIdTrackingIdentifier().toString())
        RuntimeUtil.getUserIdentifiers(mockAcctRepo).shouldHaveSize(1)
        Mockito.verify(mockAcctRepo).getIdTrackingIdentifier()
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
    fun `should return false for changed access token`() {
        val infoProvider = TestUserInfoProvider()
        AccountRepository.instance().userInfoProvider = infoProvider
        // initial setting of hashed user info
        AccountRepository.instance().updateUserInfo()

        // updated
        infoProvider.accessToken = "access-token"

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
            TestUserInfoProvider.TEST_ID_TRACKING_IDENTIFIER
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

        Mockito.verify(mockLogger).warn(captor.capture())
        captor.firstValue shouldBeInstanceOf String::class.java
    }

    @Test
    fun `should not log with only access token have valid value`() {
        AccountRepository.instance().userInfoProvider = object : UserInfoProvider {
            override fun provideAccessToken() = "valid"

            override fun provideUserId() = "userid"
        }
        AccountRepository.instance().logWarningForUserInfo("test", mockLogger)

        Mockito.verify(mockLogger, never()).warn(captor.capture())
    }

    @Test
    fun `should not log with only tracking id have valid value`() {
        AccountRepository.instance().userInfoProvider = object : UserInfoProvider {
            override fun provideAccessToken() = ""

            override fun provideUserId() = ""

            override fun provideIdTrackingIdentifier() = "valid"
        }
        AccountRepository.instance().logWarningForUserInfo("test", mockLogger)

        Mockito.verify(mockLogger, never()).warn(captor.capture())
    }

    @Test
    fun `should not log with both have empty values`() {
        AccountRepository.instance().userInfoProvider = object : UserInfoProvider {
            override fun provideAccessToken() = ""

            override fun provideUserId() = ""
        }
        AccountRepository.instance().logWarningForUserInfo("test", mockLogger)

        Mockito.verify(mockLogger, never()).warn(captor.capture())
    }

    @Test
    fun `should not log with both have null values`() {
        AccountRepository.instance().userInfoProvider = object : UserInfoProvider {
            override fun provideAccessToken(): String? = null

            override fun provideUserId() = ""

            override fun provideIdTrackingIdentifier(): String? = null
        }
        AccountRepository.instance().logWarningForUserInfo("test", mockLogger)

        Mockito.verify(mockLogger, never()).warn(captor.capture())
    }

    @Test
    fun `should log with only access token have valid value and not have user id`() {
        AccountRepository.instance().userInfoProvider = object : UserInfoProvider {
            override fun provideAccessToken() = "valid"

            override fun provideUserId() = ""
        }
        try {
            AccountRepository.instance().logWarningForUserInfo("test", mockLogger)
            if (BuildConfig.DEBUG) {
                Assert.fail()
            }
        } catch (e: IllegalStateException) {
            e.localizedMessage shouldBeEqualTo AccountRepository.TOKEN_USER_ERR_MSG
        }
        Mockito.verify(mockLogger).warn(captor.capture())
        captor.firstValue shouldBeInstanceOf String::class.java
    }
}
