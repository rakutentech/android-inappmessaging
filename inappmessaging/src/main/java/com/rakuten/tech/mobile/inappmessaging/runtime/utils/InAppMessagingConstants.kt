package com.rakuten.tech.mobile.inappmessaging.runtime.utils

/**
 * Constants class for In App Messaging.
 */
internal class InAppMessagingConstants {
    companion object {
        const val ANDROID_PLATFORM_ENUM = 2

        // ------------------------------Exception Messages-----------------------------------------------
        const val EVENT_NAME_EMPTY_EXCEPTION = "Event name can't be empty."
        const val EVENT_NAME_TOO_LONG_EXCEPTION = "Event name can't exceed 255 characters."
        const val ARGUMENT_IS_NULL_EXCEPTION = "Argument can't be null."
        const val ARGUMENT_IS_EMPTY_EXCEPTION = "Argument value is empty."
        const val VERSION_IS_EMPTY_EXCEPTION = "Version not found in context"
        const val PACKAGE_NAME_IS_EMPTY_EXCEPTION = "Package Name not found in context"
        const val LOCALE_IS_EMPTY_EXCEPTION = "Device locale not found in context"
        const val DEVICE_ID_IS_EMPTY_EXCEPTION = "Device ID not found in context"
        const val SUBSCRIPTION_KEY_IS_EMPTY_EXCEPTION = "InAppMessaging Subscription Key was not found in context"

        // -------------------------------URL Only--------------------------------------------------------
        const val TEMPLATE_BASE_URL = "http://your.base.url/"

        // ------------------------------ RAT Broadcast KEYS ---------------------------------------------
        const val RAT_EVENT_KEY_IMPRESSION = "InAppMessaging_impressions"
        const val RAT_EVENT_KEY_IMPRESSION_VALUE = "impressions"
        const val RAT_EVENT_KEY_EVENTS = "InAppMessaging_events"
        const val RAT_EVENT_KEY_EVENT_NAME = "eventName"
        const val RAT_EVENT_KEY_EVENT_TIMESTAMP = "timestamp"
        const val RAT_EVENT_KEY_EVENT_CUSTOM_ATTRIBUTE = "customAttributes"
    }
}
