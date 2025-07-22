package com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger

internal enum class EventType { CRITICAL, WARNING }

internal enum class BackendApi(val alias: String) {
    CONFIG("config"),
    PING("ping"),
    DISPLAY_PERMISSION("check"),
    IMPRESSION("events"),
}

internal enum class SdkApi {
    CONFIG,
    LOG_EVENT,
    REGISTER_ACTIVITY,
    UNREGISTER_ACTIVITY,
    CLOSE_MESSAGE,
}
