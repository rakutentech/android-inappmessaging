package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStoreException

internal object SharePreferencesUtil {
    fun generateKey(context: Context) = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    fun createSharedPreference(context: Context, key: MasterKey, account: String): SharedPreferences {
        throw KeyStoreException()
        return EncryptedSharedPreferences.create(
                context,
                account,
                key,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
