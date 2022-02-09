[inappmessaging](../../index.md) / [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](../index.md) / [Event](index.md) / [isPersistentType](./is-persistent-type.md)

# isPersistentType

`@NotNull abstract fun isPersistentType(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

This method returns event is persistent type (can be used by campaigns multiple times).
If persistent type, event will not be removed in LocalEventRepository when used by a campaign.

