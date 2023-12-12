//[inappmessaging](../../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime](../../index.md)/[InAppMessaging](../index.md)/[Companion](index.md)

# Companion

[androidJvm]\
object [Companion](index.md)

## Functions

| Name | Summary |
|---|---|
| [configure](configure.md) | [androidJvm]<br>@[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)<br>fun [configure](configure.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), subscriptionKey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, configUrl: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, enableTooltipFeature: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)? = false): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Configures the In-App Messaging SDK. |
| [instance](instance.md) | [androidJvm]<br>@[JvmStatic](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-static/index.html)<br>fun [instance](instance.md)(): [InAppMessaging](../index.md)<br>Instance of [InAppMessaging](../index.md). |

## Properties

| Name | Summary |
|---|---|
| [errorCallback](error-callback.md) | [androidJvm]<br>var [errorCallback](error-callback.md): ([Exception](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)?<br>This optional callback function is for app to receive the exception that caused failed configuration or non-fatal failures in the SDK. |
| [PUSH_PRIMER_REQ_CODE](-p-u-s-h_-p-r-i-m-e-r_-r-e-q_-c-o-d-e.md) | [androidJvm]<br>const val [PUSH_PRIMER_REQ_CODE](-p-u-s-h_-p-r-i-m-e-r_-r-e-q_-c-o-d-e.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 999<br>This is the request code that will be used when requesting push permission. |
