[inappmessaging](../../index.md) / [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](../index.md) / [CustomEvent](./index.md)

# CustomEvent

`class CustomEvent : `[`BaseEvent`](../-base-event/index.md)

Logging custom event for client to use.
Note: Please don't use the same attribute's name more than once
because the new attribute will replace the old one.

### Exceptions

`IllegalArgumentException` - if [eventName](#) is an empty string or is more than 255 characters.

### Constructors

| [&lt;init&gt;](-init-.md) | Logging custom event for client to use. Note: Please don't use the same attribute's name more than once because the new attribute will replace the old one.`CustomEvent(eventName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)` |

### Functions

| [addAttribute](add-attribute.md) | This method adds custom attribute of integer type.`fun addAttribute(key: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, value: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`CustomEvent`](./index.md)<br>This method adds custom attribute of double type.`fun addAttribute(key: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, value: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`CustomEvent`](./index.md)<br>This method adds custom attribute of string type.`fun addAttribute(key: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, value: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`CustomEvent`](./index.md)<br>This method adds custom attribute of boolean type.`fun addAttribute(key: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, value: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`CustomEvent`](./index.md)<br>This method adds custom attribute of date type.`fun addAttribute(key: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, value: `[`Date`](https://docs.oracle.com/javase/6/docs/api/java/util/Date.html)`): `[`CustomEvent`](./index.md) |
| [getAttributeMap](get-attribute-map.md) | This method returns an unmodifiable map which contains all custom attributes.`fun getAttributeMap(): `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Attribute`](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.models/-attribute/index.md)`?>` |
| [getRatEventMap](get-rat-event-map.md) | This method returns an unmodifiable map which contains all event's attributes.`fun getRatEventMap(): `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>` |

