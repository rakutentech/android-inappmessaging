package com.rakuten.tech.mobile.inappmessaging.runtime.coroutine

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppError
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppErrorLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.ui.UiMessage
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ButtonActionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.InAppMessageType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ValueType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.CustomEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.HostAppInfoRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.OnClickBehavior
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.TriggerAttribute
import com.rakuten.tech.mobile.inappmessaging.runtime.extensions.openAppNotifPermissionSettings
import com.rakuten.tech.mobile.inappmessaging.runtime.extensions.promptPushPermissionDialog
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.ImpressionManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.MessageReadinessManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.PushPrimerTrackerManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.CheckPermissionResult
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.PermissionUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.eventlogger.Event as ELEvent
import java.util.Date
import kotlin.collections.ArrayList

/**
 * Task which should be ran in the background.
 */
@SuppressWarnings("TooManyFunctions", "LargeClass")
internal class MessageActionsCoroutine(
    private val campaignRepo: CampaignRepository = CampaignRepository.instance(),
    private val readinessManager: MessageReadinessManager = MessageReadinessManager.instance(),
    private val hostAppRepo: HostAppInfoRepository = HostAppInfoRepository.instance(),
) {

    fun executeTask(uiMessage: UiMessage?, viewResourceId: Int, optOut: Boolean): Boolean {
        if (uiMessage == null || uiMessage.id.isEmpty()) {
            return false
        }
        // Getting ImpressionType, which represents which button was pressed:
        val buttonType = getOnClickBehaviorType(viewResourceId)
        if (uiMessage.type != InAppMessageType.TOOLTIP.typeId) {
            // Add event in the button if exist.
            addEmbeddedEvent(buttonType, uiMessage)
            // Handling onclick action for deep link, redirect, push primer, etc.
            handleAction(getOnClickBehavior(buttonType, uiMessage), uiMessage.id)
        } else if (buttonType == ImpressionType.CLICK_CONTENT) {
            handleAction(OnClickBehavior(2, uiMessage.tooltipData?.url))
        }
        // Update campaign status in repository
        updateCampaignInRepository(uiMessage, optOut)
        // Schedule to report impression.
        scheduleReportImpression(uiMessage, getImpressionTypes(optOut, buttonType))

        return true
    }

    /**
     * After InApp message was displayed and removed from screen, update it's impressions left and opt-out status.
     */
    private fun updateCampaignInRepository(uiMessage: UiMessage, isOptedOut: Boolean) {
        if (isOptedOut) {
            campaignRepo.optOutCampaign(uiMessage.id)
        }
        readinessManager.removeMessageFromQueue(uiMessage.id)
        campaignRepo.decrementImpressions(uiMessage.id)
    }

    /**
     * Returns an ImpressionType array which contains clicked button, checkbox impressions.
     */
    private fun getImpressionTypes(optOut: Boolean, impressionType: ImpressionType): MutableList<ImpressionType> {
        val impressionTypes: MutableList<ImpressionType> = ArrayList()
        impressionTypes.add(impressionType)
        if (optOut) {
            // No need to include optOut if it's false.
            impressionTypes.add(ImpressionType.OPT_OUT)
        }

        return impressionTypes
    }

    /**
     * This method returns which button was clicked which is represented by ImpressionType object.
     */
    @VisibleForTesting
    internal fun getOnClickBehaviorType(viewResourceId: Int): ImpressionType {
        return when (viewResourceId) {
            R.id.message_close_button, BACK_BUTTON -> ImpressionType.EXIT
            R.id.message_single_button, R.id.message_button_left -> ImpressionType.ACTION_ONE
            R.id.message_button_right -> ImpressionType.ACTION_TWO
            R.id.slide_up, R.id.message_tooltip_image_view, R.id.message_tip, R.id.message_image_view ->
                ImpressionType.CLICK_CONTENT
            else -> ImpressionType.INVALID
        }
    }

    /**
     * This method returns a OnClickBehavior object which is retrieved from the message argument
     * according to which content or button was clicked.
     */
    @VisibleForTesting
    internal fun getOnClickBehavior(impressionType: ImpressionType, uiMessage: UiMessage): OnClickBehavior? {
        return when {
            impressionType == ImpressionType.ACTION_ONE && uiMessage.buttons.isNotEmpty() ->
                uiMessage.buttons[0].buttonBehavior
            impressionType == ImpressionType.ACTION_TWO && (uiMessage.buttons.size >= 2) ->
                uiMessage.buttons[1].buttonBehavior
            impressionType == ImpressionType.CLICK_CONTENT -> uiMessage.content?.onClick
            else -> null
        }
    }

    /**
     * Notify ImpressionManager to report impression.
     *
     * @param uiMessage A Message object.
     * @param impressionTypes An ImpressionType of the button click.
     */
    private fun scheduleReportImpression(uiMessage: UiMessage, impressionTypes: List<ImpressionType>) {
        ImpressionManager.scheduleReportImpression(
            ImpressionManager.createImpressionList(impressionTypes),
            uiMessage.id,
            uiMessage.isTest,
        )
    }

    /**
     * This method handles button's actions accordingly, such as close message, redirect to webview, etc. Deep
     * link to another activity or app, or push permission. Context of the app is needed in order to use PackageManager
     * to check if there are browser apps installed, or request for push permission.
     */
    @VisibleForTesting
    @SuppressWarnings("CanBeNonNullable")
    internal fun handleAction(onClickBehavior: OnClickBehavior?, campaignId: String = "") {
        if (onClickBehavior == null) {
            return
        }
        val buttonActionType = ButtonActionType.getById(onClickBehavior.action)
        // Redirect is a special type of deep link, but no special handling is needed.
        if (ButtonActionType.DEEPLINK == buttonActionType || ButtonActionType.REDIRECT == buttonActionType) {
            handleDeeplinkRedirection(onClickBehavior.uri)
        } else if (ButtonActionType.PUSH_PRIMER == buttonActionType) {
            handlePushPrimer(campaignId)
        }
    }

    private fun handleDeeplinkRedirection(uri: String?) {
        // Always use activity context.
        val activityContext = HostAppInfoRepository.instance().getRegisteredActivity()
        if (!uri.isNullOrEmpty() && activityContext != null) {
            // Build an implicit intent.
            val intent = Intent(Intent.ACTION_DEFAULT, Uri.parse(uri))

            try {
                activityContext.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                InAppErrorLogger.logError(TAG, InAppError(ex = e, ev = ELEvent.CampaignRedirectActionFailed(uri)))
            }
        }
    }

    internal fun handlePushPrimer(campaignId: String, checker: BuildVersionChecker = BuildVersionChecker) {
        InAppMessaging.instance().onPushPrimer.let { callback ->
            if (callback != null) {
                PushPrimerTrackerManager.campaignId = campaignId
                callback.invoke()
            } else if (checker.isAndroidTAndAbove()) {
                PushPrimerTrackerManager.campaignId = campaignId
                requestPushNotifPermission()
            }
        }
    }

    @SuppressLint("NewApi")
    private fun requestPushNotifPermission() {
        val activity = hostAppRepo.getRegisteredActivity() ?: return

        val permissionResult = PermissionUtil.checkPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
        when (permissionResult) {
            CheckPermissionResult.CAN_ASK -> activity.promptPushPermissionDialog()
            CheckPermissionResult.PREVIOUSLY_DENIED -> activity.promptPushPermissionDialog()
            CheckPermissionResult.PERMANENTLY_DENIED -> activity.openAppNotifPermissionSettings()
            CheckPermissionResult.GRANTED -> Unit
        }
    }

    /**
     * This method adds embedded event to local event repository. Embedded event is inside Button or Content
     * object. Only add embedded event when corresponding button or content was clicked by user.
     */
    private fun addEmbeddedEvent(impressionType: ImpressionType, uiMessage: UiMessage) {
        val embeddedEvent = getEmbeddedEvent(impressionType, uiMessage) ?: return

        val event = createLocalCustomEvent(embeddedEvent) ?: return

        EventsManager.onEventReceived(event)
    }

    /**
     * This method retrieves embedded event object from message based on impressionType.
     */
    private fun getEmbeddedEvent(impressionType: ImpressionType, uiMessage: UiMessage): Trigger? {
        return if (ImpressionType.ACTION_ONE == impressionType || ImpressionType.ACTION_TWO == impressionType) {
            val index = if (impressionType == ImpressionType.ACTION_ONE) 0 else 1
            val buttons = uiMessage.buttons
            if (buttons.isEmpty()) {
                null
            } else {
                buttons[index].embeddedEvent
            }
        } else if (ImpressionType.CLICK_CONTENT == impressionType) {
            uiMessage.content?.embeddedEvent
        } else {
            null
        }
    }

    /**
     * This method creates a local custom event based on argument.
     */
    @SuppressWarnings("ReturnCount")
    private fun createLocalCustomEvent(embeddedEvent: Trigger): Event? {
        val type = embeddedEvent.eventType
        if (EventType.CUSTOM != EventType.getById(type)) {
            return null
        }
        val customEvent = CustomEvent(embeddedEvent.eventName)
        for (attribute in embeddedEvent.triggerAttributes) {
            val valueType = ValueType.getById(attribute.type) ?: return customEvent
            handleAttriType(valueType, customEvent, attribute)
        }
        return customEvent
    }

    private fun handleAttriType(valueType: ValueType, customEvent: CustomEvent, attribute: TriggerAttribute) {
        when (valueType) {
            ValueType.STRING -> customEvent.addAttribute(attribute.name, attribute.value)
            ValueType.INTEGER -> customEvent.addAttribute(attribute.name, attribute.value.toInt())
            ValueType.DOUBLE -> customEvent.addAttribute(attribute.name, attribute.value.toDouble())
            ValueType.BOOLEAN -> customEvent.addAttribute(attribute.name, attribute.value.toBoolean())
            ValueType.TIME_IN_MILLI -> customEvent.addAttribute(attribute.name, Date(attribute.value.toLong()))
            ValueType.INVALID -> Unit
        }
    }

    companion object {
        const val BACK_BUTTON = -1
        private const val TAG = "IAM_MessageActions"
    }
}
