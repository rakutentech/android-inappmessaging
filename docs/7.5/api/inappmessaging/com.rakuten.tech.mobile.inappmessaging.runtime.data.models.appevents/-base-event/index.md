//[inappmessaging](../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](../index.md)/[BaseEvent](index.md)

# BaseEvent

abstract class [BaseEvent](index.md)(@NotNulleventType: [EventType](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.enums/-event-type/index.md), @NotNulleventName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), @NotNullisPersistent: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) : [Event](../-event/index.md)

BaseEvent will be the base class of all local events objects, including CustomEvent. During initialization, constructor parameters will be checked for validity if there are reasons to believe that parameter could be invalid.

<p>Constructor will automatically store eventType, eventName, and timestamp at the moment of
object creation.<p>Event name can't be empty or null, else IllegalArgumentException will be thrown. Also,
logEvent name will be stored in upper case form.

#### Inheritors

| |
|---|
| [AppStartEvent](../-app-start-event/index.md) |
| [CustomEvent](../-custom-event/index.md) |
| [LoginSuccessfulEvent](../-login-successful-event/index.md) |
| [PurchaseSuccessfulEvent](../-purchase-successful-event/index.md) |

## Constructors

| | |
|---|---|
| [BaseEvent](-base-event.md) | [androidJvm]<br>constructor(@NotNulleventType: [EventType](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.enums/-event-type/index.md), @NotNulleventName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), @NotNullisPersistent: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [getAttributeMap](get-attribute-map.md) | [androidJvm]<br>@NotNull<br>open override fun [getAttributeMap](get-attribute-map.md)(): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Attribute](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.models/-attribute/index.md)?&gt;<br>This method is intended to be used by child classes which doesn't override this method. Returns an empty map. |
| [getEventName](get-event-name.md) | [androidJvm]<br>open override fun [getEventName](get-event-name.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>This method returns the event name. |
| [getEventType](get-event-type.md) | [androidJvm]<br>@NotNull<br>open override fun [getEventType](get-event-type.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>This method returns the event type id. |
| [getRatEventMap](get-rat-event-map.md) | [androidJvm]<br>@NotNull<br>open override fun [getRatEventMap](get-rat-event-map.md)(): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;<br>A map will be created containing event's base attributes like event name, and timestamp. |
| [getTimestamp](get-timestamp.md) | [androidJvm]<br>@NotNull<br>open override fun [getTimestamp](get-timestamp.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>This method returns the timestamp. |
| [isPersistentType](is-persistent-type.md) | [androidJvm]<br>@NotNull<br>open override fun [isPersistentType](is-persistent-type.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This method returns event is persistent type (can be used by campaigns multiple times). If persistent type, event will not be removed in LocalEventRepository when used by a campaign. |
