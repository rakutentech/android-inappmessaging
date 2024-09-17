//[inappmessaging](../../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime](../../index.md)/[InAppMessaging](../index.md)/[Companion](index.md)/[configure](configure.md)

# configure

[androidJvm]\

@[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)

fun [configure](configure.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), subscriptionKey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, configUrl: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, enableTooltipFeature: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)? = false): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

Configures the In-App Messaging SDK.

#### Return

`true` if configuration is successful, and `false` otherwise.

#### Parameters

androidJvm

| | |
|---|---|
| context | Context object. |
| subscriptionKey | An optional subscription key. Default is the value set in your app's AndroidManifest. |
| configUrl | An optional config URL. Default is the value set in your app's AndroidManifest. |
| enableTooltipFeature | An optional flag to en/dis-able tooltip campaigns feature. Disabled by default. |
