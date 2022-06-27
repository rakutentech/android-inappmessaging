package com.rakuten.test

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil

class MainActivityFragment : Fragment(), View.OnClickListener {

    lateinit var testButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        context?.let {
            updateUser(PreferencesUtil.getString(it, SHARED_FILE, USER_ID, "user1") ?: "user1",
            PreferencesUtil.getString(it, SHARED_FILE, ACCESS_TOKEN, "accessToken1") ?: "accessToken1")
        }
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        testButton = view.findViewById(R.id.close_message)

        view.findViewById<Button>(R.id.launch_second_activity).setOnClickListener(this)
        view.findViewById<Button>(R.id.launch_successful).setOnClickListener(this)
        view.findViewById<Button>(R.id.login_successful).setOnClickListener(this)
        view.findViewById<Button>(R.id.purchase_successful).setOnClickListener(this)
        view.findViewById<Button>(R.id.custom_event).setOnClickListener(this)
        view.findViewById<Button>(R.id.change_user).setOnClickListener(this)
        view.findViewById<Button>(R.id.login_successful_twice).setOnClickListener(this)
        view.findViewById<Button>(R.id.purchase_successful_twice).setOnClickListener(this)
        view.findViewById<Button>(R.id.login_purchase_successful).setOnClickListener(this)
        testButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.launch_second_activity -> {
                // test memory leak
                Handler(Looper.getMainLooper()).postDelayed( {
                    testButton.text = "Leak!!"
                }, 5000)
                startActivity(Intent(this.activity, SecondActivity::class.java))
            }
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

                    updateUser(userId.text.toString(), accessToken.text.toString())

                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel) {dialog, _ ->
                    dialog.dismiss()
                }
                .create()

        dialog.show()
    }

    override fun onDestroy() {
        activity?.let {
            val application = it.application as MainApplication
            PreferencesUtil.putString(it, SHARED_FILE, USER_ID, application.provider.userId)
            PreferencesUtil.putString(it, SHARED_FILE, ACCESS_TOKEN, application.provider.accessToken)
        }
        super.onDestroy()
    }

    private fun updateUser(userId: String, accessToken: String ) {
        val application = activity?.application as MainApplication
        application.provider.userId = userId
        application.provider.accessToken = accessToken
    }

    companion object {
        private const val USER_ID: String = "user-id"
        private const val SHARED_FILE: String = "user-shared-file"
        private const val ACCESS_TOKEN: String = "access-token"
    }
}