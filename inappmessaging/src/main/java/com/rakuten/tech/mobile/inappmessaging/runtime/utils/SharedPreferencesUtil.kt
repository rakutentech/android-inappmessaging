package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.content.SharedPreferences

internal object SharedPreferencesUtil {

    fun createSharedPreference(context: Context, account: String): SharedPreferences =
            context.getSharedPreferences(PREFS + "_$account", Context.MODE_PRIVATE)

    private const val PREFS = "internal_shared_prefs"
}
