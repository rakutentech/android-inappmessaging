package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

/**
 * All types of In-App messages based on Protocol V1.
 */
@SuppressWarnings("MagicNumber")
internal enum class InAppMessageType(val typeId: Int) {
    INVALID(0),
    MODAL(1),
    FULL(2),
    SLIDE(3),
    HTML(4),
    TOOLTIP(5);

    companion object {
        /**
         * Gets the campaign message type for a given [typeId].
         * If [typeId] argument is not any of the valid id, null will be returned.
         */
        fun getById(typeId: Int): InAppMessageType? {
            for (type in values()) {
                if (type.typeId == typeId) {
                    return type
                }
            }
            return null
        }
    }
}
