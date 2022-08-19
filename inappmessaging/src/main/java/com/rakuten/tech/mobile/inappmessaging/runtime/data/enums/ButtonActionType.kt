package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

/**
 * Representing all In-App message's button actions.
 */
@SuppressWarnings("MagicNumber")
internal enum class ButtonActionType(val typeId: Int) {
    INVALID(0),
    REDIRECT(1),
    DEEPLINK(2),
    CLOSE(3),
    PUSH_PRIMER(4);

    companion object {

        /**
         * Gets the button action type for a given [typeId].
         * If [typeId] argument is not any of the valid id, null will be returned.
         */
        fun getById(typeId: Int): ButtonActionType? {
            for (type in values()) {
                if (type.typeId == typeId) {
                    return type
                }
            }
            return null
        }
    }
}
