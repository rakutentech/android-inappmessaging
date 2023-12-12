//[inappmessaging](../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](../index.md)/[LoginSuccessfulEvent](index.md)

# LoginSuccessfulEvent

[androidJvm]\
class [LoginSuccessfulEvent](index.md) : [BaseEvent](../-base-event/index.md)

Login Successful Event for host app to use. Please note: this will trigger InAppMessaging SDK to update current session data to the new user.

## Constructors

| | |
|---|---|
| [LoginSuccessfulEvent](-login-successful-event.md) | [androidJvm]<br>constructor() |

## Functions

| Name | Summary |
|---|---|
| [getAttributeMap](../-base-event/get-attribute-map.md) | [androidJvm]<br>@NotNull<br>open override fun [getAttributeMap](../-base-event/get-attribute-map.md)(): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Attribute](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.models/-attribute/index.md)?&gt;<br>This method is intended to be used by child classes which doesn't override this method. Returns an empty map. |
| [getEventName](../-base-event/get-event-name.md) | [androidJvm]<br>open override fun [getEventName](../-base-event/get-event-name.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>This method returns the event name. |
| [getEventType](../-base-event/get-event-type.md) | [androidJvm]<br>@NotNull<br>open override fun [getEventType](../-base-event/get-event-type.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>This method returns the event type id. |
| [getRatEventMap](../-base-event/get-rat-event-map.md) | [androidJvm]<br>@NotNull<br>open override fun [getRatEventMap](../-base-event/get-rat-event-map.md)(): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;<br>A map will be created containing event's base attributes like event name, and timestamp. |
| [getTimestamp](../-base-event/get-timestamp.md) | [androidJvm]<br>@NotNull<br>open override fun [getTimestamp](../-base-event/get-timestamp.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>This method returns the timestamp. |
| [isPersistentType](../-base-event/is-persistent-type.md) | [androidJvm]<br>@NotNull<br>open override fun [isPersistentType](../-base-event/is-persistent-type.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This method returns event is persistent type (can be used by campaigns multiple times). If persistent type, event will not be removed in LocalEventRepository when used by a campaign. |
