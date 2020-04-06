package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.app.Activity
import android.view.ViewGroup
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

/**
 * Test class for DisplayManager.
 */
class DisplayManagerSpec : BaseTest() {

    private var activity = Mockito.mock(Activity::class.java)
    private var viewGroup = Mockito.mock(ViewGroup::class.java)
    private var parentViewGroup = Mockito.mock(ViewGroup::class.java)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should display message enqueue without exceptions`() {
        DisplayManager.instance().displayMessage()
    }

    @Test
    fun `should remove message from host activity`() {
        Mockito.`when`<Any?>(activity!!.findViewById(R.id.in_app_message_base_view)).thenReturn(viewGroup)
        When calling viewGroup!!.parent itReturns parentViewGroup
        DisplayManager.instance().removeMessage(activity)
        Mockito.verify(parentViewGroup)!!.removeView(viewGroup)
    }

    @Test
    fun `should not throw exception with null activity`() {
        DisplayManager.instance().removeMessage(null)
    }
}
