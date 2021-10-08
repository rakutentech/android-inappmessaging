package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.app.Activity
import android.view.ViewGroup
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

/**
 * Test class for DisplayManager.
 */
class DisplayManagerSpec : BaseTest() {

    private val activity = Mockito.mock(Activity::class.java)
    private val viewGroup = Mockito.mock(ViewGroup::class.java)
    private val parentViewGroup = Mockito.mock(ViewGroup::class.java)

    @Test
    fun `should display message enqueue without exceptions`() {
        DisplayManager.instance().displayMessage()
    }

    @Test
    fun `should remove message from host activity`() {
        `when`(activity.findViewById<ViewGroup>(R.id.in_app_message_base_view)).thenReturn(viewGroup)
        `when`(viewGroup.parent).thenReturn(parentViewGroup)
        DisplayManager.instance().removeMessage(activity)
        Mockito.verify(parentViewGroup).removeView(viewGroup)
    }

    @Test
    fun `should not throw exception with null activity`() {
        DisplayManager.instance().removeMessage(null)
    }
}
