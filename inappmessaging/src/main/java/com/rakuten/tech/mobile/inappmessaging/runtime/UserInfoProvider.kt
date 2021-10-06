package com.rakuten.tech.mobile.inappmessaging.runtime

/**
 * Interface which client app should implement in order for InAppMessaging SDK to get information
 * when needed.
 */
interface UserInfoProvider {

    /**
     * Only return access token if user is logged in. Else return null.
     *
     * @return String of access token.
     */
    fun provideAccessToken(): String? = "" // optional method for Kotlin implementing class

    /**
     * Only return user ID used when logging if user is logged in in the current session.
     *
     * @return String of the user ID.
     */
    fun provideUserId(): String? = "" // optional method for Kotlin implementing class

    /**
     * Only return ID tracking identifier used in the current session.
     *
     * @return String of the ID Tracking Identifier.
     */
    fun provideIdTrackingIdentifier(): String? = "" // optional method for Kotlin implementing class
}
