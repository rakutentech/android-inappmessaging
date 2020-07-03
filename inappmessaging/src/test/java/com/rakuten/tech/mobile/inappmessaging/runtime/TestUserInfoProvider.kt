package com.rakuten.tech.mobile.inappmessaging.runtime

/**
 * Test class which provides user info.
 */
class TestUserInfoProvider : UserInfoProvider {

    var userId = TEST_USER_ID
    var raeToken = TEST_USER_RAE_TOKEN
    var rakutenId = TEST_RAKUTEN_ID

    override fun provideRaeToken(): String? = raeToken
    override fun provideUserId(): String? = userId
    override fun provideRakutenId(): String? = rakutenId

    companion object {
        const val TEST_USER_RAE_TOKEN = "test_rae_token"
        const val TEST_USER_ID = "test_user_id"
        const val TEST_RAKUTEN_ID = "test_rakuten_id"
    }
}
