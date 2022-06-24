//[inappmessaging](../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](../index.md)/[AppStartEvent](index.md)

# AppStartEvent

[androidJvm]\
class [AppStartEvent](index.md) : [BaseEvent](../-base-event/index.md)

App start logEvent for host app to use. Host app can send this event after every app launch, including first launch, or resume. It is recommended to log this event in host app's base activity's onResume().

## Constructors

| | |
|---|---|
| [AppStartEvent](-app-start-event.md) | [androidJvm]<br>fun [AppStartEvent](-app-start-event.md)() |

## Functions

| Name | Summary |
|---|---|
| [getAttributeMap](../-base-event/get-attribute-map.md) | [androidJvm]<br>@NotNull<br>open override fun [getAttributeMap](../-base-event/get-attribute-map.md)(): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Attribute](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.models/-attribute/index.md)?&gt;<br>This method is intended to be used by child classes which doesn't override this method. Returns an empty map. |
| [getEventName](../-base-event/get-event-name.md) | [androidJvm]<br>open override fun [getEventName](../-base-event/get-event-name.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>This method returns the event name. |
| [getEventType](../-base-event/get-event-type.md) | [androidJvm]<br>@NotNull<br>open override fun [getEventType](../-base-event/get-event-type.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>This method returns the event type id. |
| [getRatEventMap](../-base-event/get-rat-event-map.md) | [androidJvm]<br>@NotNull<br>open override fun [getRatEventMap](../-base-event/get-rat-event-map.md)(): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;<br>A map will be created containing event's base attributes like event name, and timestamp. |
| [getTimestamp](../-base-event/get-timestamp.md) | [androidJvm]<br>@NotNull<br>open override fun [getTimestamp](../-base-event/get-timestamp.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>This method returns the timestamp. |
| [isPersistentType](../-base-event/is-persistent-type.md) | [androidJvm]<br>@NotNull<br>open override fun [isPersistentType](../-base-event/is-persistent-type.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This method returns event is persistent type (can be used by campaigns multiple times). If persistent type, event will not be removed in LocalEventRepository when used by a campaign. |
| [setShouldNotClear](../-base-event/set-should-not-clear.md) | [androidJvm]<br>open override fun [setShouldNotClear](../-base-event/set-should-not-clear.md)(shouldNotClear: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))<br>Set to true if the event was logged when user information was updated, or before/during ping request.. |
| [shouldNotClear](../-base-event/should-not-clear.md) | [androidJvm]<br>open override fun [shouldNotClear](../-base-event/should-not-clear.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This method returns true if the event was logged when user info was updated, or before/during ping request. |