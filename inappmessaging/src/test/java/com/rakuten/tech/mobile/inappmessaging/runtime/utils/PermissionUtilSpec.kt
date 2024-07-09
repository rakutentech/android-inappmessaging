package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.Manifest
import android.app.Activity
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import org.amshove.kluent.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PermissionUtilSpec {

    private val mockActivity = mock(Activity::class.java)
    private val mockActivityCompat = mockStatic(ActivityCompat::class.java)
    private val mockPreferences = mock(SharedPreferences::class.java)

    @Before
    fun setup() {
        `when`(mockActivity.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPreferences)
        `when`(mockActivity.checkSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_DENIED)

        val mockEditor = mock(Editor::class.java)
        `when`(mockPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
    }

    @After
    fun tearDown() {
        mockActivityCompat.close()
    }

    @Test
    fun `checkPermission should return Granted`() {
        `when`(mockActivity.checkSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_GRANTED)

        PermissionUtil.checkPermission(mockActivity, Manifest.permission.POST_NOTIFICATIONS) shouldBe
                CheckPermissionResult.GRANTED
    }

    @Test
    fun `checkPermission should return Can Ask`() {
        mockActivityCompat.`when`<Any> { ActivityCompat.shouldShowRequestPermissionRationale(mockActivity,
            Manifest.permission.POST_NOTIFICATIONS) }.thenReturn(false)
        `when`(mockPreferences.getBoolean(anyString(), anyBoolean())).thenReturn(true) // first time asking

        PermissionUtil.checkPermission(mockActivity, Manifest.permission.POST_NOTIFICATIONS) shouldBe
                CheckPermissionResult.CAN_ASK
    }

    @Test
    fun `checkPermission should return Previously Denied`() {
        mockActivityCompat.`when`<Any> { ActivityCompat.shouldShowRequestPermissionRationale(mockActivity,
            Manifest.permission.POST_NOTIFICATIONS) }.thenReturn(true)

        PermissionUtil.checkPermission(mockActivity, Manifest.permission.POST_NOTIFICATIONS) shouldBe
                CheckPermissionResult.PREVIOUSLY_DENIED
    }

    @Test
    fun `checkPermission should return Permanently Denied`() {
        mockActivityCompat.`when`<Any> { ActivityCompat.shouldShowRequestPermissionRationale(mockActivity,
            Manifest.permission.POST_NOTIFICATIONS) }.thenReturn(false)
        `when`(mockPreferences.getBoolean(anyString(), anyBoolean())).thenReturn(false) // not first time asking

        PermissionUtil.checkPermission(mockActivity, Manifest.permission.POST_NOTIFICATIONS) shouldBe
                CheckPermissionResult.PERMANENTLY_DENIED
    }
}