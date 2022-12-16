package com.rakuten.test

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging

class MainApplication : Application() {

    val provider = AppUserInfoProvider()
    lateinit var settings: IAMSettings

    override fun onCreate() {
        setupStrictMode()
        super.onCreate()
        settings = IAMSettings(this)
        InAppMessaging.configure(this)
        InAppMessaging.instance().registerPreference(provider)
    }

    /**
     * StrictMode is most commonly used to catch accidental disk or network access on the application's main thread,
     * where UI operations are received and animations take place.
     * https://developer.android.com/reference/android/os/StrictMode
     */
    private fun setupStrictMode() {
        StrictMode.setThreadPolicy(
            ThreadPolicy.Builder()
                //.detectDiskReads()
                //.detectDiskWrites()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyLog()
                .build()
        )
        // VM Policy - focused on memory leaks
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                //.detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
    }
}