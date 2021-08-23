package com.rakuten.tech.mobile.inappmessaging.runtime

/**
 * Interface which client app should implement in order for InAppMessaging SDK to get information
 * when needed.
 */
interface UserInfoProvider {

    /**
     * Only return RAE token if user is logged in. Else return null.
     *
     * @return String of RAE token.
     */
    fun provideRaeToken(): String?

    /**
     * Only return user ID used when logging if user is logged in in the current session.
     *
     * @return String of the user ID.
     */
    fun provideUserId(): String?

    /**
     * Only return Rakuten ID used in the current session.
     *
     * @return String of the Rakuten ID.
     */
    fun provideRakutenId(): String?

    /**
     * Only return ID tracking identifier used in the current session.
     *
     * @return String of the ID Tracking Identifier.
     */
    fun provideIdTrackingIdentifier(): String?
}
