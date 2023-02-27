package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

/**
 * Representing all In-App message's animation directions.
 */
@SuppressWarnings("MagicNumber")
internal enum class SlideFromDirectionType(val typeId: Int) {
    INVALID(0),
    BOTTOM(1),
    TOP(2), // Currently not support.
    LEFT(3),
    RIGHT(4),
    ;

    companion object {
        /**
         * Gets the slide from type for a given [typeId].
         * If [typeId] argument is not any of the valid id, null will be returned.
         */
        fun getById(typeId: Int): SlideFromDirectionType {
            for (type in values()) {
                if (type.typeId == typeId) {
                    return type
                }
            }
            return INVALID
        }
    }
}
