package com.rakuten.test

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.AppStartEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.CustomEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.LoginSuccessfulEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.PurchaseSuccessfulEvent
import com.rakuten.tech.mobile.sdkutils.PreferencesUtil

class MainActivityFragment : Fragment(), View.OnClickListener {

    private var tokenOrIdTrackingType = 0 // Use Tracking ID over access token by default

    override fun onCreate(savedInstanceState: Bundle?) {
        context?.let {
            updateUser(PreferencesUtil.getString(it, SHARED_FILE, USER_ID, "user1") ?: "user1",
            PreferencesUtil.getString(it, SHARED_FILE, TOKEN_OR_ID_TRACKING, "trackingId1") ?: "trackingId1")
        }
        super.onCreate(savedInstanceState)
    }

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
        view.findViewById<Button>(R.id.close_tooltip).setOnClickListener(this)
        view.findViewById<Button>(R.id.reconfigure).setOnClickListener(this)
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
            R.id.close_tooltip -> openCloseTooltipDialog()
            R.id.reconfigure -> showConfiguration()
            else -> Any()
        }
    }

    private fun openCloseTooltipDialog() {
        val contentView = LayoutInflater.from(activity).inflate(R.layout.dialog_close_tooltip, null)
        val viewId = contentView.findViewById<EditText>(R.id.edit_view_id)

        val dialog = AlertDialog.Builder(activity)
            .setView(contentView)
            .setTitle("Set the ID to close tooltip")
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                InAppMessaging.instance().closeTooltip(viewId.text.toString())
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) {dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun showUserInfo() {
        val contentView = LayoutInflater.from(activity).inflate(R.layout.dialog_users, null)
        val application = activity?.application as MainApplication

        val userId = contentView.findViewById<EditText>(R.id.edit_userid)
        userId.setText(application.provider.userId)
        val tokenOrIdTracking = contentView.findViewById<EditText>(R.id.edit_tokenOrTrackingId)
        tokenOrIdTracking.setText(application.provider.idTracking)
        val tokenOrIdTrackingRadio = contentView.findViewById<RadioGroup>(R.id.dialog_radio_group)
        tokenOrIdTrackingRadio.check(tokenOrIdTrackingRadio.getChildAt(tokenOrIdTrackingType).id)

        val dialog =  AlertDialog.Builder(activity)
                .setView(contentView)
                .setTitle(R.string.dialog_title_user)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    if (application.provider.userId != userId.text.toString()) {
                        InAppMessaging.instance().closeMessage()
                    }

                    updateUser(userId.text.toString(), tokenOrIdTracking.text.toString())
                    tokenOrIdTrackingType = tokenOrIdTrackingRadio.indexOfChild(
                        tokenOrIdTrackingRadio.findViewById(tokenOrIdTrackingRadio.checkedRadioButtonId))

                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel) {dialog, _ ->
                    dialog.dismiss()
                }
                .create()

        dialog.show()
    }

    private fun showConfiguration() {
        val contentView = LayoutInflater.from(activity).inflate(R.layout.dialog_configure, null)
        val settings = (activity?.application as MainApplication).settings

        val configUrl = contentView.findViewById<EditText>(R.id.edit_config_url)
        configUrl.setText(settings.configUrl)
        val subsKey = contentView.findViewById<EditText>(R.id.edit_subs_key)
        subsKey.setText(settings.subscriptionKey)
        val enableTooltip = contentView.findViewById<SwitchCompat>(R.id.tooltip_feat_switch)
        enableTooltip.isChecked = settings.isTooltipFeatureEnabled

        val dialog =  AlertDialog.Builder(activity)
            .setView(contentView)
            .setTitle(R.string.label_reconfigure)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                context?.let {
                    settings.subscriptionKey = subsKey.text.toString()
                    settings.configUrl = configUrl.text.toString()
                    if (enableTooltip.isEnabled != settings.isTooltipFeatureEnabled) {
                        settings.isTooltipFeatureEnabled = enableTooltip.isEnabled
                        InAppMessaging.configure(it, settings.subscriptionKey, settings.configUrl,
                            enableTooltipFeature = settings.isTooltipFeatureEnabled)
                    }
                }
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
            PreferencesUtil.putString(it, SHARED_FILE, TOKEN_OR_ID_TRACKING,
                if (tokenOrIdTrackingType == 0) application.provider.idTracking else application.provider.accessToken)
        }
        super.onDestroy()
    }

    private fun updateUser(userId: String, tokenOrIdTracking: String) {
        val application = activity?.application as MainApplication
        application.provider.userId = userId

        // Only one of access token or ID tracking is expected to be set at the same time
        if (tokenOrIdTrackingType == 0) {
            application.provider.accessToken = ""
            application.provider.idTracking = tokenOrIdTracking
        } else {
            application.provider.idTracking = ""
            application.provider.accessToken = tokenOrIdTracking
        }
    }

    companion object {
        private const val USER_ID: String = "user-id"
        private const val SHARED_FILE: String = "user-shared-file"
        private const val TOKEN_OR_ID_TRACKING: String = "token-or-id-tracking"
    }
}