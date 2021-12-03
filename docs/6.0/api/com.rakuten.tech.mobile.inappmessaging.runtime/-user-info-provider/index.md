[inappmessaging](../../index.md) / [com.rakuten.tech.mobile.inappmessaging.runtime](../index.md) / [UserInfoProvider](./index.md)

# UserInfoProvider

`interface UserInfoProvider`

Interface which client app should implement in order for InAppMessaging SDK to get information
when needed.

### Functions

| [provideAccessToken](provide-access-token.md) | Only return access token if user is logged in. Else return null.`open fun provideAccessToken(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [provideIdTrackingIdentifier](provide-id-tracking-identifier.md) | Only return ID tracking identifier used in the current session.`open fun provideIdTrackingIdentifier(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [provideUserId](provide-user-id.md) | Only return user ID used when logging if user is logged in in the current session.`open fun provideUserId(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |

