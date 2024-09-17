//[inappmessaging](../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime](../index.md)/[InAppMessaging](index.md)/[closeMessage](close-message.md)

# closeMessage

[androidJvm]\
abstract fun [closeMessage](close-message.md)(clearQueuedCampaigns: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false)

Close the currently displayed message. This should be called when app needs to force-close the displayed message without user action. Calling this method will not increment the campaign impression.

#### Parameters

androidJvm

| | |
|---|---|
| clearQueuedCampaigns | An optional parameter, when set to true (false by default), will additionally remove all campaigns that were queued to be displayed. |
