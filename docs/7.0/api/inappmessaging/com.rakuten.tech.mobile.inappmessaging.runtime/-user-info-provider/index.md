//[inappmessaging](../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime](../index.md)/[UserInfoProvider](index.md)

# UserInfoProvider

[androidJvm]\
interface [UserInfoProvider](index.md)

Interface which client app should implement in order for InAppMessaging SDK to get information when needed.

## Functions

| Name | Summary |
|---|---|
| [provideAccessToken](provide-access-token.md) | [androidJvm]<br>open fun [provideAccessToken](provide-access-token.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Only return access token if user is logged in. Else return null. |
| [provideIdTrackingIdentifier](provide-id-tracking-identifier.md) | [androidJvm]<br>open fun [provideIdTrackingIdentifier](provide-id-tracking-identifier.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Only return ID tracking identifier used in the current session. |
| [provideUserId](provide-user-id.md) | [androidJvm]<br>open fun [provideUserId](provide-user-id.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Only return user ID used when logging if user is logged in in the current session. |
