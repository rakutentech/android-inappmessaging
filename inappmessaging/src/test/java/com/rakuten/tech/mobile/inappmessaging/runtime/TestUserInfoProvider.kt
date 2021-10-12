package com.rakuten.tech.mobile.inappmessaging.runtime

/**
 * Test class which provides user info.
 */
class TestUserInfoProvider : UserInfoProvider {

    var userId = TEST_USER_ID
    var accessToken = TEST_USER_ACCESS_TOKEN
    var idTrackingIdentifier = ""

    override fun provideAccessToken(): String? = accessToken

    override fun provideUserId(): String? = userId

    override fun provideIdTrackingIdentifier(): String? = idTrackingIdentifier

    companion object {
        const val TEST_USER_ACCESS_TOKEN = "test_access_token"
        const val TEST_USER_ID = "test_user_id"
        const val TEST_ID_TRACKING_IDENTIFIER = "test_id_tracking"
    }
}
