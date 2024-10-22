//[inappmessaging](../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](../index.md)/[Event](index.md)

# Event

interface [Event](index.md)

Interface of local events.

#### Inheritors

| |
|---|
| [BaseEvent](../-base-event/index.md) |

## Functions

| Name | Summary |
|---|---|
| [getAttributeMap](get-attribute-map.md) | [androidJvm]<br>@NotNull<br>abstract fun [getAttributeMap](get-attribute-map.md)(): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Attribute](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.models/-attribute/index.md)?&gt;<br>This method returns event attribute map. |
| [getEventName](get-event-name.md) | [androidJvm]<br>@NotNull<br>abstract fun [getEventName](get-event-name.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>This method returns event name. |
| [getEventType](get-event-type.md) | [androidJvm]<br>@NotNull<br>abstract fun [getEventType](get-event-type.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>This method returns event type. |
| [getRatEventMap](get-rat-event-map.md) | [androidJvm]<br>@NotNull<br>abstract fun [getRatEventMap](get-rat-event-map.md)(): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;<br>This method returns RAT event map. |
| [getTimestamp](get-timestamp.md) | [androidJvm]<br>@NotNull<br>abstract fun [getTimestamp](get-timestamp.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>This method returns event timestamp. |
| [isPersistentType](is-persistent-type.md) | [androidJvm]<br>@NotNull<br>abstract fun [isPersistentType](is-persistent-type.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This method returns event is persistent type (can be used by campaigns multiple times). If persistent type, event will not be removed in LocalEventRepository when used by a campaign. |
