package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.EventType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ValueType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.Attribute
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.rat.RatAttribute
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.AnalyticsKey
import org.jetbrains.annotations.NotNull
import java.util.Collections
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.emptyList
import kotlin.collections.set

/**
 * Purchase successful Event for host app to use.
 */
class PurchaseSuccessfulEvent : BaseEvent(EventType.PURCHASE_SUCCESSFUL, EventType.PURCHASE_SUCCESSFUL.name, false) {

    /** Purchase amount in micros, $1 = 100_000. Such as $10.58 = 1058_000. */
    private var purchaseAmountMicros = -1

    /** Number of items in this purchase. */
    private var numberOfItems = -1

    /** Currency code. */
    private var currencyCode = "UNKNOWN"

    /** List of purchased item IDs. */
    private var itemIdList: List<String> = emptyList()

    /**
     * This method sets the purchase amount in micros, $1 = 100_000. Such as $10.58 = 1058_000.
     */
    @NotNull
    fun purchaseAmountMicros(@NotNull purchaseAmountMicros: Int): PurchaseSuccessfulEvent {
        this.purchaseAmountMicros = purchaseAmountMicros
        return this
    }

    /**
     * This method sets the number of items in this purchase.
     */
    @NotNull
    fun numberOfItems(@NotNull numberOfItems: Int): PurchaseSuccessfulEvent {
        this.numberOfItems = numberOfItems
        return this
    }

    /**
     * This method sets the currency code of this purchase successful logEvent.
     */
    @NotNull
    fun currencyCode(@NotNull currencyCode: String): PurchaseSuccessfulEvent {
        this.currencyCode = currencyCode
        return this
    }

    /**
     * This method sets the list of purchased item IDs.
     */
    @NotNull
    fun itemIdList(@NotNull itemIdList: List<String>): PurchaseSuccessfulEvent {
        this.itemIdList = itemIdList
        return this
    }

    /**
     * This method returns an unmodifiable map which contains all event's attributes.
     */
    @RestrictTo(LIBRARY)
    @NotNull
    override fun getRatEventMap(): Map<String, Any> {
        // Making a list of all custom attributes.
        val attributeList = ArrayList<RatAttribute>()
        attributeList.add(RatAttribute(PURCHASE_AMOUNT_MICROS_TAG, this.purchaseAmountMicros))
        attributeList.add(RatAttribute(NUMBER_OF_ITEMS_TAG, this.numberOfItems))
        attributeList.add(RatAttribute(CURRENCY_CODE_TAG, this.currencyCode))
        attributeList.add(RatAttribute(ITEM_ID_LIST_TAG, this.itemIdList))

        // Inherit basic attributes, and add custom attributes.
        val map = HashMap(super.getRatEventMap())
        map[AnalyticsKey.CUSTOM_ATTRIBUTES.key] = attributeList

        return Collections.unmodifiableMap(map)
    }

    /**
     * This method returns a map of Attribute objects.
     * Key: Attribute's name, Value: Attribute object.
     */
    @RestrictTo(LIBRARY)
    @NotNull
    override fun getAttributeMap(): Map<String, Attribute?> {
        val map = java.util.HashMap<String, Attribute>()
        map[PURCHASE_AMOUNT_MICROS_TAG] =
            Attribute(PURCHASE_AMOUNT_MICROS_TAG, purchaseAmountMicros.toString(), ValueType.INTEGER)
        map[NUMBER_OF_ITEMS_TAG] = Attribute(NUMBER_OF_ITEMS_TAG, numberOfItems.toString(), ValueType.INTEGER)
        map[CURRENCY_CODE_TAG] = Attribute(CURRENCY_CODE_TAG, currencyCode, ValueType.STRING)
        map[ITEM_ID_LIST_TAG] = Attribute(ITEM_ID_LIST_TAG, itemIdList.toString(), ValueType.STRING)
        return map
    }

    companion object {
        private const val PURCHASE_AMOUNT_MICROS_TAG = "purchaseAmountMicros"
        private const val NUMBER_OF_ITEMS_TAG = "numberOfItems"
        private const val CURRENCY_CODE_TAG = "currencyCode"
        private const val ITEM_ID_LIST_TAG = "itemIdList"
    }
}
