package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber
import java.io.File
import java.security.KeyStore

internal object SharedPreferencesUtil {

    @SuppressWarnings("TooGenericExceptionCaught", "ReturnCount", "LongMethod", "NestedBlockDepth")
    fun createSharedPreference(
        context: Context,
        account: String,
        key: MasterKey? = null,
        shouldRetry: Boolean = true,
        timber: Timber.Tree = Timber.tag(TAG)
    ): SharedPreferences {
        if (isEncryptedEnabled(context)) {
            try {
                val sharedPref = EncryptedSharedPreferences.create(
                        context,
                        account,
                        key ?: MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                setEncryptedEnabled(context, true)
                return sharedPref
            } catch (e: Exception) {
                timber.e(e)
                if (shouldRetry) {
                    try {
                        removeMasterKey(context)
                        return createSharedPreference(context, account, shouldRetry = false)
                    } catch (ex: Exception) {
                        timber.e(ex)
                    }
                }
            }
        }
        return getNormalSharedPreference(context, account)
    }

    private fun removeMasterKey(context: Context) {
        KeyStore.getInstance("AndroidKeyStore").run {
            load(null)
            deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        }

        context.filesDir.parent?.let { parentPath ->
            File(parentPath + File.separator + "shared_prefs" +
                    File.separator + MasterKey.DEFAULT_MASTER_KEY_ALIAS + ".xml").delete()
        }
    }

    private fun getNormalSharedPreference(context: Context, account: String): SharedPreferences {
        setEncryptedEnabled(context, false)
        return context.getSharedPreferences(PREFS + "_$account", Context.MODE_PRIVATE)
    }

    private fun isEncryptedEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        // default is true to try using encrypted shared preferences first
        return prefs.getBoolean(IS_ENCRYPTED_KEY, true)
    }

    private fun setEncryptedEnabled(context: Context, isEnabled: Boolean) {
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        // default is true to try using encrypted shared preferences first
        editor.putBoolean(IS_ENCRYPTED_KEY, isEnabled).apply()
    }

    private const val PREFS = "internal_shared_prefs"
    private const val IS_ENCRYPTED_KEY = "prefs_key_encrypted"
    private const val TAG = "IAM_SharedPrefs"
}
