package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.app.Activity
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.imageview.ShapeableImageView
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.PositionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.*
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ImageUtilSpec
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@SuppressWarnings("LargeClass")
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class InAppMessagingTooltipViewSpec {

    private val hostAppActivity = Mockito.mock(Activity::class.java)
    private val mockMessage = Mockito.mock(Message::class.java)
    private val mockPayload = Mockito.mock(MessagePayload::class.java)
    private val mockResource = Mockito.mock(Resource::class.java)
    private val mockResources = Mockito.mock(Resources::class.java)
    private val mockTooltip = Mockito.mock(Tooltip::class.java)
    private var view: InAppMessagingTooltipView? = null
    private val mockView = Mockito.mock(View::class.java)

    @Before
    @SuppressWarnings("LongMethod")
    fun setup() {
        Mockito.`when`(hostAppActivity.layoutInflater).thenReturn(
            LayoutInflater.from(ApplicationProvider.getApplicationContext())
        )
        Mockito.`when`(mockMessage.getMessagePayload()).thenReturn(mockPayload)
        Mockito.`when`(mockMessage.getTooltipConfig()).thenReturn(mockTooltip)
        Mockito.`when`(mockPayload.backgroundColor).thenReturn("#000000")
        Mockito.`when`(mockPayload.resource).thenReturn(mockResource)
        Mockito.`when`(hostAppActivity.resources).thenReturn(mockResources)
        Mockito.`when`(hostAppActivity.packageName).thenReturn("test")
        Mockito.`when`(mockResources.getIdentifier(eq("target"), eq("id"), any())).thenReturn(1)
        Mockito.`when`(hostAppActivity.findViewById<View>(1)).thenReturn(mockView)
        val mockP = Mockito.mock(ViewGroup::class.java)
        val mockGp = Mockito.mock(ViewGroup::class.java)
        val mockRoot = Mockito.mock(ViewGroup::class.java)
        Mockito.`when`(mockView.parent).thenReturn(mockP)
        Mockito.`when`(mockP.parent).thenReturn(mockGp)
        Mockito.`when`(mockGp.parent).thenReturn(mockRoot)
        Mockito.`when`(mockView.rootView).thenReturn(mockRoot)
        Mockito.`when`(mockView.findViewById<View>(1)).thenReturn(mockView)
        view = hostAppActivity
            .layoutInflater
            .inflate(R.layout.in_app_message_tooltip, null) as InAppMessagingTooltipView
        val group = FrameLayout(ApplicationProvider.getApplicationContext())
        group.addView(view)
    }

    @Test
    fun `should set default type for invalid position value`() {
        Mockito.`when`(mockTooltip.position).thenReturn("invalid-pos")
        view?.populateViewData(mockMessage)
        view?.type shouldBeEqualTo PositionType.BOTTOM_CENTER
    }

    @Test
    fun `should not display tooltip due to empty url`() {
        Mockito.`when`(mockTooltip.position).thenReturn("top-center")
        Mockito.`when`(mockResource.imageUrl).thenReturn("")
        view?.populateViewData(mockMessage)
        view?.findViewById<ShapeableImageView>(R.id.message_tooltip_image_view)?.visibility shouldBeEqualTo View.GONE
    }

    @Test
    fun `should show image no id`() {
        showImage(PositionType.TOP_RIGHT, false)
    }

    @Test
    fun `should show image for top-right`() {
        showImage(PositionType.TOP_RIGHT)
    }

    @Test
    fun `should show image for top-center`() {
        showImage(PositionType.TOP_CENTER)
    }

    @Test
    fun `should show image for top-left`() {
        showImage(PositionType.TOP_LEFT)
    }

    @Test
    fun `should show image for bottom-right`() {
        showImage(PositionType.BOTTOM_RIGHT)
    }

    @Test
    fun `should show image for bottom-center`() {
        showImage(PositionType.BOTTOM_CENTER)
    }

    @Test
    fun `should show image for bottom-left`() {
        showImage(PositionType.BOTTOM_LEFT)
    }

    @Test
    fun `should show image for left`() {
        showImage(PositionType.LEFT)
    }

    @Test
    fun `should show image for right`() {
        showImage(PositionType.RIGHT)
    }

    @Test
    fun `should show image with mock handler`() {
        val mockHandler = Mockito.mock(Handler::class.java)
        view?.mainHandler = mockHandler
        Mockito.`when`(mockHandler.postDelayed(any(), any())).thenAnswer {
            it.getArgument<Runnable>(0).run()
            true
        }
        showImage(PositionType.RIGHT)
        Thread.sleep(1000)
        view?.findViewById<ShapeableImageView>(R.id.message_tooltip_image_view)?.visibility shouldBeEqualTo View.VISIBLE
    }

    @Test
    fun `should show image for right with null activity`() {
        showImage(PositionType.RIGHT, activity = null)
    }

    @Test
    fun `should show image for right with null view`() {
        Mockito.`when`(hostAppActivity.findViewById<View>(1)).thenReturn(null)
        showImage(PositionType.RIGHT)
    }

    @Test
    fun `should not show image for null error`() {
        Mockito.`when`(mockTooltip.position).thenReturn("top-center")
        verifyImageFetch(false, isNull = true)
        view?.type shouldBeEqualTo PositionType.TOP_CENTER
    }

    @Test
    fun `should not show image for error exception`() {
        Mockito.`when`(mockTooltip.position).thenReturn("top-center")
        verifyImageFetch(false)
        view?.type shouldBeEqualTo PositionType.TOP_CENTER
    }

    @Test
    fun `should not show image for picasso exception`() {
        Mockito.`when`(mockTooltip.position).thenReturn("top-center")
        verifyImageFetch(false, isException = true)
        view?.type shouldBeEqualTo PositionType.TOP_CENTER
    }

    private fun verifyImageFetch(
        isValid: Boolean,
        isException: Boolean = false,
        isNull: Boolean = false
    ) {
        ImageUtilSpec.IS_VALID = isValid
        ImageUtilSpec.IS_NULL = isNull
        Mockito.`when`(mockResource.imageUrl).thenReturn("any url")
        Mockito.`when`(mockPayload.headerColor).thenReturn("#")
        view?.picasso = ImageUtilSpec.setupMockPicasso(isException)
        view?.populateViewData(mockMessage)
        val imageView = view?.findViewById<ShapeableImageView>(R.id.message_tooltip_image_view)

        if (isValid) {
            imageView?.visibility shouldBeEqualTo View.INVISIBLE
        } else {
            imageView?.visibility shouldBeEqualTo View.GONE
        }
    }

    private fun showImage(type: PositionType, withId: Boolean = true, activity: Activity? = hostAppActivity) {
        if (withId) {
            Mockito.`when`(mockTooltip.id).thenReturn("target")
            InAppMessaging.initialize(ApplicationProvider.getApplicationContext())
            activity?.let { InAppMessaging.instance().registerMessageDisplayActivity(it) }
        }
        Mockito.`when`(mockTooltip.position).thenReturn(type.typeId)
        verifyImageFetch(true)
        view?.type shouldBeEqualTo type
        val image = view?.findViewById<ImageView>(R.id.message_tooltip_image_view)
        view?.isTest = true
        view?.layoutParams = ViewGroup.MarginLayoutParams(0, 0)
        image?.viewTreeObserver?.dispatchOnGlobalLayout()

        if (withId && activity != null) {
            Mockito.verify(hostAppActivity).findViewById<View>(any())
        }
    }
}
