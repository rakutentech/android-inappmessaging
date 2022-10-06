package com.rakuten.tech.mobile.inappmessaging.runtime.coroutine

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ButtonActionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ValueType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.CustomEvent
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents.Event
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.CampaignRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.OnClickBehavior
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.Trigger
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.EventsManager
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.ImpressionManager
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.BuildVersionChecker
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import java.util.Date
import kotlin.collections.ArrayList

/**
 * Task which should be ran in the background.
 */
@SuppressWarnings("TooGenericExceptionCaught", "TooManyFunctions", "MaxChainedCallsOnSameLine")
internal class MessageActionsCoroutine(private val campaignRepo: CampaignRepository = CampaignRepository.instance()) {

    fun executeTask(message: Message?, viewResourceId: Int, optOut: Boolean): Boolean {
        if (message == null || message.getCampaignId().isEmpty()) {
            return false
        }
        // Getting ImpressionType, which represents which button was pressed:
        val buttonType = getOnClickBehaviorType(viewResourceId)
        // Add event in the button if exist.
        addEmbeddedEvent(buttonType, message)
        // Handling onclick action for deep link, redirect, etc.
        handleAction(getOnClickBehavior(buttonType, message))
        // Update campaign status in repository
        updateCampaignInRepository(message, optOut)
        // Schedule to report impression.
        scheduleReportImpression(message, getImpressionTypes(optOut, buttonType))

        return true
    }

    /**
     * After InApp message was displayed and removed from screen, update it's impressions left and opt-out status.
     */
    private fun updateCampaignInRepository(message: Message, isOptedOut: Boolean) {
        if (isOptedOut) {
            campaignRepo.optOutCampaign(message)
        }
        campaignRepo.decrementImpressions(message.getCampaignId())
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
            R.id.message_close_button -> ImpressionType.EXIT
            R.id.message_single_button, R.id.message_button_left -> ImpressionType.ACTION_ONE
            R.id.message_button_right -> ImpressionType.ACTION_TWO
            R.id.slide_up -> ImpressionType.CLICK_CONTENT
            BACK_BUTTON -> ImpressionType.EXIT
            else -> ImpressionType.INVALID
        }
    }

    /**
     * This method returns a OnClickBehavior object which is retrieved from the message argument
     * according to which content or button was clicked.
     */
    @VisibleForTesting
    internal fun getOnClickBehavior(impressionType: ImpressionType, message: Message): OnClickBehavior? {
        val controlSettings = message.getMessagePayload().messageSettings.controlSettings

        return when {
            impressionType == ImpressionType.ACTION_ONE && controlSettings.buttons.isNotEmpty() ->
                controlSettings.buttons[0].buttonBehavior
            impressionType == ImpressionType.ACTION_TWO && (controlSettings.buttons.size >= 2) ->
                controlSettings.buttons[1].buttonBehavior
            impressionType == ImpressionType.CLICK_CONTENT -> controlSettings.content?.onClick
            else -> null
        }
    }

    /**
     * Notify ImpressionManager to report impression.
     *
     * @param message A Message object.
     * @param impressionTypes An ImpressionType of the button click.
     */
    private fun scheduleReportImpression(message: Message, impressionTypes: List<ImpressionType>) {
        ImpressionManager.scheduleReportImpression(
            ImpressionManager.createImpressionList(impressionTypes),
            message.getCampaignId(),
            message.isTest()
        )
    }

    /**
     * This method handles button's actions accordingly, such as close message, redirect to webview, etc. Deep
     * link to another activity or app, or push permission. Context of the app is needed in order to use PackageManager
     * to check if there are browser apps installed, or request for push permission.
     */
    @VisibleForTesting
    @SuppressWarnings("CanBeNonNullable")
    internal fun handleAction(onClickBehavior: OnClickBehavior?) {
        if (onClickBehavior == null) {
            return
        }
        val buttonActionType = ButtonActionType.getById(onClickBehavior.action)
        // Redirect is a special type of deep link, but no special handling is needed.
        if (ButtonActionType.DEEPLINK == buttonActionType || ButtonActionType.REDIRECT == buttonActionType) {
            handleDeeplinkRedirection(onClickBehavior.uri)
        } else if (ButtonActionType.PUSH_PRIMER == buttonActionType) {
            handlePushPrimer()
        }
    }

    private fun handleDeeplinkRedirection(uri: String?) {
        // Always use activity context.
        val activityContext = InAppMessaging.instance().getRegisteredActivity()
        if (!uri.isNullOrEmpty() && activityContext != null) {
            // Build an implicit intent.
            val intent = Intent(Intent.ACTION_DEFAULT, Uri.parse(uri))

            try {
                activityContext.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                InAppLogger(TAG).debug(e.message)
            }
        }
    }

    @SuppressLint("InlinedApi")
    @SuppressWarnings("NestedScopeFunctions")
    internal fun handlePushPrimer(buildChecker: BuildVersionChecker = BuildVersionChecker.instance()) {
        InAppMessaging.instance().onPushPrimer.let {
            if (it != null) {
                it.invoke()
            } else if (buildChecker.isAndroidTAndAbove()) {
                InAppMessaging.instance().getRegisteredActivity()?.let { act ->
                    ActivityCompat.requestPermissions(
                        act, arrayOf(Manifest.permission.POST_NOTIFICATIONS), InAppMessaging.PUSH_PRIMER_REQ_CODE
                    )
                }
            }
        }
    }

    /**
     * This method adds embedded event to local event repository. Embedded event is inside Button or Content
     * object. Only add embedded event when corresponding button or content was clicked by user.
     */
    private fun addEmbeddedEvent(impressionType: ImpressionType, message: Message) {
        val embeddedEvent = getEmbeddedEvent(impressionType, message) ?: return

        val event = createLocalCustomEvent(embeddedEvent) ?: return

        EventsManager.onEventReceived(event)
    }

    /**
     * This method retrieves embedded event object from message based on impressionType.
     */
    @SuppressWarnings("LongMethod", "ComplexCondition", "ReturnCount", "MaxLineLength", "MaximumLineLength")
    private fun getEmbeddedEvent(impressionType: ImpressionType, message: Message): Trigger? {
        if (ImpressionType.ACTION_ONE == impressionType || ImpressionType.ACTION_TWO == impressionType) {
            val index = if (impressionType == ImpressionType.ACTION_ONE) 0 else 1
            return if (message.getMessagePayload().messageSettings.controlSettings.buttons.isEmpty()) {
                null
            } else {
                message.getMessagePayload().messageSettings.controlSettings.buttons[index].embeddedEvent
            }
        } else if (ImpressionType.CLICK_CONTENT == impressionType) {
            return message.getMessagePayload().messageSettings.controlSettings.content?.embeddedEvent
        }
        return null
    }

    /**
     * This method creates a local custom event based on argument.
     */
    @SuppressWarnings("LongMethod", "ReturnCount", "ComplexMethod", "ElseCaseInsteadOfExhaustiveWhen")
    private fun createLocalCustomEvent(embeddedEvent: Trigger): Event? {
        val type = embeddedEvent.eventType
        if (EventType.CUSTOM != EventType.getById(type)) {
            return null
        }

        val eventName = embeddedEvent.eventName

        val customEvent = CustomEvent(eventName)
        val attributes = embeddedEvent.triggerAttributes
        for (attribute in attributes) {
            val valueType = ValueType.getById(attribute.type) ?: return customEvent
            when (valueType) {
                ValueType.STRING -> customEvent.addAttribute(attribute.name, attribute.value)
                ValueType.INTEGER -> customEvent.addAttribute(attribute.name, attribute.value.toInt())
                ValueType.DOUBLE -> customEvent.addAttribute(attribute.name, attribute.value.toDouble())
                ValueType.BOOLEAN -> customEvent.addAttribute(attribute.name, attribute.value.toBoolean())
                ValueType.TIME_IN_MILLI -> customEvent.addAttribute(attribute.name, Date(attribute.value.toLong()))
                else -> Unit
            }
        }
        return customEvent
    }

    companion object {
        const val BACK_BUTTON = -1
        private const val TAG = "IAM_MessageActions"
    }
}
