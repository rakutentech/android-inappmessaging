package com.rakuten.test

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.CustomEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.LoginSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import kotlinx.android.synthetic.main.fragment_main.*

class MainActivityFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val launchSecondActivity = view.findViewById<Button>(R.id.launch_second_activity)
        val launchSuccessfulButton = view.findViewById<Button>(R.id.launch_successful)
        val loginSuccessfulButton = view.findViewById<Button>(R.id.login_successful)
        val purchaseSuccessfulButton = view.findViewById<Button>(R.id.purchase_successful)
        val customEventButton = view.findViewById<Button>(R.id.custom_event)
        launchSecondActivity.setOnClickListener(this)
        launchSuccessfulButton.setOnClickListener(this)
        loginSuccessfulButton.setOnClickListener(this)
        purchaseSuccessfulButton.setOnClickListener(this)
        customEventButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v) {
            launch_second_activity -> startActivity(Intent(this.activity, SecondActivity::class.java))
            launch_successful -> InAppMessaging.instance().logEvent(AppStartEvent())
            login_successful -> InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
            purchase_successful -> InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY"))
            custom_event -> InAppMessaging.instance().logEvent(
                    CustomEvent("search_event").addAttribute("KEYWORD", "BASKETBALL").addAttribute("foo", 2))
            else -> {
            }
        }
    }
}