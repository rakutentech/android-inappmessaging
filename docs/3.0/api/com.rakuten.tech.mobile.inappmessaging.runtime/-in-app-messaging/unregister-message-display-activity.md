[inappmessaging](../../index.md) / [com.rakuten.tech.mobile.inappmessaging.runtime](../index.md) / [InAppMessaging](index.md) / [unregisterMessageDisplayActivity](./unregister-message-display-activity.md)

# unregisterMessageDisplayActivity

`abstract fun unregisterMessageDisplayActivity(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

This method unregisters the activity from InAppMessaging
This method should be called in onPause() of the registered activity in order to avoid memory leaks.
If there is message being displayed, it will be closed automatically.

