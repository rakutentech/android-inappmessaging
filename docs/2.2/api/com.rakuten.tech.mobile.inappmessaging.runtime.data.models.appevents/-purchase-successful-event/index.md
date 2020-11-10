[inappmessaging](../../index.md) / [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](../index.md) / [PurchaseSuccessfulEvent](./index.md)

# PurchaseSuccessfulEvent

`class PurchaseSuccessfulEvent : `[`BaseEvent`](../-base-event/index.md)

Purchase successful Event for host app to use.

### Constructors

| [&lt;init&gt;](-init-.md) | Purchase successful Event for host app to use.`PurchaseSuccessfulEvent()` |

### Properties

| [currencyCode](currency-code.md) | `var currencyCode: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [itemIdList](item-id-list.md) | `var itemIdList: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>` |
| [numberOfItems](number-of-items.md) | `var numberOfItems: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [purchaseAmountMicros](purchase-amount-micros.md) | `var purchaseAmountMicros: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |

### Functions

| [currencyCode](currency-code.md) | This method sets the currency code of this purchase successful logEvent.`fun currencyCode(currencyCode: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`PurchaseSuccessfulEvent`](./index.md) |
| [getAttributeMap](get-attribute-map.md) | This method returns a map of Attribute objects. Key: Attribute's name, Value: Attribute object.`fun getAttributeMap(): `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Attribute`](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.models/-attribute/index.md)`?>` |
| [getRatEventMap](get-rat-event-map.md) | This method returns an unmodifiable map which contains all event's attributes.`fun getRatEventMap(): `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>` |
| [itemIdList](item-id-list.md) | This method sets the list of purchased item IDs.`fun itemIdList(itemIdList: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>): `[`PurchaseSuccessfulEvent`](./index.md) |
| [numberOfItems](number-of-items.md) | This method sets the number of items in this purchase.`fun numberOfItems(numberOfItems: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`PurchaseSuccessfulEvent`](./index.md) |
| [purchaseAmountMicros](purchase-amount-micros.md) | This method sets the purchase amount in micros, $1 = 100_000. Such as $10.58 = 1058_000.`fun purchaseAmountMicros(purchaseAmountMicros: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`PurchaseSuccessfulEvent`](./index.md) |

