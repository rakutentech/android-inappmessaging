package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages

import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger

/**
 * Interface for an InApp campaign message.
 * All in-app messages must implement this interface.
 */
@SuppressWarnings("ComplexInterface", "TooManyFunctions")
internal interface Message {
    /**
     * The impressions (number of displays) left for this campaign.
     * Note that this value is obtained from cache only.
     */
    var impressionsLeft: Int?

    /**
     * The opt-out (whether not to show again) status for this campaign.
     * Note that this value is obtained from cache only.
     */
    var isOptedOut: Boolean?

    /**
     * This method returns the message type.
     */
    fun getType(): Int

    /**
     * This method returns the message campaign id.
     */
    fun getCampaignId(): String

    /**
     * This method returns the message trigger list.
     */
    fun getTriggers(): List<Trigger>?

    /**
     * This method returns the message payload.
     */
    fun getMessagePayload(): MessagePayload

    /**
     * This method checks if message is for test.
     */
    fun isTest(): Boolean

    /**
     * This method returns max impressions.
     */
    fun getMaxImpressions(): Int

    /**
     * This method sets max impressions.
     */
    fun setMaxImpression(maxImpression: Int)

    fun getContexts(): List<String>

    /**
     * Indicates Impressions should never end.
     */
    fun infiniteImpressions(): Boolean

    /**
     * Indicates no end date for the campaign.
     */
    fun hasNoEndDate(): Boolean

    /**
     * True if close 'X' button should be displayed on campaign.
     */
    fun isCampaignDismissable(): Boolean

    companion object {
        fun updatedMessage(message: Message, asOptedOut: Boolean): Message {
            message.isOptedOut = asOptedOut
            return message
        }

        fun updatedMessage(message: Message, impressionsLeft: Int): Message {
            message.impressionsLeft = impressionsLeft
            return message
        }
    }
}
