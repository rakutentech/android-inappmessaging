package com.rakuten.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.CustomEvent

class SecondFragment : Fragment(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.sec_act_custom_event_click).setOnClickListener(this)
        view.findViewById<Button>(R.id.sec_act_custom_event_click).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sec_act_custom_event_click -> InAppMessaging.instance().logEvent(
                CustomEvent("sec_act_click_event").addAttribute("foo", 2))
        }
    }
}