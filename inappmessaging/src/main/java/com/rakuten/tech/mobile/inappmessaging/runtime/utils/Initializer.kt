package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.provider.Settings
import androidx.core.content.pm.PackageInfoCompat
import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.HostAppInfo
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.exception.InAppMessagingException
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.CacheUtil.getMemoryCacheSize
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso
import java.lang.ClassCastException
import java.lang.IllegalStateException
import java.util.Locale
import java.util.UUID
import com.squareup.picasso.OkHttp3Downloader
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * IAM SDK initialization class.
 */
internal object Initializer {

    private const val TAG = "IAM_InitWorker"
    internal const val ID_KEY = "uuid_key"
    private const val IMAGE_REQUEST_TIMEOUT_SECONDS = 20L
    private const val IMAGE_RESOURCE_TIMEOUT_SECONDS = 300L
    private const val CACHE_MAX_SIZE = 50L * 1024L * 1024L // 50 MiB

    /**
     * This method returns a string of Android Device ID. Note: In order to get device ID without Context,
     * use HostAppInfoRepo.
     */
    @SuppressLint("HardwareIds") // Suppress lint check of using device id.
    private fun getDeviceId(context: Context, sharedUtil: PreferencesUtil) =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: getUuid(context, sharedUtil)

    /**
     * This method gets device's locale based on API level.
     */
    @SuppressWarnings("Deprecation", "kotlin:S1874")
    @TargetApi(Build.VERSION_CODES.N)
    private fun getLocale(context: Context): Locale? =
        if (BuildVersionChecker.instance().isNougatAndAbove()) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }

    /**
     * This method retrieves host app's app version.
     */
    @SuppressWarnings("Deprecation")
    private fun getHostAppVersion(context: Context): String {
        val hostPackageName = getHostAppPackageName(context)
        val packageInfo: PackageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(hostPackageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                context.packageManager.getPackageInfo(hostPackageName, 0)
            }
        } catch (e: NameNotFoundException) {
            InAppLogger(TAG).debug(e.message)
            return ""
        }

        val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
        return packageInfo.versionName + "." + versionCode
    }

    /**
     * This method retrieves host app's package name.
     */
    private fun getHostAppPackageName(context: Context): String =
        if (context.packageName != null) context.packageName else ""

    @Throws(InAppMessagingException::class)
    fun initializeSdk(
        context: Context,
        subscriptionKey: String?,
        configUrl: String?,
        sharedUtil: PreferencesUtil = PreferencesUtil
    ) {
        val hostAppInfo = HostAppInfo(
            packageName = getHostAppPackageName(context), deviceId = getDeviceId(context, sharedUtil),
            version = getHostAppVersion(context), subscriptionKey = subscriptionKey, locale = getLocale(context),
            configUrl = configUrl
        )

        // Store hostAppInfo in repository.
        HostAppInfoRepository.instance().addHostInfo(hostAppInfo)

        initializePicassoInstance(context)

        InAppLogger(TAG).debug(Gson().toJson(hostAppInfo))
    }

    /**
     * This method retrieves the stored UUID or generates a random ID if not available.
     * This value is only used if Settings.Secure.ANDROID_ID returns a null value.
     */
    private fun getUuid(context: Context, sharedUtil: PreferencesUtil): String {
        if (sharedUtil.contains(context, "uuid", ID_KEY)) {
            return sharedUtil.getString(context = context, name = "uuid", key = ID_KEY, defValue = "").toString()
        }
        val id = UUID.randomUUID().toString()
        sharedUtil.putString(context = context, name = "uuid", key = ID_KEY, value = id)
        return id
    }

    private fun initializePicassoInstance(context: Context) {
        try {
            val cacheDirectory = File(context.cacheDir, "http_cache")
            val client = OkHttpClient.Builder()
                .readTimeout(IMAGE_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .callTimeout(IMAGE_RESOURCE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .cache(Cache(cacheDirectory, CACHE_MAX_SIZE))
                .build()

            val picasso = Picasso.Builder(context)
                .downloader(OkHttp3Downloader(client))
                .memoryCache(LruCache(getMemoryCacheSize()))
                .build()
            Picasso.setSingletonInstance(picasso)
        } catch (ignored: IllegalStateException) {
            // Picasso instance was already initialized
        }
    }
}
