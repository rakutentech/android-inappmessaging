package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages

import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessagePayload
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger

/**
 * Interface for an InApp campaign message.
 * All in-app messages must implement this interface.
 */
@SuppressWarnings("ComplexInterface")
internal interface Message {
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
    fun getTriggers(): List<Trigger>

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
     * Returns the number of times this message has been queued (ready for display) but was removed
     * when closeMessage API was called.
     */
    fun getNumberOfTimesClosed(): Int

    /**
     * Increments the number of times this message has been queued (ready for display) but
     * was removed when closeMessage API was called.
     */
    fun incrementTimesClosed()
}
