[inappmessaging](../../index.md) / [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](../index.md) / [Event](./index.md)

# Event

`interface Event`

Interface of local events.

### Functions

| [getAttributeMap](get-attribute-map.md) | This method returns event attribute map.`abstract fun getAttributeMap(): `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Attribute`](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.models/-attribute/index.md)`?>` |
| [getEventName](get-event-name.md) | This method returns event name.`abstract fun getEventName(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [getEventType](get-event-type.md) | This method returns event type.`abstract fun getEventType(): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [getRatEventMap](get-rat-event-map.md) | This method returns RAT event map.`abstract fun getRatEventMap(): `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>` |
| [getTimestamp](get-timestamp.md) | This method returns event timestamp.`abstract fun getTimestamp(): `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |
| [isPersistentType](is-persistent-type.md) | This method returns event is persistent type (can be used by campaigns multiple times). If persistent type, event will not be removed in LocalEventRepository when used by a campaign.`abstract fun isPersistentType(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [setShouldNotClear](set-should-not-clear.md) | Set to true if the event was logged when user information was updated, or before/during ping request..`abstract fun setShouldNotClear(shouldNotClear: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [shouldNotClear](should-not-clear.md) | This method returns true if the event was logged when user info was updated, or before/during ping request.`abstract fun shouldNotClear(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

### Inheritors

| [BaseEvent](../-base-event/index.md) | BaseEvent will be the base class of all local events objects, including CustomEvent. During initialization, constructor parameters will be checked for validity if there are reasons to believe that parameter could be invalid.`abstract class BaseEvent : `[`Event`](./index.md) |

