package com.rakuten.test

import android.content.Context
import com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil

class AppUserInfoProvider(private val context: Context) : UserInfoProvider {
    
    override fun provideAccessToken(): String {
        if (!isIdTracking()) {
            return PreferencesUtil.getString(context, SHARED_FILE, TOKEN_OR_ID_TRACKING, "").orEmpty()
        }
        return ""
    }

    override fun provideUserId(): String {
        return PreferencesUtil.getString(context, SHARED_FILE, USER_ID, "").orEmpty()
    }

    override fun provideIdTrackingIdentifier(): String {
        if (isIdTracking()) {
            return PreferencesUtil.getString(context, SHARED_FILE, TOKEN_OR_ID_TRACKING, "").orEmpty()
        }
        return ""
    }

    fun isIdTracking(): Boolean {
        return PreferencesUtil.getBoolean(context, SHARED_FILE, IS_ID_TRACKING, true)
    }

    fun saveUser(userId: String, tokenOrIdTracking: String, isIdTracking: Boolean) {
        PreferencesUtil.clear(context, SHARED_FILE)
        PreferencesUtil.putString(context, SHARED_FILE, USER_ID, userId)
        PreferencesUtil.putString(context, SHARED_FILE, TOKEN_OR_ID_TRACKING, tokenOrIdTracking)
        PreferencesUtil.putBoolean(context, SHARED_FILE, IS_ID_TRACKING, isIdTracking)
    }

    companion object {
        private const val USER_ID: String = "user-id"
        private const val SHARED_FILE: String = "user-shared-file"
        private const val TOKEN_OR_ID_TRACKING: String = "token-or-id-tracking"
        private const val IS_ID_TRACKING: String = "is-id-tracking"
    }
}