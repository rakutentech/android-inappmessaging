package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import org.amshove.kluent.shouldEqual
import org.junit.Assert
import org.junit.Test

/**
 * Test class for ReadyForDisplayMessageRepository.
 */
class ReadyForDisplayMessageRepositorySpec : BaseTest() {
    @Test
    fun `should throw exception when list is null`() {
        try {
            ReadyForDisplayMessageRepository.instance().replaceAllMessages(null)
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            e.localizedMessage shouldEqual InAppMessagingConstants.ARGUMENT_IS_NULL_EXCEPTION
        }
    }
}
