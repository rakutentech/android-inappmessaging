package com.rakuten.tech.mobile.inappmessaging.runtime.manager

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
        MessageMixerPingScheduler.instance().pingMessageMixerService(0)
    }
}
