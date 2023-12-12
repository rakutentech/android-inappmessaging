//[inappmessaging](../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](../index.md)/[CustomEvent](index.md)

# CustomEvent

class [CustomEvent](index.md)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)eventName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [BaseEvent](../-base-event/index.md)

Logging custom event for client to use. Note: Please don't use the same attribute's name more than once because the new attribute will replace the old one.

#### Throws

| | |
|---|---|
| [IllegalArgumentException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-illegal-argument-exception/index.html) | if eventName is an empty string or is more than 255 characters. |

## Constructors

| | |
|---|---|
| [CustomEvent](-custom-event.md) | [androidJvm]<br>constructor(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)eventName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |

## Functions

| Name | Summary |
|---|---|
| [addAttribute](add-attribute.md) | [androidJvm]<br>@NotNull<br>fun [addAttribute](add-attribute.md)(@NotNullkey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), @NotNullvalue: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)): [CustomEvent](index.md)<br>This method adds custom attribute of date type.<br>[androidJvm]<br>@NotNull<br>fun [addAttribute](add-attribute.md)(@NotNullkey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), @NotNullvalue: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): [CustomEvent](index.md)<br>This method adds custom attribute of boolean type.<br>[androidJvm]<br>@NotNull<br>fun [addAttribute](add-attribute.md)(@NotNullkey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), @NotNullvalue: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)): [CustomEvent](index.md)<br>This method adds custom attribute of double type.<br>[androidJvm]<br>@NotNull<br>fun [addAttribute](add-attribute.md)(@NotNullkey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), @NotNullvalue: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [CustomEvent](index.md)<br>This method adds custom attribute of integer type.<br>[androidJvm]<br>@NotNull<br>fun [addAttribute](add-attribute.md)(@NotNullkey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), @NotNullvalue: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [CustomEvent](index.md)<br>This method adds custom attribute of string type. |
| [getAttributeMap](get-attribute-map.md) | [androidJvm]<br>@NotNull<br>open override fun [getAttributeMap](get-attribute-map.md)(): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Attribute](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.models/-attribute/index.md)?&gt;<br>This method returns an unmodifiable map which contains all custom attributes. |
| [getEventName](../-base-event/get-event-name.md) | [androidJvm]<br>open override fun [getEventName](../-base-event/get-event-name.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>This method returns the event name. |
| [getEventType](../-base-event/get-event-type.md) | [androidJvm]<br>@NotNull<br>open override fun [getEventType](../-base-event/get-event-type.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>This method returns the event type id. |
| [getRatEventMap](get-rat-event-map.md) | [androidJvm]<br>@NotNull<br>open override fun [getRatEventMap](get-rat-event-map.md)(): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;<br>This method returns an unmodifiable map which contains all event's attributes. |
| [getTimestamp](../-base-event/get-timestamp.md) | [androidJvm]<br>@NotNull<br>open override fun [getTimestamp](../-base-event/get-timestamp.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>This method returns the timestamp. |
| [isPersistentType](../-base-event/is-persistent-type.md) | [androidJvm]<br>@NotNull<br>open override fun [isPersistentType](../-base-event/is-persistent-type.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This method returns event is persistent type (can be used by campaigns multiple times). If persistent type, event will not be removed in LocalEventRepository when used by a campaign. |
