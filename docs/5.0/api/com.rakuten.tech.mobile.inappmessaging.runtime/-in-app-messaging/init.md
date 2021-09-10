[inappmessaging](../../index.md) / [com.rakuten.tech.mobile.inappmessaging.runtime](../index.md) / [InAppMessaging](index.md) / [init](./init.md)

# init

`fun init(context: `[`Context`](https://developer.android.com/reference/android/content/Context.html)`, errorCallback: ((`[`Exception`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)? = null): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

Initializes the In-App Messaging SDK. [errorCallback](init.md#com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging.Companion$init(android.content.Context, kotlin.Function1((java.lang.Exception, kotlin.Unit)))/errorCallback) is an optional callback function for
app to receive the exception that caused failed init.

**Return**
`true` if initialization is successful, and `false` otherwise.

