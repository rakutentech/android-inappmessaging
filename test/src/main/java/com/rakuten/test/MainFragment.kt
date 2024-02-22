package com.rakuten.test

import android.app.AlertDialog
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

class MainFragment : Fragment(), View.OnClickListener {
    private var ignoredContexts = ""

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        view.findViewById<Button>(R.id.set_contexts).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
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
            R.id.set_contexts -> setContexts()
            else -> Any()
        }
    }

    private fun setContexts() {
        val contentView = LayoutInflater.from(activity).inflate(R.layout.dialog_contexts, null)
        val viewId = contentView.findViewById<EditText>(R.id.edit_contexts)
        viewId.setText(ignoredContexts)

        val dialog = AlertDialog.Builder(activity)
            .setView(contentView)
            .setTitle("Set Contexts")
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                ignoredContexts = viewId.text.toString().trim()
                if (ignoredContexts.isNotEmpty()) {
                    InAppMessaging.instance().onVerifyContext = { contexts, _ ->
                        contexts.intersect(ignoredContexts.split(",").toSet()).isEmpty()
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
        val appProvider = (activity?.application as MainApplication).provider

        val userId = contentView.findViewById<EditText>(R.id.edit_userid)
        userId.setText(appProvider.provideUserId())
        val tokenOrIdTrackingRadio = contentView.findViewById<RadioGroup>(R.id.dialog_radio_group)
        val isIdTracking = appProvider.isIdTracking()
        tokenOrIdTrackingRadio.check(tokenOrIdTrackingRadio.getChildAt(if (isIdTracking) 0 else 1).id)
        val tokenOrIdTracking = contentView.findViewById<EditText>(R.id.edit_tokenOrTrackingId)
        tokenOrIdTracking.setText(
            if (isIdTracking) appProvider.provideIdTrackingIdentifier() else appProvider.provideAccessToken())

        val dialog =  AlertDialog.Builder(activity)
                .setView(contentView)
                .setTitle(R.string.dialog_title_user)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    val tokenOrIdTrackingType = tokenOrIdTrackingRadio
                        .indexOfChild(tokenOrIdTrackingRadio.findViewById(tokenOrIdTrackingRadio.checkedRadioButtonId))
                    appProvider.saveUser(userId.text.toString(), tokenOrIdTracking.text.toString(),
                        tokenOrIdTrackingType == 0)
                    InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
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
                    settings.isTooltipFeatureEnabled = enableTooltip.isChecked
                    InAppMessaging.configure(it, settings.subscriptionKey, settings.configUrl,
                        enableTooltipFeature = settings.isTooltipFeatureEnabled)
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) {dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }
}