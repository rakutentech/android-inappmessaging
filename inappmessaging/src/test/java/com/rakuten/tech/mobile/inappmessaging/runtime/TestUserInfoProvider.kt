package com.rakuten.tech.mobile.inappmessaging.runtime

/**
 * Test class which provides user info.
 */
class TestUserInfoProvider : UserInfoProvider {
    override fun provideRaeToken(): String? = TEST_USER_RAE_TOKEN

    override fun provideUserId(): String? = TEST_USER_ID

    companion object {
        const val TEST_USER_RAE_TOKEN = "test_rae_token"
        const val TEST_USER_ID = "test_user_id"
    }
}
