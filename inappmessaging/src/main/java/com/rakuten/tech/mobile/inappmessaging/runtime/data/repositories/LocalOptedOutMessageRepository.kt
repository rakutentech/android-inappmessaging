package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import timber.log.Timber
import java.lang.ClassCastException

/**
 * This class contains opted out messages that user chose not to see it again.
 */
@WorkerThread
internal interface LocalOptedOutMessageRepository {

    /**
     * Adding a message to the opted out message repository.
     */
    fun addMessage(message: Message)

    /**
     * This method checks if message exists in the the opted out repository.
     */
    fun hasMessage(messageCampaignId: String?): Boolean

    /**
     * Clears all message from the repository.
     * This is done during session update due to user info update.
     */
    fun clearMessages()

    companion object {
        private var instance: LocalOptedOutMessageRepository = LocalOptedOutMessageRepositoryImpl()
        @VisibleForTesting
        internal const val LOCAL_OPTED_OUT_KEY = "local_opted_out_list"
        private const val TAG = "IAM_LocalOptedOutRepo"

        fun instance() = instance
    }

    private class LocalOptedOutMessageRepositoryImpl : LocalOptedOutMessageRepository {
        private val optedOutMessages = HashSet<String?>()
        private var user = ""

        init {
            synchronized(optedOutMessages) {
                checkAndResetSet(true)
            }
        }

        override fun addMessage(message: Message) {
            synchronized(optedOutMessages) {
                checkAndResetSet()
                optedOutMessages.add(message.getCampaignId())
                saveUpdatedSet()
            }
        }

        override fun hasMessage(messageCampaignId: String?): Boolean {
            synchronized(optedOutMessages) {
                // check if caching is enabled and if there are changes in user info
                checkAndResetSet()
                return optedOutMessages.contains(messageCampaignId)
            }
        }

        override fun clearMessages() {
            synchronized(optedOutMessages) {
                optedOutMessages.clear()
                saveUpdatedSet()
            }
        }

        private fun checkAndResetSet(onLaunch: Boolean = false) {
            // check if caching is enabled and if there are changes in user info
            if (InAppMessaging.instance().isLocalCachingEnabled() &&
                    (onLaunch || user != AccountRepository.instance().userInfoHash)) {
                user = AccountRepository.instance().userInfoHash
                optedOutMessages.clear()
                // reset id list from cached using updated user info
                try {
                    InAppMessaging.instance().getSharedPref()?.getStringSet(
                            LOCAL_OPTED_OUT_KEY, HashSet<String?>())?.let {
                        optedOutMessages.addAll(it)
                    }
                } catch (ex: ClassCastException) {
                    Timber.tag(TAG).d(ex.cause, "Incorrect type for $LOCAL_OPTED_OUT_KEY data")
                }
            }
        }

        private fun saveUpdatedSet() {
            // check if caching is enabled to update persistent data
            if (InAppMessaging.instance().isLocalCachingEnabled()) {
                // save updated id list
                InAppMessaging.instance().getSharedPref()?.edit()?.putStringSet(LOCAL_OPTED_OUT_KEY,
                        optedOutMessages)?.apply() ?: Timber.tag(TAG).d("failed saving opted out data")
            }
        }
    }
}
