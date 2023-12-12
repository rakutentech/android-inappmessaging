//[inappmessaging](../../../index.md)/[com.rakuten.tech.mobile.inappmessaging.runtime](../index.md)/[InAppMessaging](index.md)/[closeTooltip](close-tooltip.md)

# closeTooltip

[androidJvm]\
abstract fun [closeTooltip](close-tooltip.md)(viewId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))

Closes a tooltip by `viewId` (`UIElement` identifier). This should be called when app needs to force-close the displayed tooltip without user action. Calling this method will not increment the campaign impression.

#### Parameters

androidJvm

| | |
|---|---|
| viewId | The ID of UI element where the tooltip is attached. |
