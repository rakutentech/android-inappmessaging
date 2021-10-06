package com.rakuten.test

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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

        launch_second_activity.setOnClickListener(this)
        launch_successful.setOnClickListener(this)
        login_successful.setOnClickListener(this)
        purchase_successful.setOnClickListener(this)
        custom_event.setOnClickListener(this)
        change_user.setOnClickListener(this)
        login_successful_twice.setOnClickListener(this)
        purchase_successful_twice.setOnClickListener(this)
        login_purchase_successful.setOnClickListener(this)
        close_message.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v) {
            launch_second_activity -> startActivity(Intent(this.activity, SecondActivity::class.java))
            launch_successful -> InAppMessaging.instance().logEvent(AppStartEvent())
            login_successful -> InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
            purchase_successful -> InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY"))
            custom_event -> InAppMessaging.instance().logEvent(
                    CustomEvent("search_event").addAttribute("KEYWORD", "BASKETBALL").addAttribute("foo", 2))
            change_user -> showUserInfo()
            login_successful_twice -> {
                InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
                InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
            }
            purchase_successful_twice -> {
                InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY"))
                InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY"))
            }
            login_purchase_successful -> {
                InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
                InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY"))
            }
            close_message -> {
                InAppMessaging.instance().closeMessage()
            }
            else -> {
            }
        }
    }

    private fun showUserInfo() {
        val contentView = LayoutInflater.from(activity).inflate(R.layout.dialog_users, null)
        val application = activity?.application as MainApplication

        val userId = contentView.findViewById<EditText>(R.id.edit_userid)
        userId.setText(application.provider.userId)
        val accessToken = contentView.findViewById<EditText>(R.id.edit_accesstoken)
        accessToken.setText(application.provider.accessToken)
        val idTracking = contentView.findViewById<EditText>(R.id.edit_idTracking)
        accessToken.setText(application.provider.idTracking)

        val dialog =  AlertDialog.Builder(activity)
                .setView(contentView)
                .setTitle(R.string.dialog_title_user)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    if (application.provider.userId != userId.text.toString()) {
                        InAppMessaging.instance().closeMessage()
                    }
                    application.provider.userId = userId.text.toString()
                    application.provider.accessToken = accessToken.text.toString()
                    application.provider.idTracking = idTracking.text.toString()

                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel) {dialog, _ ->
                    dialog.dismiss()
                }
                .create()

        dialog.show()
    }
}