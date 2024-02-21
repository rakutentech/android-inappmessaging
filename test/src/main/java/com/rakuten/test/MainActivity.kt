package com.rakuten.test

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent

class MainActivity : AppCompatActivity() {

    lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFragment(MainActivityFragment())
        setupAboutView()
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
        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.screen1 -> {
                    loadFragment(MainActivityFragment())
                    true
                }
                R.id.screen2 -> {
                    loadFragment(SecondActivityFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun setupAboutView() {
        findViewById<TextView>(R.id.about).text =
            "App (${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}), SDK (${com.rakuten.tech.mobile.inappmessaging.runtime.BuildConfig.VERSION_NAME})"
    }
}