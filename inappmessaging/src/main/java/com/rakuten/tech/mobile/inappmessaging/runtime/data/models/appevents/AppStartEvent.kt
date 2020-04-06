package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType

/**
 * App start logEvent for host app to use. Host app can send this event after every app launch,
 * including first launch, or resume.
 * It is recommended to log this event in host app's base activity's onResume().
 */
class AppStartEvent : BaseEvent(EventType.APP_START, EventType.APP_START.name)
