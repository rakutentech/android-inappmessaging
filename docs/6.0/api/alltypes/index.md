

### All Types

|

##### [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent](../com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents/-app-start-event/index.md)

App start logEvent for host app to use. Host app can send this event after every app launch,
including first launch, or resume.
It is recommended to log this event in host app's base activity's onResume().


|

##### [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Attribute](../com.rakuten.tech.mobile.inappmessaging.runtime.data.models/-attribute/index.md)

This class represents InAppMessaging's custom event attribute.


|

##### [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.BaseEvent](../com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents/-base-event/index.md)

BaseEvent will be the base class of all local events objects, including CustomEvent. During
initialization, constructor parameters will be checked for validity if there are reasons to
believe that parameter could be invalid.


|

##### [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.CustomEvent](../com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents/-custom-event/index.md)

Logging custom event for client to use.
Note: Please don't use the same attribute's name more than once
because the new attribute will replace the old one.


|

##### [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event](../com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents/-event/index.md)

Interface of local events.


|

##### [com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType](../com.rakuten.tech.mobile.inappmessaging.runtime.data.enums/-event-type/index.md)

Local events type according to type ID.


|

##### [com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging](../com.rakuten.tech.mobile.inappmessaging.runtime/-in-app-messaging/index.md)

Main entry point for the IAM SDK.
Should be accessed via [InAppMessaging.instance](../com.rakuten.tech.mobile.inappmessaging.runtime/-in-app-messaging/instance.md).


|

##### [com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException](../com.rakuten.tech.mobile.inappmessaging.runtime.exception/-in-app-messaging-exception/index.md)

Custom exception of In-App Messaging.


|

##### [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.LoginSuccessfulEvent](../com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents/-login-successful-event/index.md)

Login Successful Event for host app to use.
Please note: this will trigger InAppMessaging SDK to update current session data to the new user.


|

##### [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent](../com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents/-purchase-successful-event/index.md)

Purchase successful Event for host app to use.


|

##### [com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider](../com.rakuten.tech.mobile.inappmessaging.runtime/-user-info-provider/index.md)

Interface which client app should implement in order for InAppMessaging SDK to get information
when needed.


|

##### [com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ValueType](../com.rakuten.tech.mobile.inappmessaging.runtime.data.enums/-value-type/index.md)

Data value's type supported by InAppMessaging's custom events.


