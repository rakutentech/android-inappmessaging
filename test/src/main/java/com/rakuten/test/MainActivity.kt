package com.rakuten.test

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent

class MainActivity : AppCompatActivity() {
    var leakActivity: Activity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        leakActivity = this
        SingletonManager.getInstance(this)
        setContentView(R.layout.activity_main)
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
}

internal class SingletonManager private constructor(private val context: Context) {

    companion object {
        private var instance: SingletonManager? = null

        fun getInstance(context: Context): SingletonManager {
            if (instance == null) {
                instance = SingletonManager(context);
            }
            return instance!!
        }
    }
}