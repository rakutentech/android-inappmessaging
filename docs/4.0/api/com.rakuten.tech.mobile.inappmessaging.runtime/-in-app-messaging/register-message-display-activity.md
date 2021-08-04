[inappmessaging](../../index.md) / [com.rakuten.tech.mobile.inappmessaging.runtime](../index.md) / [InAppMessaging](index.md) / [registerMessageDisplayActivity](./register-message-display-activity.md)

# registerMessageDisplayActivity

`abstract fun registerMessageDisplayActivity(@NonNull activity: `[`Activity`](https://developer.android.com/reference/android/app/Activity.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

This method registers [activity](register-message-display-activity.md#com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging$registerMessageDisplayActivity(android.app.Activity)/activity) where message can be displayed
This method should be called in onResume() of the activity to register.
In order for InAppMessaging SDK to display messages, host app must pass an Activity
which the host app allows the SDK to display any Messages.

