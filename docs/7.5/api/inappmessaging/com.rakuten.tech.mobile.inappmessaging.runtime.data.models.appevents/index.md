//[inappmessaging](../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [AppStartEvent](-app-start-event/index.md) | [androidJvm]<br>class [AppStartEvent](-app-start-event/index.md) : [BaseEvent](-base-event/index.md)<br>App start logEvent for host app to use. Host app can send this event after every app launch, including first launch, or resume. It is recommended to log this event in host app's base activity's onResume(). |
| [BaseEvent](-base-event/index.md) | [androidJvm]<br>abstract class [BaseEvent](-base-event/index.md)(@NotNulleventType: [EventType](../com.rakuten.tech.mobile.inappmessaging.runtime.data.enums/-event-type/index.md), @NotNulleventName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), @NotNullisPersistent: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) : [Event](-event/index.md)<br>BaseEvent will be the base class of all local events objects, including CustomEvent. During initialization, constructor parameters will be checked for validity if there are reasons to believe that parameter could be invalid. |
| [CustomEvent](-custom-event/index.md) | [androidJvm]<br>class [CustomEvent](-custom-event/index.md)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)eventName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [BaseEvent](-base-event/index.md)<br>Logging custom event for client to use. Note: Please don't use the same attribute's name more than once because the new attribute will replace the old one. |
| [Event](-event/index.md) | [androidJvm]<br>interface [Event](-event/index.md)<br>Interface of local events. |
| [LoginSuccessfulEvent](-login-successful-event/index.md) | [androidJvm]<br>class [LoginSuccessfulEvent](-login-successful-event/index.md) : [BaseEvent](-base-event/index.md)<br>Login Successful Event for host app to use. Please note: this will trigger InAppMessaging SDK to update current session data to the new user. |
| [PurchaseSuccessfulEvent](-purchase-successful-event/index.md) | [androidJvm]<br>class [PurchaseSuccessfulEvent](-purchase-successful-event/index.md) : [BaseEvent](-base-event/index.md)<br>Purchase successful Event for host app to use. |
