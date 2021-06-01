package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import androidx.security.crypto.MasterKey
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.times
import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import timber.log.Timber
import java.io.File
import java.lang.NullPointerException

@RunWith(RobolectricTestRunner::class)
class SharedPreferencesUtilSpec : BaseTest() {
    companion object {
        private const val ACCOUNT = "test"
        private const val PREFS = "internal_shared_prefs"
        private const val ACCOUNT_PREFS = "internal_shared_prefs_$ACCOUNT"
        private const val IS_ENCRYPTED_KEY = "prefs_key_encrypted"
    }
    private val mockMasterKey = Mockito.mock(MasterKey::class.java)
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val mockContext = Mockito.mock(Context::class.java)
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val accountPrefs = context.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE)

    @Before
    fun setup() {
        When calling mockContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE) itReturns prefs
        When calling mockContext.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE) itReturns accountPrefs
        When calling mockContext.applicationContext itThrows NullPointerException()
        prefs.edit().clear().apply()
    }

    @Test
    fun `should set encrypted flag in pref to false`() {
        val file = Mockito.mock(File::class.java)
        When calling mockContext.filesDir itReturns file
        When calling file.parent itReturns "samplePath"
        SharedPreferencesUtil.createSharedPreference(mockContext, ACCOUNT, mockMasterKey)
        prefs.contains(IS_ENCRYPTED_KEY).shouldBeTrue()
        prefs.getBoolean(IS_ENCRYPTED_KEY, true).shouldBeFalse()
    }

    @Test
    fun `should set encrypted flag in pref to false with failed removing of master`() {
        val mockTimber = Mockito.mock(Timber.Tree::class.java)
        SharedPreferencesUtil.createSharedPreference(mockContext, ACCOUNT, mockMasterKey, timber = mockTimber)
        prefs.contains(IS_ENCRYPTED_KEY).shouldBeTrue()
        prefs.getBoolean(IS_ENCRYPTED_KEY, true).shouldBeFalse()
        Mockito.verify(mockTimber, times(2)).e(any(Throwable::class))
    }

    @Test
    fun `should use normal shared pref is encrypted flag is false`() {
        prefs.edit().putBoolean(IS_ENCRYPTED_KEY, false).apply()
        accountPrefs.edit().putString("KEY_TEST", "normal_pref").apply()

        val createdPref = SharedPreferencesUtil.createSharedPreference(context, ACCOUNT, mockMasterKey)
        createdPref.contains("KEY_TEST").shouldBeTrue()
        createdPref.getString("KEY_TEST", "") shouldBeEqualTo "normal_pref"
    }
}
