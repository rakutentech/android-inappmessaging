package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessagingTestConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Locale

/**
 * Test class for HostAppInfoRepository class
 */
@SuppressWarnings(
    "LargeClass",
)
class HostAppInfoRepositorySpec : BaseTest() {

    private val testAppInfo = HostAppInfo(
        InAppMessagingTestConstants.APP_ID, InAppMessagingTestConstants.DEVICE_ID,
        InAppMessagingTestConstants.APP_VERSION, InAppMessagingTestConstants.SUB_KEY,
        InAppMessagingTestConstants.LOCALE, isTooltipFeatureEnabled = true,
    )

    @Before
    override fun setup() {
        super.setup()
        HostAppInfoRepository.instance().clearInfo()
    }

    @After
    override fun tearDown() {
        super.tearDown()
        HostAppInfoRepository.instance().clearInfo()
    }

    @Test
    fun `should use correct data`() {
        HostAppInfoRepository.instance().addHostInfo(testAppInfo)
        HostAppInfoRepository.instance().getVersion() shouldBeEqualTo InAppMessagingTestConstants.APP_VERSION
        HostAppInfoRepository.instance().getPackageName() shouldBeEqualTo InAppMessagingTestConstants.APP_ID
        HostAppInfoRepository.instance().getDeviceLocale() shouldBeEqualTo InAppMessagingTestConstants.LOCALE.toString()
            .replace("_", "-")
            .lowercase(Locale.getDefault())
        HostAppInfoRepository.instance()
            .getSubscriptionKey() shouldBeEqualTo InAppMessagingTestConstants.SUB_KEY
        HostAppInfoRepository.instance().getDeviceId() shouldBeEqualTo InAppMessagingTestConstants.DEVICE_ID
    }

    @Test
    fun `should throw exception with correct message`() {
        try {
            HostAppInfoRepository.instance().addHostInfo(null)
            Assert.fail()
        } catch (e: InAppMessagingException) {
            e.localizedMessage shouldBeEqualTo InAppMessagingConstants.ARGUMENT_IS_NULL_EXCEPTION
        }
    }

    @Test
    fun `should throw exception for invalid version`() {
        val hostAppInfo = HostAppInfo()
        try {
            HostAppInfoRepository.instance().addHostInfo(hostAppInfo)
            Assert.fail()
        } catch (e: InAppMessagingException) {
            e.localizedMessage shouldBeEqualTo InAppMessagingConstants.VERSION_IS_EMPTY_EXCEPTION
        }
    }

    @Test(expected = InAppMessagingException::class)
    fun `should throw exception for empty package name`() {
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                version = "test",
                packageName = "",
                subscriptionKey = "test",
                deviceId = "test",
            ),
        )
    }

    @Test(expected = InAppMessagingException::class)
    fun `should throw exception for null package name`() {
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                version = "test",
                packageName = null,
                subscriptionKey = "test",
                deviceId = "test",
            ),
        )
    }

    @Test(expected = InAppMessagingException::class)
    fun `should throw exception for empty subscription key`() {
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                version = "test",
                packageName = "test",
                subscriptionKey = "",
                deviceId = "test",
            ),
        )
    }

    @Test(expected = InAppMessagingException::class)
    fun `should throw exception for null subscription key`() {
        HostAppInfoRepository.instance().addHostInfo(
            HostAppInfo(
                version = "test",
                packageName = "test",
                subscriptionKey = null,
                deviceId = "test",
            ),
        )
    }

    @Test
    fun `should not throw exception for invalid locale`() {
        val hostAppInfo = HostAppInfo(
            version = InAppMessagingTestConstants.APP_VERSION,
            packageName = InAppMessagingTestConstants.APP_ID,
            subscriptionKey = InAppMessagingTestConstants.SUB_KEY,
        )
        HostAppInfoRepository.instance().addHostInfo(hostAppInfo)
    }

    @Test
    fun `should not throw exception for empty device id`() {
        val hostAppInfo = HostAppInfo(
            version = InAppMessagingTestConstants.APP_VERSION,
            packageName = InAppMessagingTestConstants.APP_ID,
            subscriptionKey = InAppMessagingTestConstants.SUB_KEY,
            locale = InAppMessagingTestConstants.LOCALE,
            deviceId = "",
        )
        HostAppInfoRepository.instance().addHostInfo(hostAppInfo)
    }

    @Test
    fun `should not throw exception for null device id`() {
        val hostAppInfo = HostAppInfo(
            version = InAppMessagingTestConstants.APP_VERSION,
            packageName = InAppMessagingTestConstants.APP_ID,
            subscriptionKey = InAppMessagingTestConstants.SUB_KEY,
            locale = InAppMessagingTestConstants.LOCALE,
            deviceId = null,
        )
        HostAppInfoRepository.instance().addHostInfo(hostAppInfo)
    }

    @Test
    fun `should return empty or default value for unset host app info`() {
        val instance = HostAppInfoRepository.instance()
        instance.getVersion().shouldBeEmpty()
        instance.getPackageName().shouldBeEmpty()
        instance.getDeviceLocale() shouldBeEqualTo Locale.getDefault().toString().replace("_", "-")
            .lowercase(Locale.getDefault())
        instance.getSubscriptionKey().shouldBeEmpty()
        instance.getDeviceId().shouldBeEmpty()
        instance.getConfigUrl().shouldBeEmpty()
    }

    @Test
    fun `should set subscription key to empty when set to null`() {
        val mockHostInfo = mock(HostAppInfo::class.java)

        `when`(mockHostInfo.version).thenReturn("test")
        `when`(mockHostInfo.packageName).thenReturn("test")
        `when`(mockHostInfo.subscriptionKey).thenReturn("test")
        `when`(mockHostInfo.locale).thenReturn(Locale.ENGLISH)
        `when`(mockHostInfo.deviceId).thenReturn("test")

        HostAppInfoRepository.instance().addHostInfo(mockHostInfo)

        `when`(mockHostInfo.subscriptionKey).thenReturn(null)
        HostAppInfoRepository.instance().getSubscriptionKey().shouldBeEmpty()
    }

    @Test
    fun `should disable tooltip feature by default when not set`() {
        HostAppInfoRepository.instance().isTooltipFeatureEnabled().shouldBeFalse()
    }

    @Test
    fun `should disable tooltip feature by default when set to null`() {
        HostAppInfoRepository.instance().addHostInfo(testAppInfo.copy(isTooltipFeatureEnabled = null))
        HostAppInfoRepository.instance().isTooltipFeatureEnabled().shouldBeFalse()
    }

    @Test
    fun `should disable tooltip feature`() {
        HostAppInfoRepository.instance().addHostInfo(testAppInfo.copy(isTooltipFeatureEnabled = false))
        HostAppInfoRepository.instance().isTooltipFeatureEnabled().shouldBeFalse()
    }

    @Test
    fun `should enable tooltip feature`() {
        HostAppInfoRepository.instance().addHostInfo(testAppInfo.copy(isTooltipFeatureEnabled = true))
        HostAppInfoRepository.instance().isTooltipFeatureEnabled().shouldBeTrue()
    }
}
