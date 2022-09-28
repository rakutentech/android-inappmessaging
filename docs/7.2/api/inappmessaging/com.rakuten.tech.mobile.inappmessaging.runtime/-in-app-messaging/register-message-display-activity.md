//[inappmessaging](../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime](../index.md)/[InAppMessaging](index.md)/[registerMessageDisplayActivity](register-message-display-activity.md)

# registerMessageDisplayActivity

[androidJvm]\
abstract fun [registerMessageDisplayActivity](register-message-display-activity.md)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)activity: [Activity](https://developer.android.com/reference/kotlin/android/app/Activity.html))

This method registers [activity](register-message-display-activity.md) where message can be displayed This method should be called in onResume() of the activity to register. In order for InAppMessaging SDK to display messages, host app must pass an Activity which the host app allows the SDK to display any Messages.
