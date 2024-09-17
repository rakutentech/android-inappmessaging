//[inappmessaging](../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime](../index.md)/[InAppMessaging](index.md)

# InAppMessaging

[androidJvm]\
abstract class [InAppMessaging](index.md)

Main entry point for the IAM SDK. Should be accessed via [InAppMessaging.instance](-companion/instance.md).

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [closeMessage](close-message.md) | [androidJvm]<br>abstract fun [closeMessage](close-message.md)(clearQueuedCampaigns: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false)<br>Close the currently displayed message. This should be called when app needs to force-close the displayed message without user action. Calling this method will not increment the campaign impression. |
| [closeTooltip](close-tooltip.md) | [androidJvm]<br>abstract fun [closeTooltip](close-tooltip.md)(viewId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))<br>Closes a tooltip by `viewId` (`UIElement` identifier). This should be called when app needs to force-close the displayed tooltip without user action. Calling this method will not increment the campaign impression. |
| [logEvent](log-event.md) | [androidJvm]<br>abstract fun [logEvent](log-event.md)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)event: [Event](../../com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents/-event/index.md))<br>This methods logs the [event](log-event.md) which the InAppMessaging SDK checks to know the messages' triggers are satisfied, then display that message if all trigger conditions are satisfied. |
| [registerMessageDisplayActivity](register-message-display-activity.md) | [androidJvm]<br>abstract fun [registerMessageDisplayActivity](register-message-display-activity.md)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)activity: [Activity](https://developer.android.com/reference/kotlin/android/app/Activity.html))<br>This method registers [activity](register-message-display-activity.md) where message can be displayed This method should be called in onResume() of the activity to register. In order for InAppMessaging SDK to display messages, host app must pass an Activity which the host app allows the SDK to display any Messages. |
| [registerPreference](register-preference.md) | [androidJvm]<br>abstract fun [registerPreference](register-preference.md)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)userInfoProvider: [UserInfoProvider](../-user-info-provider/index.md))<br>This method registers provider containing user information [userInfoProvider](register-preference.md), like Access Token and User ID. |
| [trackPushPrimer](track-push-primer.md) | [androidJvm]<br>abstract fun [trackPushPrimer](track-push-primer.md)(permissions: [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;, grantResults: [IntArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int-array/index.html))<br>Tracks if user grants or denies the push notification via push primer message. This API only works for Android 13 and up devices. |
| [unregisterMessageDisplayActivity](unregister-message-display-activity.md) | [androidJvm]<br>abstract fun [unregisterMessageDisplayActivity](unregister-message-display-activity.md)()<br>This method unregisters the activity from InAppMessaging This method should be called in onPause() of the registered activity in order to avoid memory leaks. If there is message being displayed, it will be closed automatically. |

## Properties

| Name | Summary |
|---|---|
| [onPushPrimer](on-push-primer.md) | [androidJvm]<br>abstract var [onPushPrimer](on-push-primer.md): () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)?<br>This callback is called if a push primer button is tapped. If not set, SDK will request push permission. |
| [onVerifyContext](on-verify-context.md) | [androidJvm]<br>abstract var [onVerifyContext](on-verify-context.md): (contexts: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;, campaignTitle: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) -&gt; [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This callback is called just before showing a message of campaign that has registered contexts. Return `false` to prevent the message from displaying. |
