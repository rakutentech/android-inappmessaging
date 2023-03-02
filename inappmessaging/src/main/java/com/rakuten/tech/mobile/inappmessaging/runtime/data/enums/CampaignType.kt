package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

internal enum class CampaignType(val typeId: Int) {
    INVALID(0),
    REGULAR(1),
    PUSH_PRIMER(2),
    ;

    companion object {

        /**
         * Gets the button action type for a given [typeId].
         * If [typeId] argument is not any of the valid id, null will be returned.
         */
        fun getById(typeId: Int): CampaignType? {
            for (type in values()) {
                if (type.typeId == typeId) {
                    return type
                }
            }
            return null
        }
    }
}
