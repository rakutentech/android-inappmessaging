[inappmessaging](../index.md) / [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](./index.md)

## Package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents

### Types

| [AppStartEvent](-app-start-event/index.md) | App start logEvent for host app to use. Host app can send this event after every app launch, including first launch, or resume. It is recommended to log this event in host app's base activity's onResume().`class AppStartEvent : `[`BaseEvent`](-base-event/index.md) |
| [BaseEvent](-base-event/index.md) | BaseEvent will be the base class of all local events objects, including CustomEvent. During initialization, constructor parameters will be checked for validity if there are reasons to believe that parameter could be invalid.`abstract class BaseEvent : `[`Event`](-event/index.md) |
| [CustomEvent](-custom-event/index.md) | Logging custom event for client to use. Note: Please don't use the same attribute's name more than once because the new attribute will replace the old one.`class CustomEvent : `[`BaseEvent`](-base-event/index.md) |
| [Event](-event/index.md) | Interface of local events.`interface Event` |
| [LoginSuccessfulEvent](-login-successful-event/index.md) | Login Successful Event for host app to use. Please note: this will trigger InAppMessaging SDK to update current session data to the new user.`class LoginSuccessfulEvent : `[`BaseEvent`](-base-event/index.md) |
| [PurchaseSuccessfulEvent](-purchase-successful-event/index.md) | Purchase successful Event for host app to use.`class PurchaseSuccessfulEvent : `[`BaseEvent`](-base-event/index.md) |

