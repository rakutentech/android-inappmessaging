package com.rakuten.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
    }

    override fun onResume() {
        super.onResume()
        InAppMessaging.instance().registerMessageDisplayActivity(this)
    }

    override fun onPause() {
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        super.onPause()
    }
}