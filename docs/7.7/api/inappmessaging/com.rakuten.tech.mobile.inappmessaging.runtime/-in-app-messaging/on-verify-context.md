//[inappmessaging](../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime](../index.md)/[InAppMessaging](index.md)/[onVerifyContext](on-verify-context.md)

# onVerifyContext

[androidJvm]\
abstract var [onVerifyContext](on-verify-context.md): (contexts: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;, campaignTitle: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) -&gt; [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

This callback is called just before showing a message of campaign that has registered contexts. Return `false` to prevent the message from displaying.
