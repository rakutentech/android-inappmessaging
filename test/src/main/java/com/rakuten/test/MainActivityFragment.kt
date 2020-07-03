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
            else -> {
            }
        }
    }

    private fun showUserInfo() {
        val contentView = LayoutInflater.from(activity).inflate(R.layout.dialog_users, null)
        val application = activity?.application as MainApplication

        val userId = contentView.findViewById<EditText>(R.id.edit_userid)
        userId.setText(application.provider.userId)
        val rakutenId = contentView.findViewById<EditText>(R.id.edit_rakutenid)
        rakutenId.setText(application.provider.rakutenId)
        val raeToken = contentView.findViewById<EditText>(R.id.edit_raetoken)
        raeToken.setText(application.provider.raeToken)

        val dialog =  AlertDialog.Builder(activity)
                .setView(contentView)
                .setTitle(R.string.dialog_title_user)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    application.provider.userId = userId.text.toString()
                    application.provider.raeToken = raeToken.text.toString()
                    application.provider.rakutenId = rakutenId.text.toString()

                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel) {dialog, _ ->
                    dialog.dismiss()
                }
                .create()

        dialog.show()
    }
}