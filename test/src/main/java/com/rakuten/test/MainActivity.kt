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
        setupVersionsDisplay()
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
                R.id.screen1 -> screen1TabClicked()
                R.id.screen2 -> screen2TabClicked()
            }
            true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupVersionsDisplay() {
        val aboutText = findViewById<TextView>(R.id.about)
        aboutText.text =
            "App (${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}), SDK (${com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig.VERSION_NAME})"
    }

    private fun screen1TabClicked() {
        loadFragment(MainFragment())

        listOf(
            "screen2",
            "sec_act_custom_event_click"
        ).forEach { viewId -> InAppMessaging.instance().closeTooltip(viewId) }
        InAppMessaging.instance().logEvent(CustomEvent("screen1"))
    }

    private fun screen2TabClicked() {
        loadFragment(SecondFragment())

        // As of current version, the only way to programmatically close tooltip is by supplying viewId.
        listOf(
            "screen1",
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
        ).forEach { viewId -> InAppMessaging.instance().closeTooltip(viewId) }
        InAppMessaging.instance().logEvent(CustomEvent("screen2"))
    }
}