package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessagingTestConstants
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingInitializationException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.amshove.kluent.shouldEqual
import org.junit.Assert
import org.junit.Test
import java.util.Locale

/**
 * Test class for HostAppInfoRepository class
 */
class HostAppInfoRepositorySpec : BaseTest() {

    @Test
    fun `should use correct data`() {
        val testAppInfo = HostAppInfo(
                InAppMessagingTestConstants.APP_ID,
                InAppMessagingTestConstants.DEVICE_ID,
                InAppMessagingTestConstants.APP_VERSION,
                InAppMessagingTestConstants.SUB_KEY,
                InAppMessagingTestConstants.LOCALE)
        HostAppInfoRepository.instance().addHostInfo(testAppInfo)
        HostAppInfoRepository.instance().getVersion() shouldEqual InAppMessagingTestConstants.APP_VERSION
        HostAppInfoRepository.instance().getPackageName() shouldEqual InAppMessagingTestConstants.APP_ID
        HostAppInfoRepository.instance().getDeviceLocale() shouldEqual InAppMessagingTestConstants.LOCALE.toString()
                .replace("_", "-").toLowerCase(Locale.getDefault())
        HostAppInfoRepository.instance()
                .getInAppMessagingSubscriptionKey() shouldEqual InAppMessagingTestConstants.SUB_KEY
        HostAppInfoRepository.instance().getDeviceId() shouldEqual InAppMessagingTestConstants.DEVICE_ID
    }

    @Test
    fun `should throw exception with correct message`() {
        try {
            HostAppInfoRepository.instance().addHostInfo(null)
            Assert.fail()
        } catch (e: InAppMessagingInitializationException) {
            e.localizedMessage shouldEqual InAppMessagingConstants.ARGUMENT_IS_NULL_EXCEPTION
        }
    }

    @Test
    fun `should throw exception for invalid version`() {
        val hostAppInfo = HostAppInfo()
        try {
            HostAppInfoRepository.instance().addHostInfo(hostAppInfo)
            Assert.fail()
        } catch (e: InAppMessagingInitializationException) {
            e.localizedMessage shouldEqual InAppMessagingConstants.VERSION_IS_EMPTY_EXCEPTION
        }
    }

    @Test
    fun `should throw exception for invalid package name`() {
        val hostAppInfo = HostAppInfo(version = InAppMessagingTestConstants.APP_VERSION)
        try {
            HostAppInfoRepository.instance().addHostInfo(hostAppInfo)
            Assert.fail()
        } catch (e: InAppMessagingInitializationException) {
            e.localizedMessage shouldEqual InAppMessagingConstants.PACKAGE_NAME_IS_EMPTY_EXCEPTION
        }
    }

    @Test
    fun `should throw exception for invalid subscription key`() {
        val hostAppInfo = HostAppInfo(
                version = InAppMessagingTestConstants.APP_VERSION,
                packageName = InAppMessagingTestConstants.APP_ID)
        try {
            HostAppInfoRepository.instance().addHostInfo(hostAppInfo)
            Assert.fail()
        } catch (e: InAppMessagingInitializationException) {
            e.localizedMessage shouldEqual InAppMessagingConstants.SUBSCRIPTION_KEY_IS_EMPTY_EXCEPTION
        }
    }

    @Test
    fun `should not throw exception for invalid locale`() {
        val hostAppInfo = HostAppInfo(
                version = InAppMessagingTestConstants.APP_VERSION,
                packageName = InAppMessagingTestConstants.APP_ID,
                subscriptionKey = InAppMessagingTestConstants.SUB_KEY)
        HostAppInfoRepository.instance().addHostInfo(hostAppInfo)
    }

    @Test
    fun `should not throw exception for invalid device id`() {
        val hostAppInfo = HostAppInfo(
                version = InAppMessagingTestConstants.APP_VERSION,
                packageName = InAppMessagingTestConstants.APP_ID,
                subscriptionKey = InAppMessagingTestConstants.SUB_KEY,
                locale = InAppMessagingTestConstants.LOCALE)
        HostAppInfoRepository.instance().addHostInfo(hostAppInfo)
    }
}
