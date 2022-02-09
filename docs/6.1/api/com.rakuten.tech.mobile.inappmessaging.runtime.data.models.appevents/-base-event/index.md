[inappmessaging](../../index.md) / [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](../index.md) / [BaseEvent](./index.md)

# BaseEvent

`abstract class BaseEvent : `[`Event`](../-event/index.md)

BaseEvent will be the base class of all local events objects, including CustomEvent. During
initialization, constructor parameters will be checked for validity if there are reasons to
believe that parameter could be invalid.

### Constructors

| [&lt;init&gt;](-init-.md) | BaseEvent will be the base class of all local events objects, including CustomEvent. During initialization, constructor parameters will be checked for validity if there are reasons to believe that parameter could be invalid.`BaseEvent(eventType: `[`EventType`](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.enums/-event-type/index.md)`, eventName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, isPersistent: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`)` |

### Functions

| [getAttributeMap](get-attribute-map.md) | This method is intended to be used by child classes which doesn't override this method. Returns an empty map.`open fun getAttributeMap(): `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Attribute`](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.models/-attribute/index.md)`?>` |
| [getEventName](get-event-name.md) | This method returns the event name.`open fun getEventName(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [getEventType](get-event-type.md) | This method returns the event type id.`open fun getEventType(): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [getRatEventMap](get-rat-event-map.md) | A map will be created containing event's base attributes like event name, and timestamp.`open fun getRatEventMap(): `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>` |
| [getTimestamp](get-timestamp.md) | This method returns the timestamp.`open fun getTimestamp(): `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |
| [isPersistentType](is-persistent-type.md) | This method returns event is persistent type (can be used by campaigns multiple times). If persistent type, event will not be removed in LocalEventRepository when used by a campaign.`open fun isPersistentType(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [setShouldNotClear](set-should-not-clear.md) | Set to true if the event was logged when user information was updated, or before/during ping request..`open fun setShouldNotClear(shouldNotClear: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [shouldNotClear](should-not-clear.md) | This method returns true if the event was logged when user info was updated, or before/during ping request.`open fun shouldNotClear(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

### Inheritors

| [AppStartEvent](../-app-start-event/index.md) | App start logEvent for host app to use. Host app can send this event after every app launch, including first launch, or resume. It is recommended to log this event in host app's base activity's onResume().`class AppStartEvent : `[`BaseEvent`](./index.md) |
| [CustomEvent](../-custom-event/index.md) | Logging custom event for client to use. Note: Please don't use the same attribute's name more than once because the new attribute will replace the old one.`class CustomEvent : `[`BaseEvent`](./index.md) |
| [LoginSuccessfulEvent](../-login-successful-event/index.md) | Login Successful Event for host app to use. Please note: this will trigger InAppMessaging SDK to update current session data to the new user.`class LoginSuccessfulEvent : `[`BaseEvent`](./index.md) |
| [PurchaseSuccessfulEvent](../-purchase-successful-event/index.md) | Purchase successful Event for host app to use.`class PurchaseSuccessfulEvent : `[`BaseEvent`](./index.md) |

