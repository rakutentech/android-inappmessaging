package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.RestrictTo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message

/**
 * Message Repositories interface.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal interface MessageRepository {
    /**
     * This method replaces a new list of messages into repository.
     */
    @Throws(IllegalArgumentException::class)
    fun replaceAllMessages(messageList: List<Message>?)

    /**
     * This method returns a copy of all messages in the repository.
     */
    fun getAllMessagesCopy(): List<Message>
}
