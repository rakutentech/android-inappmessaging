package com.rakuten.tech.mobile.inappmessaging.runtime.test_helpers

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Tooltip
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.ValidTestMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.view.InAppMessagingTooltipView
import org.mockito.Mockito

internal object TooltipHelper {
    fun createMessage(
        position: String = "bottom-center",
        imageUrl: String? = "valid url"): ValidTestMessage {

        return ValidTestMessage(
            type = InAppMessageType.TOOLTIP.typeId,
            tooltip = Tooltip("ui-element", position, "testurl"),
            imageUrl = imageUrl
        )
    }

    fun loadImage(tv: InAppMessagingTooltipView, position: String = "bottom-center") {
        // Setup Picasso
        tv.picasso = MockPicasso.init(MockPicassoReturnType.CALLBACK_SUCCESS)
        tv.isTest = true
        val mockHandler = Mockito.mock(Handler::class.java)
        tv.mainHandler = mockHandler
        Mockito.`when`(mockHandler.postDelayed(any(), any())).thenAnswer {
            it.getArgument<Runnable>(0).run()
            true
        }

        tv.populateViewData(createMessage(position))
        // Simulate image loaded
        tv.findViewById<ImageView>(R.id.message_tooltip_image_view)?.viewTreeObserver?.dispatchOnGlobalLayout()
        // Handler delay
        Thread.sleep(1000)
    }
}