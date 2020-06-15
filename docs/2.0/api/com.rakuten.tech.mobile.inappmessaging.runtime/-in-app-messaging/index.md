[inappmessaging](../../index.md) / [com.rakuten.tech.mobile.inappmessaging.runtime](../index.md) / [InAppMessaging](./index.md)

# InAppMessaging

`abstract class InAppMessaging`

Main entry point for the IAM SDK.
Should be accessed via [InAppMessaging.instance](instance.md).

### Functions

| [logEvent](log-event.md) | This methods logs the [event](log-event.md#com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging$logEvent(com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event)/event) which the InAppMessaging SDK checks to know the messages' triggers are satisfied, then display that message if all trigger conditions are satisfied.`abstract fun logEvent(event: `[`Event`](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents/-event/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [registerMessageDisplayActivity](register-message-display-activity.md) | This method registers [activity](register-message-display-activity.md#com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging$registerMessageDisplayActivity(android.app.Activity)/activity) where message can be displayed This method should be called in onResume() of the activity to register. In order for InAppMessaging SDK to display messages, host app must pass an Activity which the host app allows the SDK to display any Messages.`abstract fun registerMessageDisplayActivity(activity: `[`Activity`](https://developer.android.com/reference/android/app/Activity.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [registerPreference](register-preference.md) | This method registers provider containing user information [userInfoProvider](register-preference.md#com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging$registerPreference(com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider)/userInfoProvider), like RAE Token and Uer ID.`abstract fun registerPreference(userInfoProvider: `[`UserInfoProvider`](../-user-info-provider/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [unregisterMessageDisplayActivity](unregister-message-display-activity.md) | This method unregisters the activity from InAppMessaging This method should be called in onPause() of the registered activity in order to avoid memory leaks. If there is message being displayed, it will be closed automatically.`abstract fun unregisterMessageDisplayActivity(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [updateSession](update-session.md) | This methods updates the host app's session. This allows InAppMessaging to update the locally stored messages which can be dependent on user information.`abstract fun updateSession(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Companion Object Functions

| [instance](instance.md) | Instance of [InAppMessaging](./index.md).`fun instance(): `[`InAppMessaging`](./index.md) |

