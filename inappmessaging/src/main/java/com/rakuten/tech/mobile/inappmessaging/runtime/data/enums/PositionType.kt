package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

internal enum class PositionType(val typeId: String) {
    TOP_RIGHT("top-right"),
    TOP_CENTER("top-center"),
    TOP_LEFT("top-left"),
    BOTTOM_RIGHT("bottom-right"),
    BOTTOM_CENTER("bottom-center"),
    BOTTOM_LEFT("bottom-left"),
    RIGHT("right"),
    LEFT("left");

    companion object {
        /**
         * Gets the operator type for a given [typeId].
         * If [typeId] argument is not any of the valid id, null will be returned.
         */
        fun getById(typeId: String): PositionType? {
            for (type in values()) {
                if (type.typeId.equals(typeId, true)) {
                    return type
                }
            }
            return null
        }
    }
}
