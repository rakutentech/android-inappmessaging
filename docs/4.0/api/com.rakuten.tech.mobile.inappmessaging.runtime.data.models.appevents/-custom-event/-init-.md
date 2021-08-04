[inappmessaging](../../index.md) / [com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents](../index.md) / [CustomEvent](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`CustomEvent(@NonNull eventName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)`

Logging custom event for client to use.
Note: Please don't use the same attribute's name more than once
because the new attribute will replace the old one.

### Exceptions

`IllegalArgumentException` - if [eventName](#) is an empty string or is more than 255 characters.