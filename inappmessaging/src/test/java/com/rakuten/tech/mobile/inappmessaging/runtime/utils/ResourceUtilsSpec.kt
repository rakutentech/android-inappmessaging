package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.app.Activity
import android.content.res.Resources
import android.os.Build
import android.view.View
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import org.amshove.kluent.shouldBeNull
import org.junit.Test
import org.mockito.Mockito
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.M])
class ResourceUtilsSpec {

    @Test
    fun `should return null for invalid id`() {
        val mockActivity = Mockito.mock(Activity::class.java)
        val mockResource = Mockito.mock(Resources::class.java)
        Mockito.`when`(mockActivity.packageName).thenReturn("test")
        Mockito.`when`(mockActivity.resources).thenReturn(mockResource)
        Mockito.`when`(mockResource.getIdentifier(eq("target"), eq("id"), any())).thenReturn(0)
        ResourceUtils.findViewByName<View>(mockActivity, "target").shouldBeNull()
    }
}
