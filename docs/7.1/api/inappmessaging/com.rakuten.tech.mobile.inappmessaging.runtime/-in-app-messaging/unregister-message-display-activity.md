//[inappmessaging](../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime](../index.md)/[InAppMessaging](index.md)/[unregisterMessageDisplayActivity](unregister-message-display-activity.md)

# unregisterMessageDisplayActivity

[androidJvm]\
abstract fun [unregisterMessageDisplayActivity](unregister-message-display-activity.md)()

This method unregisters the activity from InAppMessaging This method should be called in onPause() of the registered activity in order to avoid memory leaks. If there is message being displayed, it will be closed automatically.
