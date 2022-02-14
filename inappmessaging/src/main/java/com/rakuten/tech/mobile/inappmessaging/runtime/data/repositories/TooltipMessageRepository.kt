package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import android.graphics.Rect
import android.view.View
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ResourceUtils
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ViewUtil

/**
 * Contains all tooltip messages that are ready for display, but not yet displayed.
 */
internal abstract class TooltipMessageRepository : ReadyMessageRepository {

    abstract fun getCampaign(id: String): Message?

    companion object {
        private var instance: TooltipMessageRepository = TooltipMessageRepositoryImpl()
        private const val TAG = "IAM_ReadyDisplayRepo"

        fun instance(): TooltipMessageRepository = instance
    }

    private class TooltipMessageRepositoryImpl : TooltipMessageRepository() {
        // Oldest message should be displayed first, Deque offers the flexibility to add object to head or tail.
        private val messages: MutableList<Message> = ArrayList()

        override fun replaceAllMessages(messageList: List<Message>) {
            // Preventing race condition.
            synchronized(messages) {
                messages.clear()
                messages.addAll(messageList)
            }
        }

        override fun getAllMessagesCopy(): List<Message> {
            synchronized(messages) {
                val activity = InAppMessaging.instance().getRegisteredActivity() ?: return listOf()
                return ArrayList(messages.filter { message ->
                    val view = message.getTooltipConfig()?.id?.let { ResourceUtils.findViewByName<View>(activity, it) }
                    shouldRemove(view)
                })
            }
        }

        private fun shouldRemove(view: View?) = if (view != null) {
            val scrollView = ViewUtil.getScrollView(view)
            if (scrollView != null) {
                val scrollBounds = Rect()
                scrollView.getHitRect(scrollBounds)
                view.getLocalVisibleRect(scrollBounds)
            } else {
                true
            }
        } else {
            false
        }

        override fun removeMessage(campaignId: String, shouldIncrementTimesClosed: Boolean) {
            synchronized(messages) {
                messages.removeAll { message ->
                    if (message.getCampaignId() == campaignId) {
                        // messages contain unique campaigns (no two campaigns have the same campaign id)
                        if (shouldIncrementTimesClosed) {
                            PingResponseMessageRepository.instance().incrementTimesClosed(listOf(message))
                        }
                        true
                    } else {
                        false
                    }
                }
            }
        }

        override fun clearMessages(shouldIncrementTimesClosed: Boolean) {
            synchronized(messages) {
                messages.clear()
            }
        }

        override fun getCampaign(id: String): Message? = messages.firstOrNull { it.getCampaignId() == id }
    }
}
