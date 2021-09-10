[inappmessaging](../../index.md) / [com.rakuten.tech.mobile.inappmessaging.runtime](../index.md) / [UserInfoProvider](./index.md)

# UserInfoProvider

`interface UserInfoProvider`

Interface which client app should implement in order for InAppMessaging SDK to get information
when needed.

### Functions

| [provideIdTrackingIdentifier](provide-id-tracking-identifier.md) | Only return ID tracking identifier used in the current session.`open fun provideIdTrackingIdentifier(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [provideRaeToken](provide-rae-token.md) | Only return RAE token if user is logged in. Else return null.`abstract fun provideRaeToken(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [provideRakutenId](provide-rakuten-id.md) | Only return Rakuten ID used in the current session.`abstract fun provideRakutenId(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [provideUserId](provide-user-id.md) | Only return user ID used when logging if user is logged in in the current session.`abstract fun provideUserId(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |

