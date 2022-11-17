package com.rakuten.test

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.CustomEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.view.CustomOnTouchListener

class SecondActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        findViewById<ScrollView>(R.id.scrollView)?.setOnTouchListener(object : CustomOnTouchListener() {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                super.onTouch(v, event)
                Log.e("IAM", "scroll")
                return false
            }
        })

        findViewById<Button>(R.id.sec_act_custom_event_click).setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        InAppMessaging.instance().registerMessageDisplayActivity(this)
    }

    override fun onPause() {
        InAppMessaging.instance().unregisterMessageDisplayActivity()
        super.onPause()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sec_act_custom_event_click -> InAppMessaging.instance().logEvent(
                CustomEvent("sec_act_click_event").addAttribute("foo", 2))
        }
    }
}