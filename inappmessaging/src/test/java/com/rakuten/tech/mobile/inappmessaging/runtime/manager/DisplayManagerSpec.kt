package com.rakuten.tech.mobile.inappmessaging.runtime.manager

import android.app.Activity
import android.content.res.Resources
import android.graphics.Rect
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.testhelpers.TooltipHelper
import org.amshove.kluent.shouldBeNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.verification.VerificationMode
import org.robolectric.RobolectricTestRunner

/**
 * Test class for DisplayManager.
 */
@RunWith(RobolectricTestRunner::class)
@SuppressWarnings("LargeClass")
class DisplayManagerSpec : BaseTest() {

    private val activity = Mockito.mock(Activity::class.java)
    private val viewGroup = Mockito.mock(ViewGroup::class.java)
    private val parentViewGroup = Mockito.mock(FrameLayout::class.java)
    private val handler = Mockito.mock(Handler::class.java)

    @Before
    override fun setup() {
        InAppMessaging.initialize(ApplicationProvider.getApplicationContext(), enableTooltipFeature = true)
        InAppMessaging.instance().registerMessageDisplayActivity(activity)
        DisplayManager.instance = DisplayManager.DisplayManagerImpl(handler)
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

    @Test
    fun `should remove message from host activity with id but no tooltip`() {
        `when`(activity.findViewById<ViewGroup>(R.id.in_app_message_base_view)).thenReturn(viewGroup)
        DisplayManager.instance().removeMessage(activity, id = ID, delay = 1).shouldBeNull()
        Mockito.verify(parentViewGroup, never()).removeView(viewGroup)
    }

    @Test
    fun `should remove tooltip with id and single child`() {
        setupTooltipView()
        DisplayManager.instance().removeMessage(activity, id = ID, delay = 1).shouldBeNull()
    }

    @Test
    fun `should remove tooltip with id and null parent`() {
        setupTooltipView()
        `when`(viewGroup.parent).thenReturn(null)
        `when`(viewGroup.tag).thenReturn("invalid")
        DisplayManager.instance().removeMessage(activity, id = ID, delay = 1).shouldBeNull()
    }

    @Test
    fun `should remove tooltip with id and parent no child`() {
        setupTooltipView()
        `when`(parentViewGroup.childCount).thenReturn(0)
        `when`(viewGroup.tag).thenReturn("invalid")
        verifyViewGroup()
    }

    @Test
    fun `should not process remove tooltip when already removed`() {
        setupTooltipView()
        `when`(parentViewGroup.childCount).thenReturn(0)
        DisplayManager.instance().removeMessage(activity, id = ID, delay = 1)
        Mockito.verify(parentViewGroup, never()).removeView(any())
    }

    @Test
    fun `should remove tooltip all`() {
        setupTooltipView()
        DisplayManager.instance().removeMessage(activity, removeAll = true).shouldBeNull()
        Mockito.verify(parentViewGroup).removeView(any())
    }

    @Test
    fun `should remove tooltip all with null view`() {
        setupTooltipView()
        `when`(activity.findViewById<ViewGroup>(R.id.in_app_message_tooltip_view)).thenReturn(null)
        DisplayManager.instance().removeMessage(activity, removeAll = true).shouldBeNull()
        Mockito.verify(parentViewGroup, never()).removeView(any())
    }

    @Test
    fun `should remove tooltip all with null parent`() {
        setupTooltipView()
        `when`(viewGroup.parent).thenReturn(null)
        DisplayManager.instance().removeMessage(activity, removeAll = true).shouldBeNull()
        Mockito.verify(parentViewGroup, never()).removeView(any())
    }

    @Test
    fun `should remove tooltip all with diff id`() {
        setupTooltipView()
        `when`(viewGroup.id).thenReturn(-1)
        DisplayManager.instance().removeMessage(activity, removeAll = true).shouldBeNull()
        Mockito.verify(parentViewGroup, never()).removeView(any())
    }

    @Test
    fun `should remove tooltip all with no child`() {
        setupTooltipView()
        `when`(parentViewGroup.childCount).thenReturn(0)
        DisplayManager.instance().removeMessage(activity, removeAll = true).shouldBeNull()
        Mockito.verify(parentViewGroup, never()).removeView(any())
    }

    @Test
    fun `should remove tooltip with id and single child no delay`() {
        setupTooltipView()
        verifyViewGroup(times(1))
    }

    @Test
    fun `should remove tooltip with id and single child no delay in scroll`() {
        setupTooltipView()
        val gp = setupTooltipInScroll()
        verifyViewGroup(times(2))
        Mockito.verify(gp).removeView(any())
        Mockito.verify(gp).addView(any())
    }

    @Test
    fun `should remove tooltip with id and single child no delay in scroll edge case`() {
        setupTooltipView()
        val gp = setupTooltipInScroll()
        `when`(parentViewGroup.childCount).thenReturn(2).thenReturn(0)
        verifyViewGroup(times(1))
        Mockito.verify(gp, never()).removeView(any())
        Mockito.verify(gp, never()).addView(any())
    }

    @Test
    fun `should remove tooltip with id but no child`() {
        setupTooltipView()
        val gp = setupTooltipInScroll()
        `when`(parentViewGroup.childCount).thenReturn(0)
        verifyViewGroup()
        Mockito.verify(gp, never()).removeView(any())
        Mockito.verify(gp, never()).addView(any())
    }

    @Test
    fun `should remove message with id and single child no delay but not tooltip`() {
        setupTooltipView()
        `when`(viewGroup.id).thenReturn(0)
        verifyViewGroup()
    }

    @Test
    fun `should remove message but parent not view group`() {
        setupTooltipView()
        `when`(viewGroup.parent).thenReturn(Mockito.mock(ViewParent::class.java))
        verifyViewGroup()
    }

    @Test
    fun `should remove message with id and single child no delay but diff id`() {
        setupTooltipView()
        val mockViewGroup = Mockito.mock(ViewGroup::class.java)
        `when`(parentViewGroup.getChildAt(1)).thenReturn(mockViewGroup)
        `when`(viewGroup.tag).thenReturn("not_test")
        verifyViewGroup()
    }

    @Test
    fun `should remove message with id and single child no delay but diff id on first child`() {
        setupTooltipView()
        val mockViewGroup = Mockito.mock(ViewGroup::class.java)
        `when`(parentViewGroup.getChildAt(0)).thenReturn(mockViewGroup)
        `when`(viewGroup.tag).thenReturn("not_test")
        `when`(mockViewGroup.id).thenReturn(R.id.in_app_message_tooltip_view)
        `when`(mockViewGroup.tag).thenReturn(ID)
        val mockParent = Mockito.mock(ViewGroup::class.java)
        `when`(mockViewGroup.parent).thenReturn(mockParent)
        DisplayManager.instance().removeMessage(activity, id = ID).shouldBeNull()
        Mockito.verify(mockParent).removeView(any())

        `when`(mockViewGroup.tag).thenReturn("not_test")
        DisplayManager.instance().removeMessage(activity, id = ID).shouldBeNull()

        `when`(mockViewGroup.tag).thenReturn(ID)
        `when`(parentViewGroup.getChildAt(0)).thenReturn(null)
        DisplayManager.instance().removeMessage(activity, id = ID).shouldBeNull()
        Mockito.verify(mockParent).removeView(any())
    }

    @Test
    fun `should not remove tooltip for null activity`() {
        setupTooltipView()
        verifyRemoveTarget()
    }

    @Test
    fun `should not remove tooltip for no campaign`() {
        setupTooltipView()
        setupTargetToRemove()
        verifyRemoveTarget()
    }

    @Test
    fun `should not remove tooltip for no layout`() {
        setupTooltipView()
        setupTargetToRemove()
        verifyRemoveTarget(layout = null)
    }

    @Test
    fun `should not remove tooltip for no tooltip details`() {
        setupTooltipView()
        setupTargetToRemove(null)
        verifyRemoveTarget()
    }

    @Test
    fun `should remove tooltip for not visible target`() {
        setupTooltipView()
        setupTargetToRemove()
        verifyRemoveTarget(times(1), false)
    }

    @Test
    fun `should not remove tooltip for visible target`() {
        setupTooltipView()
        setupTargetToRemove()
        verifyRemoveTarget()
    }

    @Test
    fun `should not remove tooltip without child`() {
        setupTooltipView()
        setupTargetToRemove()
        verifyRemoveTarget()
    }

    @Test
    fun `should not remove tooltip with diff id`() {
        setupTooltipView()
        setupTargetToRemove()
        `when`(viewGroup.id).thenReturn(-1)
        verifyRemoveTarget()
    }

    @Test
    fun `should not remove tooltip for diff tag`() {
        setupTooltipView()
        setupTargetToRemove()
        `when`(parentViewGroup.getChildAt(0)).thenReturn(null)
        `when`(viewGroup.tag).thenReturn(ID).thenReturn(null)
        verifyRemoveTarget(visible = false)
        Mockito.verify(parentViewGroup).getHitRect(any())
    }

    private fun setupTargetToRemove(tooltip: Tooltip? = Tooltip("target", "top-center", "testurl", 5)) {
        val message: Message = if (tooltip == null) {
            TooltipHelper.createMessage()
        } else {
            TooltipHelper.createMessage(
                position = tooltip.position,
                imageUrl = tooltip.url,
                target = tooltip.id,
            )
        }
        CampaignRepository.instance().clearMessages()
        CampaignRepository.instance().syncWith(listOf(message), 0)
        MessageReadinessManager.instance().addMessageToQueue(message.campaignId)
    }

    @SuppressWarnings("LongMethod")
    private fun verifyRemoveTarget(
        mode: VerificationMode = never(),
        visible: Boolean = true,
        layout: ViewGroup? = parentViewGroup,
    ) {
        `when`(activity.findViewById<ViewGroup>(R.id.in_app_message_tooltip_layout)).thenReturn(layout)
        `when`(activity.packageName).thenReturn("test")
        val mockResource = Mockito.mock(Resources::class.java)
        `when`(activity.resources).thenReturn(mockResource)
        `when`(mockResource.getIdentifier(eq("target"), eq("id"), any())).thenReturn(1)
        val mockView = Mockito.mock(View::class.java)
        `when`(activity.findViewById<View>(1)).thenReturn(mockView)
        `when`(parentViewGroup.getHitRect(any())).thenAnswer {
            val rect = it.getArgument<Rect>(0)
            rect.set(100, 100, 100, 100)
        }
        `when`(mockView.getLocalVisibleRect(anyOrNull())).thenReturn(visible)
        DisplayManager.instance().removeHiddenTargets(parentViewGroup)
        Mockito.verify(parentViewGroup, mode).removeView(any())
    }

    private fun verifyViewGroup(mode: VerificationMode = never()) {
        DisplayManager.instance().removeMessage(activity, id = ID).shouldBeNull()
        Mockito.verify(parentViewGroup, mode).removeView(any())
    }

    private fun setupTooltipView() {
        `when`(activity.findViewById<ViewGroup>(R.id.in_app_message_tooltip_view)).thenReturn(viewGroup)
        `when`(viewGroup.parent).thenReturn(parentViewGroup)
        `when`(parentViewGroup.id).thenReturn(R.id.in_app_message_tooltip_layout)
        `when`(parentViewGroup.childCount).thenReturn(2)
        `when`(parentViewGroup.getChildAt(1)).thenReturn(viewGroup)
        `when`(viewGroup.id).thenReturn(R.id.in_app_message_tooltip_view)
        `when`(viewGroup.tag).thenReturn(ID)
        `when`(handler.postDelayed(any(), anyLong())).thenAnswer {
            (it.arguments[0] as Runnable).run()
            true
        }
    }

    private fun setupTooltipInScroll(): ViewGroup {
        `when`(activity.findViewById<ViewGroup>(R.id.in_app_message_tooltip_view))
            .thenReturn(viewGroup)
            .thenReturn(null)
        val mockScroll = Mockito.mock(ScrollView::class.java)
        `when`(parentViewGroup.getChildAt(0)).thenReturn(mockScroll)
        val gp = Mockito.mock(ViewGroup::class.java)
        `when`(parentViewGroup.parent).thenReturn(gp)
        return gp
    }

    companion object {
        private const val ID = "test"
    }
}
