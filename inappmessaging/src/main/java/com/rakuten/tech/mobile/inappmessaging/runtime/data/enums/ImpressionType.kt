package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

/**
 * All types of impressions.
 */
@SuppressWarnings("MagicNumber")
internal enum class ImpressionType(val typeId: Int) {
    INVALID(0),

    // View impression.
    IMPRESSION(1),

    // Clicking button 1.
    ACTION_ONE(2),

    // Clicking button 2.
    ACTION_TWO(3),

    // Clicking close button.
    EXIT(4),

    // Clicking content.
    CLICK_CONTENT(5),

    // Checkbox of opt-out.
    OPT_OUT(6);

    companion object {
        /**
         * Gets the impression type for a given [typeId].
         * If [typeId] argument is not any of the valid id, null will be returned.
         */
        fun getById(typeId: Int): ImpressionType? {
            for (type in values()) {
                if (type.typeId == typeId) {
                    return type
                }
            }
            return null
        }
    }
}
