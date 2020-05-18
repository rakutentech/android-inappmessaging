package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType

/**
 * Login Successful Event for host app to use.
 * Please note: this will trigger InAppMessaging SDK to update current session data to the new user.
 */
class LoginSuccessfulEvent : BaseEvent(EventType.LOGIN_SUCCESSFUL, EventType.LOGIN_SUCCESSFUL.name, false)
