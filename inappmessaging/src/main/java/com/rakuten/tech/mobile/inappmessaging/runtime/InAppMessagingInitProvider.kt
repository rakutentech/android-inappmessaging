package com.rakuten.tech.mobile.inappmessaging.runtime

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import androidx.work.Configuration
import androidx.work.WorkManager
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingInitializationException
import com.rakuten.tech.mobile.manifestconfig.annotations.ManifestConfig
import com.rakuten.tech.mobile.manifestconfig.annotations.MetaData

/**
 * Fake ContentProvider that initializes the InApp Messaging SDK.
 */
internal class InAppMessagingInitProvider : ContentProvider() {

    @ManifestConfig
    internal interface App {

        /**
         * Subscription Key from the InAppMessaging Dashboard.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.inappmessaging.subscriptionkey")
        fun subscriptionKey(): String?

        /**
         * Config URL for the IAM API.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.inappmessaging.configurl")
        fun configUrl(): String?

        /**
         * Flag to enable/disable debug logging.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.inappmessaging.debugging", value = "false")
        fun isDebugging(): Boolean
    }

    @Throws(InAppMessagingInitializationException::class)
    override fun onCreate(): Boolean {
        val context = context ?: return false
        val manifestConfig = AppManifestConfig(context)

        // Special handling of WorkManager initialization for Android 11
        val config = Configuration.Builder().build()
        WorkManager.initialize(context, config)

        InAppMessaging.init(context, manifestConfig.subscriptionKey(), manifestConfig.configUrl(),
                isDebugLogging = manifestConfig.isDebugging())
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
}
