package com.rakuten.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.CustomEvent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFragment(MainFragment())
        setupVersionDisplay()
        setupBottomNav()
    }

    override fun onStart() {
        super.onStart()
        InAppMessaging.instance().logEvent(AppStartEvent())
    }

    override fun onResume() {
        super.onResume()
        InAppMessaging.instance().registerMessageDisplayActivity(this)
    }

    override fun onPause() {
        super.onPause()
        InAppMessaging.instance().unregisterMessageDisplayActivity()
    }

    private  fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content,fragment)
        transaction.commit()
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.screen1 -> {
                    loadFragment(MainFragment())
                    closeScreenTooltipsIfAny(R.id.screen2)
                    InAppMessaging.instance().logEvent(CustomEvent("screen1"))
                }
                R.id.screen2 -> {
                    loadFragment(SecondFragment())
                    closeScreenTooltipsIfAny(R.id.screen1)
                    InAppMessaging.instance().logEvent(CustomEvent("screen2"))
                }
            }
            true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupVersionDisplay() {
        val aboutText = findViewById<TextView>(R.id.about)
        aboutText.text = "App Version: ${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}"
    }

    private fun closeScreenTooltipsIfAny(screenId: Int) {
        // The list of viewIds to close. Closing currently displayed tooltip without supplying viewId is not supported
        // in the current version.
        var viewIds = listOf<String>()

        when(screenId) {
            R.id.screen1 -> {
                viewIds = listOf(
                    "set_contexts",
                    "close_message",
                    "close_tooltip",
                    "change_user",
                    "launch_successful",
                    "login_successful",
                    "purchase_successful",
                    "purchase_successful_twice",
                    "login_successful_twice",
                    "login_purchase_successful",
                    "custom_event",
                    "reconfigure"
                )
            }
            R.id.screen2 -> {
                viewIds = listOf(
                    "sec_act_custom_event_click"
                )
            }
        }
        viewIds.forEach { viewId -> InAppMessaging.instance().closeTooltip(viewId) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when(requestCode) {
            InAppMessaging.PUSH_PRIMER_REQ_CODE -> {
                InAppMessaging.instance().trackPushPrimer(permissions, grantResults)
            }
        }
    }
}