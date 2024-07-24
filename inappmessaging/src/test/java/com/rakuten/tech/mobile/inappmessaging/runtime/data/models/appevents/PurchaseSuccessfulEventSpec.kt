package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.appevents

import com.rakuten.tech.mobile.inappmessaging.runtime.BaseTest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.rat.RatAttribute
import com.rakuten.tech.mobile.inappmessaging.runtime.manager.AnalyticsKey
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldHaveKey
import org.amshove.kluent.shouldHaveSize
import org.junit.Before
import org.junit.Test

/**
 * Test purchase logEvent class.
 */
class PurchaseSuccessfulEventSpec : BaseTest() {
    @Before
    override fun setup() {
        super.setup()
        itemList.add("item_id_1")
        itemList.add("item_id_2")
        itemList.add("item_id_3")
        event = PurchaseSuccessfulEvent()
            .purchaseAmountMicros(PURCHASE_AMOUNT_MICRO)
            .numberOfItems(NUMBER_OF_ITEMS)
            .currencyCode(CURRENCY)
            .itemIdList(itemList)
    }

    @Test
    fun `should have correct custom attributes`() {
        val event = PurchaseSuccessfulEvent()
            .currencyCode("USD")
            .numberOfItems(5)
            .purchaseAmountMicros(5000000)
        val map = event.getRatEventMap()

        map shouldHaveKey AnalyticsKey.CUSTOM_ATTRIBUTES.key
        val attr = map[AnalyticsKey.CUSTOM_ATTRIBUTES.key] as ArrayList<RatAttribute>
        attr shouldHaveSize 4
        attr shouldContain RatAttribute(PURCHASE_AMOUNT_MICROS_TAG, 5000000)
        attr shouldContain RatAttribute(NUMBER_OF_ITEMS_TAG, 5)
        attr shouldContain RatAttribute(CURRENCY_CODE_TAG, "USD")
        val other: List<Int> = emptyList()
        attr shouldContain RatAttribute(ITEM_ID_LIST_TAG, other)
    }

    companion object {
        private const val PURCHASE_AMOUNT_MICRO = 100000
        private const val NUMBER_OF_ITEMS = 3
        private const val CURRENCY = "USD"
        private val itemList: MutableList<String> = ArrayList()
        private var event: PurchaseSuccessfulEvent? = null

        // from PurchaseSuccessfulEvent
        private const val PURCHASE_AMOUNT_MICROS_TAG = "purchaseAmountMicros"
        private const val NUMBER_OF_ITEMS_TAG = "numberOfItems"
        private const val CURRENCY_CODE_TAG = "currencyCode"
        private const val ITEM_ID_LIST_TAG = "itemIdList"
    }
}
