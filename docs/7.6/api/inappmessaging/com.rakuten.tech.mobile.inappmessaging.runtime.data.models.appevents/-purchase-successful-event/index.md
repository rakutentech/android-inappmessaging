//[inappmessaging](../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](../index.md)/[PurchaseSuccessfulEvent](index.md)

# PurchaseSuccessfulEvent

[androidJvm]\
class [PurchaseSuccessfulEvent](index.md) : [BaseEvent](../-base-event/index.md)

Purchase successful Event for host app to use.

## Constructors

| | |
|---|---|
| [PurchaseSuccessfulEvent](-purchase-successful-event.md) | [androidJvm]<br>constructor() |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [currencyCode](currency-code.md) | [androidJvm]<br>@NotNull<br>fun [currencyCode](currency-code.md)(@NotNullcurrencyCode: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [PurchaseSuccessfulEvent](index.md)<br>This method sets the currency code of this purchase successful logEvent. |
| [getAttributeMap](get-attribute-map.md) | [androidJvm]<br>@NotNull<br>open override fun [getAttributeMap](get-attribute-map.md)(): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Attribute](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.models/-attribute/index.md)?&gt;<br>This method returns a map of Attribute objects. Key: Attribute's name, Value: Attribute object. |
| [getEventName](../-base-event/get-event-name.md) | [androidJvm]<br>open override fun [getEventName](../-base-event/get-event-name.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>This method returns the event name. |
| [getEventType](../-base-event/get-event-type.md) | [androidJvm]<br>@NotNull<br>open override fun [getEventType](../-base-event/get-event-type.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>This method returns the event type id. |
| [getRatEventMap](get-rat-event-map.md) | [androidJvm]<br>@NotNull<br>open override fun [getRatEventMap](get-rat-event-map.md)(): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;<br>This method returns an unmodifiable map which contains all event's attributes. |
| [getTimestamp](../-base-event/get-timestamp.md) | [androidJvm]<br>@NotNull<br>open override fun [getTimestamp](../-base-event/get-timestamp.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>This method returns the timestamp. |
| [isPersistentType](../-base-event/is-persistent-type.md) | [androidJvm]<br>@NotNull<br>open override fun [isPersistentType](../-base-event/is-persistent-type.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This method returns event is persistent type (can be used by campaigns multiple times). If persistent type, event will not be removed in LocalEventRepository when used by a campaign. |
| [itemIdList](item-id-list.md) | [androidJvm]<br>@NotNull<br>fun [itemIdList](item-id-list.md)(@NotNullitemIdList: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;): [PurchaseSuccessfulEvent](index.md)<br>This method sets the list of purchased item IDs. |
| [numberOfItems](number-of-items.md) | [androidJvm]<br>@NotNull<br>fun [numberOfItems](number-of-items.md)(@NotNullnumberOfItems: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [PurchaseSuccessfulEvent](index.md)<br>This method sets the number of items in this purchase. |
| [purchaseAmountMicros](purchase-amount-micros.md) | [androidJvm]<br>@NotNull<br>fun [purchaseAmountMicros](purchase-amount-micros.md)(@NotNullpurchaseAmountMicros: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [PurchaseSuccessfulEvent](index.md)<br>This method sets the purchase amount in micros, $1 = 100_000. Such as $10.58 = 1058_000. |
