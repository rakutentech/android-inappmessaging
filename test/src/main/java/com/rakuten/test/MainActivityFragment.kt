package com.rakuten.test

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.CustomEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.LoginSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent

class MainActivityFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.launch_second_activity).setOnClickListener(this)
        view.findViewById<Button>(R.id.launch_successful).setOnClickListener(this)
        view.findViewById<Button>(R.id.login_successful).setOnClickListener(this)
        view.findViewById<Button>(R.id.purchase_successful).setOnClickListener(this)
        view.findViewById<Button>(R.id.custom_event).setOnClickListener(this)
        view.findViewById<Button>(R.id.change_user).setOnClickListener(this)
        view.findViewById<Button>(R.id.login_successful_twice).setOnClickListener(this)
        view.findViewById<Button>(R.id.purchase_successful_twice).setOnClickListener(this)
        view.findViewById<Button>(R.id.login_purchase_successful).setOnClickListener(this)
        view.findViewById<Button>(R.id.close_message).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.launch_second_activity -> startActivity(Intent(this.activity, SecondActivity::class.java))
            R.id.launch_successful -> InAppMessaging.instance().logEvent(AppStartEvent())
            R.id.login_successful -> InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
            R.id.purchase_successful -> InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY"))
            R.id.custom_event -> InAppMessaging.instance().logEvent(
                    CustomEvent("search_event").addAttribute("KEYWORD", "BASKETBALL").addAttribute("foo", 2))
            R.id.change_user -> showUserInfo()
            R.id.login_successful_twice -> {
                InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
                InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
            }
            R.id.purchase_successful_twice -> {
                InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY"))
                InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY"))
            }
            R.id.login_purchase_successful -> {
                InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
                InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent().currencyCode("JPY"))
            }
            R.id.close_message -> {
                InAppMessaging.instance().closeMessage()
            }
            else -> Any()
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