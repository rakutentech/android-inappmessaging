package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalDisplayedMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalEventRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.LocalOptedOutMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.PingResponseMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ReadyForDisplayMessageRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.schedulers.MessageMixerPingScheduler

/**
 * SessionManager, it is the manager of session tracking. It will discard old message data, and
 * prepare new message data for the new user or session.
 */
internal object SessionManager {
    /**
     * Upon login successful or logout, old messages will be discarded, then prepare new messages for the new
     * user.
     */
    fun onSessionUpdate() {
        // clear locally stored campaigns from ping response
        PingResponseMessageRepository.instance().clearMessages()

        // clear locally stored campaigns which are ready for display
        ReadyForDisplayMessageRepository.instance().clearMessages()

        // clear locally stored campaigns which are already displayed
        LocalDisplayedMessageRepository.instance().clearMessages()

        // clear locally stored campaigns which are opted out
        LocalOptedOutMessageRepository.instance().clearMessages()

        // clear locally stored triggered events (non-persistent)
        LocalEventRepository.instance().clearNonPersistentEvents()

        MessageMixerPingScheduler.instance().pingMessageMixerService(0)
    }
}
